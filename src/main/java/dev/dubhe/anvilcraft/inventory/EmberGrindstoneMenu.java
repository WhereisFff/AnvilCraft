package dev.dubhe.anvilcraft.inventory;

import com.google.common.collect.Collections2;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModMenuTypes;

import dev.dubhe.anvilcraft.util.ListUtil;
import dev.dubhe.anvilcraft.util.MenuUtil;
import lombok.Getter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EmberGrindstoneMenu extends AbstractContainerMenu {
    private final Container tool;
    private final Container book;
    private final Container resultBook;
    private final ContainerLevelAccess access;

    @Getter
    private final DataSlot selectedIndex = MenuUtil.standalone(-1);
    @Getter
    private final DataSlot cost = DataSlot.standalone();
    @Getter
    private final List<EnchantmentInstance> enchantments = new CopyOnWriteArrayList<>();
    private DataComponentType<ItemEnchantments> componentType;

    public EmberGrindstoneMenu(MenuType<EmberGrindstoneMenu> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public EmberGrindstoneMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        this(ModMenuTypes.EMBER_GRINDSTONE.get(), containerId, playerInventory, access);
    }

    /**
     * 皇家砂轮菜单
     *
     * @param type            菜单类型
     * @param containerId     容器id
     * @param playerInventory 背包
     * @param access          检查
     */
    public EmberGrindstoneMenu(
        MenuType<EmberGrindstoneMenu> type,
        int containerId,
        Inventory playerInventory,
        ContainerLevelAccess access) {
        super(type, containerId);
        this.tool = new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                EmberGrindstoneMenu.this.slotsChanged(this);
            }
        };
        this.book = new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                EmberGrindstoneMenu.this.slotsChanged(this);
            }
        };
        this.resultBook = new ResultContainer();
        this.access = access;
        this.addSlot(new Slot(this.tool, 0, 25, 24) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.isDamageableItem() || stack.is(Items.ENCHANTED_BOOK) || stack.isEnchanted();
            }

            @Override
            public void set(@NotNull ItemStack stack) {
                super.set(stack);
                componentType = EnchantmentHelper.getComponentType(stack);
                enchantments.clear();
                enchantments.addAll(Collections2.transform(
                    stack.getOrDefault(componentType, ItemEnchantments.EMPTY).entrySet(),
                    entry -> new EnchantmentInstance(entry.getKey(), entry.getIntValue())));
                enchantments.removeIf(
                    inst -> inst.enchantment.is(EnchantmentTags.CURSE));
            }
        });
        this.addSlot(new Slot(this.book, 0, 25, 42) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.BOOK);
            }
        });
        this.addSlot(new Slot(this.resultBook, 0, 145, 34) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                if (!player.level().isClientSide) {
                    int selected = selectedIndex.get();
                    if (selected < 0 || selected >= enchantments.size()) return;

                    ItemStack toolItem = tool.getItem(0);
                    ItemEnchantments.Mutable enchantmentsCopy = new ItemEnchantments.Mutable(
                        toolItem.getOrDefault(componentType, ItemEnchantments.EMPTY));
                    enchantmentsCopy.removeIf(holder -> holder.equals(enchantments.get(selectedIndex.get()).enchantment));
                    toolItem.set(componentType, enchantmentsCopy.toImmutable());
                    getSlot(0).set(toolItem);

                    ItemStack bookItem = book.getItem(0);
                    bookItem.shrink(1);
                    getSlot(1).set(bookItem);

                    setCarried(stack);
                }

                super.onTake(player, stack);
                player.playSound(SoundEvents.GRINDSTONE_USE);

                selectedIndex.set(-1);
                cost.set(0);
            }
        });
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.selectedIndex.set(-1);
        this.addDataSlot(this.selectedIndex);
        this.addDataSlot(this.cost);
    }

    private void createResult() {
        ItemStack result = ItemStack.EMPTY;
        if (this.selectedIndex.get() > -1 && !this.getSlot(0).getItem().isEmpty() && !this.getSlot(1).getItem().isEmpty()) {
            result = EnchantedBookItem.createForEnchantment(this.enchantments.get(this.selectedIndex.get()));
        }
        this.resultBook.setItem(0, result);
        this.refreshCost();
    }

    private void refreshCost() {
        ItemStack input = this.getSlot(0).getItem();
        EnchantmentInstance enchantment = this.getSelectedEnchantment();
        if (enchantment == null) return;
        int repairCost = input.getOrDefault(DataComponents.REPAIR_COST, 0);
        int anvilCost = enchantment.enchantment.value().getAnvilCost();
        this.cost.set(Math.clamp(
            (long) anvilCost * enchantment.level * input.getCount() * (repairCost <= 0 ? 1 : repairCost),
            0, Integer.MAX_VALUE
        ));
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        if (this.getSlot(0).getItem().isEmpty()) this.setSelectedEnchantment(-1);
        if (container != this.resultBook) this.createResult();
    }

    public void setSelectedEnchantment(int index) {
        this.selectedIndex.set(index);
        this.createResult();
    }

    private @Nullable EnchantmentInstance getSelectedEnchantment() {
        return ListUtil.safelyGet(this.enchantments, this.selectedIndex.get());
    }

    public int calculateMaxRowIndex() {
        return Mth.positiveCeilDiv(this.enchantments.size(), 3) - 2;
    }

    public int getRowIndexForScroll(float scrollOffs) {
        return Math.max((int) (scrollOffs * this.calculateMaxRowIndex()), 0);
    }

    public float getScrollForRowIndex(int rowIndex) {
        return Mth.clamp((float) rowIndex / this.calculateMaxRowIndex(), 0.0F, 1.0F);
    }

    public float subtractInputFromScroll(float scrollOffs, double input) {
        return Mth.clamp(scrollOffs - (float) (input / this.calculateMaxRowIndex()), 0.0F, 1.0F);
    }

    public boolean canScroll() {
        return this.enchantments.size() > 6;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack clickedItem = slot.getItem();
            itemStack = clickedItem.copy();
            if (index >= 0 && index <= 2) {
                if (!this.moveItemStackTo(itemStack, 3, 38, false)) {
                    return ItemStack.EMPTY;
                } else {
                    if (index == 2) {
                        slot.onTake(player, clickedItem);
                        return ItemStack.EMPTY;
                    }
                    int surplus = clickedItem.getCount() - clickedItem.getMaxStackSize();
                    ItemStack stack = surplus > 0 ? clickedItem.copyWithCount(surplus) : ItemStack.EMPTY;
                    this.getSlot(index).setByPlayer(stack);
                }
            } else {
                ItemStack book;
                if (itemStack.isDamageableItem() || itemStack.is(Items.ENCHANTED_BOOK) || itemStack.isEnchanted()) {
                    if (!this.getSlot(0).hasItem()) {
                        this.getSlot(0).setByPlayer(itemStack);
                        this.getSlot(index).setByPlayer(ItemStack.EMPTY);
                    } else {
                        return ItemStack.EMPTY;
                    }
                } else if (itemStack.is(Items.BOOK)) {
                    if (!this.getSlot(1).hasItem()) {
                        this.getSlot(1).setByPlayer(itemStack);
                        this.getSlot(index).setByPlayer(ItemStack.EMPTY);
                    } else if (
                        (book = this.getSlot(1).getItem()).is(Items.BOOK)
                        && book.getCount() < book.getMaxStackSize()
                    ) {
                        int canSet = book.getMaxStackSize() - book.getCount();
                        canSet = Math.min(itemStack.getCount(), canSet);
                        book.grow(canSet);
                        itemStack.shrink(canSet);
                        this.getSlot(1).setByPlayer(book);
                        this.getSlot(index).setByPlayer(itemStack);
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, ModBlocks.EMBER_GRINDSTONE.get());
    }

    /**
     * 移除
     *
     * @param player 玩家
     */
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> {
            this.clearContainer(player, this.tool);
            this.clearContainer(player, this.book);
        });
    }

    protected void clearContainer(Player player, @NotNull Container container) {
        int i;
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
            for (i = 0; i < container.getContainerSize(); ++i) {
                player.drop(container.removeItemNoUpdate(i), false);
            }

        } else {
            for (i = 0; i < container.getContainerSize(); ++i) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.removeItemNoUpdate(i));
                }
            }
        }
    }
}
