package dev.dubhe.anvilcraft.item.amulet;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.item.property.BoxContents;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AmuletBoxItem extends Item {
    public static final int MAX_SHOWN_GRID_ITEMS_X = 4;
    public static final int MAX_SHOWN_GRID_ITEMS = 12;
    public static final int OVERFLOWING_MAX_SHOWN_GRID_ITEMS = 11;
    private static final int FULL_BAR_COLOR = 0xFF5454FF;
    private static final int BAR_COLOR = 0x7087FFFF;

    public AmuletBoxItem(Properties properties) {
        super(properties.component(ModComponents.BOX_CONTENTS, BoxContents.EMPTY));
    }

    @SuppressWarnings("removal")
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ItemProperties.register(
            this,
            AnvilCraft.of("box_contents"),
            (stack, level, entity, seed) -> getFullnessDisplay(stack)
        );
    }

    public static float getFullnessDisplay(ItemStack itemStack) {
        BoxContents contents = itemStack.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
        return contents.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        BoxContents contents = itemStack.get(ModComponents.BOX_CONTENTS);
        if (contents == null) {
            return false;
        } else {
            ItemStack itemStack2 = slot.getItem();
            BoxContents.Mutable mutable = new BoxContents.Mutable(contents);
            if (clickAction == ClickAction.PRIMARY && !itemStack2.isEmpty()) {
                if (mutable.tryTransfer(slot, player) > 0) {
                    playInsertSound(player);
                }

                itemStack.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
                this.broadcastChangesOnContainerMenu(player);
                return true;
            } else if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty()) {
                ItemStack itemStack3 = mutable.removeOne();
                if (itemStack3 != null) {
                    ItemStack itemStack4 = slot.safeInsert(itemStack3);
                    if (itemStack4.getCount() > 0) {
                        mutable.tryInsert(itemStack4);
                    } else {
                        playRemoveOneSound(player);
                    }
                }

                itemStack.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
                this.broadcastChangesOnContainerMenu(player);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (clickAction == ClickAction.PRIMARY && itemStack2.isEmpty()) {
            return false;
        } else {
            BoxContents bundleContents = itemStack.get(ModComponents.BOX_CONTENTS);
            if (bundleContents == null) {
                return false;
            } else {
                BoxContents.Mutable mutable = new BoxContents.Mutable(bundleContents);
                if (clickAction == ClickAction.PRIMARY && !itemStack2.isEmpty()) {
                    if (slot.allowModification(player) && mutable.tryInsert(itemStack2.copy()) > 0) {
                        playInsertSound(player);
                        itemStack2.shrink(itemStack2.getCount());
                    }

                    itemStack.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
                    this.broadcastChangesOnContainerMenu(player);
                    return true;
                } else if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty()) {
                    if (slot.allowModification(player)) {
                        ItemStack itemStack3 = mutable.removeOne();
                        if (itemStack3 != null) {
                            playRemoveOneSound(player);
                            slotAccess.set(itemStack3);
                        }
                    }

                    itemStack.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
                    this.broadcastChangesOnContainerMenu(player);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        Inventory inventory = player.getInventory();
        ItemStack box = InventoryUtil.getFirstItem(inventory, this);
        if (!level.isClientSide) {
            BoxContents contents = box.get(ModComponents.BOX_CONTENTS);
            if (contents == null) return InteractionResultHolder.fail(box);
            BoxContents.Mutable mutable = new BoxContents.Mutable(contents);
            if (!player.isShiftKeyDown()) {
                List<ItemStack> items = InventoryUtil.getItems(inventory);
                for (ItemStack stack : items) {
                    if (stack.isEmpty() || !stack.is(Items.TOTEM_OF_UNDYING)) continue;
                    if (mutable.tryInsert(stack.copy()) != 0) {
                        inventory.removeItem(stack);
                    }
                }
                box.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
            } else {
                boolean droped = false;
                for (int i = 0; i < contents.totemCount(); i++) {
                    ItemStack stack = mutable.removeOneTotem();
                    if (stack == null) continue;
                    InventoryUtil.addToInventory(player.getInventory(), stack);
                    droped = true;
                }
                if (droped) {
                    playDropContentsSound(level, player);
                }
                box.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(box);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        BoxContents contents = itemStack.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
        return contents.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack itemStack) {
        BoxContents contents = itemStack.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate(contents.weight(), MAX_SHOWN_GRID_ITEMS), 13);
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        BoxContents contents = itemStack.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
        return contents.weight().compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        BoxContents contents = stack.get(ModComponents.BOX_CONTENTS);
        int count = 0;
        if (contents != null) {
            count = Mth.mulAndTruncate(contents.weight(), 16);
        }
        tooltipComponents.add(Component.translatable("tooltip.anvilcraft.item.amulet_box.line_1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.anvilcraft.item.amulet_box.line_2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable(
            "tooltip.anvilcraft.item.amulet_box.fullness", count, 16
        ).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource source) {
        BoxContents contents = itemEntity.getItem().get(ModComponents.BOX_CONTENTS);
        if (contents != null) {
            itemEntity.getItem().set(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
            ItemUtils.onContainerDestroyed(itemEntity, contents.itemsCopy());
        }
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playDropContentsSound(Level level, Entity entity) {
        level.playSound(
            null, entity.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F
        );
    }

    private void broadcastChangesOnContainerMenu(Player player) {
        player.containerMenu.slotsChanged(player.getInventory());
    }
}
