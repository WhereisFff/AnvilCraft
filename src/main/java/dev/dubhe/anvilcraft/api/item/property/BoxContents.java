package dev.dubhe.anvilcraft.api.item.property;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.item.amulet.AmuletBoxItem;
import dev.dubhe.anvilcraft.util.CodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class BoxContents implements TooltipComponent {
    public static final BoxContents EMPTY = new BoxContents(List.of(), List.of());
    public static final Codec<BoxContents> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        ItemStack.CODEC.listOf().fieldOf("amulets").forGetter(o -> o.amulets),
        ItemStack.CODEC.listOf().fieldOf("totems").forGetter(o -> o.totems)
    ).apply(ins, BoxContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxContents> STREAM_CODEC = CodecUtil
        .createPairStreamCodec(
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())
        ).map(BoxContents::new, BoxContents::itemsPair);
    private static final Fraction TOTEM = Fraction.getFraction(1, 16);
    private static final Fraction AMULET = Fraction.getFraction(6, 16);
    private final List<ItemStack> amulets;
    private final List<ItemStack> totems;
    private final Fraction weight;

    BoxContents(List<ItemStack> amulets, List<ItemStack> totems, Fraction weight) {
        this.amulets = amulets;
        this.totems = totems;
        this.weight = weight;
    }

    public BoxContents(List<ItemStack> amulets, List<ItemStack> totems) {
        this(amulets, totems, computeContentWeight(amulets, totems));
    }

    public BoxContents(Pair<List<ItemStack>, List<ItemStack>> amuletsAndTotems) {
        this(amuletsAndTotems.getFirst(), amuletsAndTotems.getSecond());
    }

    private static Fraction computeContentWeight(List<ItemStack> amulets, List<ItemStack> totems) {
        Fraction fraction = Fraction.ZERO;

        for (ItemStack itemstack : amulets) {
            fraction = fraction.add(AMULET.multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
        }
        for (ItemStack itemstack : totems) {
            fraction = fraction.add(TOTEM.multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
        }

        return fraction;
    }

    public static boolean canItemBeInBundle(ItemStack itemStack) {
        return !itemStack.isEmpty()
               && (itemStack.is(ModItemTags.AMULET) || itemStack.is(Items.TOTEM_OF_UNDYING));
    }

    public int getNumberOfItemsToShow() {
        int size = this.size();
        int max = size > AmuletBoxItem.MAX_SHOWN_GRID_ITEMS
                ? AmuletBoxItem.OVERFLOWING_MAX_SHOWN_GRID_ITEMS
                : AmuletBoxItem.MAX_SHOWN_GRID_ITEMS;
        int remainder = size % AmuletBoxItem.MAX_SHOWN_GRID_ITEMS_X;
        int flowed = remainder == 0 ? 0 : AmuletBoxItem.MAX_SHOWN_GRID_ITEMS_X - remainder;
        return Math.min(size, max - flowed);
    }

    public ItemStack peek(int index) {
        return this.itemsMerged().get(index).copy();
    }

    public Stream<ItemStack> itemCopyStream() {
        return this.items().stream().map(ItemStack::copy);
    }

    public List<ItemStack> items() {
        List<ItemStack> items = new ArrayList<>();
        items.addAll(this.amulets);
        items.addAll(this.totems);
        return items;
    }

    public List<ItemStack> itemsMerged() {
        List<ItemStack> items = new ArrayList<>(this.amulets);
        ItemStack defaultTotem = Items.TOTEM_OF_UNDYING.getDefaultInstance();
        for (ItemStack totem : this.totems) {
            if (ItemStack.isSameItemSameComponents(defaultTotem, totem)) {
                defaultTotem = defaultTotem.copyWithCount(defaultTotem.getCount() + 1);
            } else {
                items.add(totem);
            }
        }
        items.add(this.amulets.size(), defaultTotem);
        return items;
    }

    public Pair<List<ItemStack>, List<ItemStack>> itemsPair() {
        return new Pair<>(this.amulets, this.totems);
    }

    public List<ItemStack> itemsCopy() {
        return Lists.transform(this.items(), ItemStack::copy);
    }

    public int size() {
        return this.items().size();
    }

    public int totemCount() {
        return this.totems.size();
    }

    public Fraction weight() {
        return this.weight;
    }

    public boolean isEmpty() {
        return this.items().isEmpty();
    }

    public boolean isFull() {
        return this.weight.compareTo(Fraction.ONE) == 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof BoxContents contents && this.weight.equals(contents.weight)
                   && ItemStack.listMatches(this.items(), contents.items());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int hashCode() {
        return ItemStack.hashStackList(this.items());
    }

    @Override
    public String toString() {
        return "BoxContents" + this.items();
    }

    public static class Mutable {
        private final List<ItemStack> amulets;
        private final List<ItemStack> totems;
        private Fraction weight;

        public Mutable(BoxContents contents) {
            this.amulets = new ArrayList<>(contents.amulets);
            this.totems = new ArrayList<>(contents.totems);
            this.weight = contents.weight;
        }

        public Mutable clearItems() {
            this.amulets.clear();
            this.totems.clear();
            this.weight = Fraction.ZERO;
            return this;
        }

        public List<ItemStack> items() {
            List<ItemStack> items = new ArrayList<>();
            items.addAll(this.amulets);
            items.addAll(this.totems);
            return items;
        }

        public boolean isFull() {
            return this.weight.compareTo(Fraction.ONE) == 0;
        }

        private int getMaxAmountToAdd(ItemStack stack) {
            Fraction fraction = Fraction.ONE.subtract(this.weight);
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                return Math.max(fraction.divideBy(TOTEM).intValue(), 0);
            } else if (stack.is(ModItemTags.AMULET)) {
                return Math.max(fraction.divideBy(AMULET).intValue(), 0);
            } else {
                return 0;
            }
        }

        public int tryInsert(ItemStack stack) {
            if (stack.isEmpty() || this.isFull() || this.getMaxAmountToAdd(stack) == 0) return 0;
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                this.totems.add(stack);
                this.weight = this.weight.add(TOTEM.multiplyBy(Fraction.getFraction(stack.getCount(), 1)));
                return stack.getCount();
            } else if (stack.is(ModItemTags.AMULET)) {
                this.amulets.add(stack);
                this.weight = this.weight.add(AMULET.multiplyBy(Fraction.getFraction(stack.getCount(), 1)));
                return stack.getCount();
            } else {
                return 0;
            }
        }

        public int tryTransfer(Slot slot, Player player) {
            ItemStack stack = slot.getItem();
            int i = this.getMaxAmountToAdd(stack);
            return this.tryInsert(slot.safeTake(stack.getCount(), i, player));
        }

        @Nullable
        public ItemStack peekOne() {
            if (this.items().isEmpty()) {
                return null;
            } else {
                return this.items().getFirst().copy();
            }
        }

        @Nullable
        public ItemStack peekOneTotem() {
            if (this.totems.isEmpty()) {
                return null;
            } else {
                return this.totems.getFirst().copy();
            }
        }

        @Nullable
        public ItemStack removeOne() {
            if (this.items().isEmpty()) {
                return null;
            } else {
                Fraction weight = AMULET;
                List<ItemStack> items = this.amulets;
                if (items.isEmpty()) {
                    items = this.totems;
                    weight = TOTEM;
                }
                if (items.isEmpty()) return null;
                ItemStack itemstack = items.removeFirst().copy();
                this.weight = this.weight.subtract(weight.multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
                return itemstack;
            }
        }

        @Nullable
        public ItemStack removeOneTotem() {
            if (this.totems.isEmpty()) {
                return null;
            } else {
                ItemStack itemstack = this.totems.removeFirst().copy();
                this.weight = this.weight.subtract(TOTEM.multiplyBy(Fraction.getFraction(itemstack.getCount(), 1)));
                return itemstack;
            }
        }

        public Fraction weight() {
            return this.weight;
        }

        public BoxContents toImmutable() {
            return new BoxContents(this.amulets, this.totems, this.weight);
        }
    }
}
