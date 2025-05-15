package dev.dubhe.anvilcraft.item.amulet;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import dev.dubhe.anvilcraft.util.PlayerUtil;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.neoforged.neoforge.common.Tags;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AmuletBoxItem extends Item {
    public AmuletBoxItem(Properties properties) {
        super(properties.component(ModComponents.TOTEM_COUNT, 0));
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        Inventory inventory = player.getInventory();
        ItemStack box = InventoryUtil.getFirstItem(inventory, this);
        if (!level.isClientSide) {
            if (!player.isShiftKeyDown()) {
                while (
                    !this.isFullTotem(box)
                        && inventory.contains(stack -> stack.getItem().equals(Items.TOTEM_OF_UNDYING))
                ) {
                    ItemStack totem = InventoryUtil.getFirstItem(inventory, Items.TOTEM_OF_UNDYING);
                    box.set(ModComponents.TOTEM_COUNT, box.get(ModComponents.TOTEM_COUNT) + 1);
                    totem.shrink(1);
                }
            } else {
                BoxContents contents = box.get(ModComponents.BOX_CONTENTS);
                if (contents == null) return InteractionResultHolder.fail(box);
                List<ItemStack> totems
                for (int i = box.get(ModComponents.TOTEM_COUNT); i > 0; i--) {
                    InventoryUtil.addToInventory(player.getInventory(), Items.TOTEM_OF_UNDYING.getDefaultInstance());
                }
                box.set(ModComponents.BOX_CONTENTS, B);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(box);
        }
        return super.use(level, player, usedHand);
    }

    @SuppressWarnings("DataFlowIssue")
    private boolean isFullTotem(ItemStack stack) {
        return !(stack.getItem() instanceof AmuletBoxItem) || stack.get(ModComponents.BOX_CONTENTS).weight.doubleValue() == 1;
    }

    public int getMaxTotemCount() {
        return 16;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.anvilcraft.item.amulet_box.line_1").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.anvilcraft.item.amulet_box.line_2").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable(
            "tooltip.anvilcraft.item.amulet_box.totem_count",
            stack.get(ModComponents.TOTEM_COUNT)
        ).withStyle(ChatFormatting.GRAY));
    }

    public static float getFullnessDisplay(ItemStack stack) {
        BoxContents contents = stack.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
        return contents.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (stack.getCount() != 1 || action != ClickAction.SECONDARY) {
            return false;
        } else {
            BoxContents contents = stack.get(ModComponents.BOX_CONTENTS);
            if (contents == null) {
                return false;
            } else {
                ItemStack itemstack = slot.getItem();
                BoxContents.Mutable mutable = new BoxContents.Mutable(contents);
                if (itemstack.isEmpty()) {
                    this.playRemoveOneSound(player);
                    ItemStack stack1 = mutable.removeOne();
                    if (stack1 != null) {
                        ItemStack stack2 = slot.safeInsert(stack1);
                        mutable.tryInsert(stack2);
                    }
                } else if (itemstack.getItem().canFitInsideContainerItems()) {
                    int i = mutable.tryTransfer(slot, player);
                    if (i > 0) {
                        this.playInsertSound(player);
                    }
                }

                stack.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
                return true;
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(
        ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access
    ) {
        if (stack.getCount() != 1) return false;
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            BoxContents contents = stack.get(ModComponents.BOX_CONTENTS);
            if (contents == null) {
                return false;
            } else {
                BoxContents.Mutable mutable = new BoxContents.Mutable(contents);
                if (other.isEmpty()) {
                    ItemStack itemstack = mutable.removeOne();
                    if (itemstack != null) {
                        this.playRemoveOneSound(player);
                        access.set(itemstack);
                    }
                } else {
                    int i = mutable.tryInsert(other);
                    if (i > 0) {
                        this.playInsertSound(player);
                    }
                }

                stack.set(ModComponents.BOX_CONTENTS, mutable.toImmutable());
                return true;
            }
        } else {
            return false;
        }
    }

    public static final class BoxContents implements TooltipComponent {
        public static final BoxContents EMPTY = new BoxContents(List.of());
        public static final Codec<BoxContents> CODEC = ItemStack.CODEC.listOf().xmap(BoxContents::new, BoxContents::items);
        public static final StreamCodec<RegistryFriendlyByteBuf, BoxContents> STREAM_CODEC = ItemStack.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(BoxContents::new, BoxContents::items);
        private static final Fraction TOTEM = Fraction.getFraction(1, 16);
        private static final Fraction AMULET = Fraction.getFraction(6, 16);
        private static final int NO_STACK_INDEX = -1;
        final List<ItemStack> items;
        final Fraction weight;

        BoxContents(List<ItemStack> items, Fraction weight) {
            this.items = items;
            this.weight = weight;
        }

        public BoxContents(List<ItemStack> items) {
            this(items, computeContentWeight(items));
        }

        private static Fraction computeContentWeight(List<ItemStack> content) {
            Fraction fraction = Fraction.ZERO;

            for (ItemStack itemstack : content) {
                fraction = fraction.add(getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
            }

            return fraction;
        }

        static Fraction getWeight(ItemStack stack) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                return TOTEM;
            } else if (stack.is(ModItemTags.AMULET)) {
                return AMULET;
            }
            throw new IllegalArgumentException("Amulet Box can only put Totem of Undying or Amulet");
        }

        public ItemStack getItemUnsafe(int index) {
            return this.items.get(index);
        }

        public Stream<ItemStack> itemCopyStream() {
            return this.items.stream().map(ItemStack::copy);
        }

        public List<ItemStack> items() {
            return this.items;
        }

        public List<ItemStack> itemsCopy() {
            return Lists.transform(this.items, ItemStack::copy);
        }

        public int size() {
            return this.items.size();
        }

        public Fraction weight() {
            return this.weight;
        }

        public boolean isEmpty() {
            return this.items.isEmpty();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else {
                return other instanceof BoxContents contents && this.weight.equals(contents.weight)
                       && ItemStack.listMatches(this.items, contents.items);
            }
        }

        @Override
        public int hashCode() {
            return ItemStack.hashStackList(this.items);
        }

        @Override
        public String toString() {
            return "BoxContents" + this.items;
        }

        public static class Mutable {
            private final List<ItemStack> items;
            private Fraction weight;

            public Mutable(BoxContents contents) {
                this.items = new ArrayList<>(contents.items);
                this.weight = contents.weight;
            }

            public Mutable clearItems() {
                this.items.clear();
                this.weight = Fraction.ZERO;
                return this;
            }

            private int findStackIndex(ItemStack stack) {
                if (stack.isStackable()) {
                    for (int i = 0; i < this.items.size(); i++) {
                        if (ItemStack.isSameItemSameComponents(this.items.get(i), stack)) {
                            return i;
                        }
                    }
                }
                return -1;
            }

            private int getMaxAmountToAdd(ItemStack stack) {
                Fraction fraction = Fraction.ONE.subtract(this.weight);
                return Math.max(fraction.divideBy(BoxContents.getWeight(stack)).intValue(), 0);
            }

            public int tryInsert(ItemStack stack) {
                if (!stack.isEmpty() && stack.getItem().canFitInsideContainerItems()) {
                    int i = Math.min(stack.getCount(), this.getMaxAmountToAdd(stack));
                    if (i == 0) {
                        return 0;
                    } else {
                        this.weight = this.weight.add(BoxContents.getWeight(stack).multiplyBy(Fraction.getFraction(i, 1)));
                        int j = this.findStackIndex(stack);
                        if (j != -1) {
                            ItemStack stack1 = this.items.remove(j);
                            ItemStack stack2 = stack1.copyWithCount(stack1.getCount() + i);
                            stack.shrink(i);
                            this.items.addFirst(stack2);
                        } else {
                            this.items.addFirst(stack.split(i));
                        }

                        return i;
                    }
                } else {
                    return 0;
                }
            }

            public int tryTransfer(Slot slot, Player player) {
                ItemStack itemstack = slot.getItem();
                int i = this.getMaxAmountToAdd(itemstack);
                return this.tryInsert(slot.safeTake(itemstack.getCount(), i, player));
            }

            @Nullable
            public ItemStack removeOne() {
                if (this.items.isEmpty()) {
                    return null;
                } else {
                    ItemStack itemstack = this.items.removeFirst().copy();
                    this.weight = this.weight.subtract(BoxContents.getWeight(itemstack).multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
                    return itemstack;
                }
            }

            public Fraction weight() {
                return this.weight;
            }

            public BoxContents toImmutable() {
                return new BoxContents(List.copyOf(this.items), this.weight);
            }
        }
    }
}
