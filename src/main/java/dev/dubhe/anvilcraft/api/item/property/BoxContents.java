package dev.dubhe.anvilcraft.api.item.property;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.item.amulet.AmuletBoxItem;
import dev.dubhe.anvilcraft.item.amulet.AmuletItem;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

@SuppressWarnings("unused")
public final class BoxContents implements TooltipComponent {
    public static final BoxContents EMPTY = new BoxContents(List.of(), List.of(), 0);
    public static final Codec<BoxContents> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        ItemStack.CODEC.listOf().fieldOf("amulets").forGetter(BoxContents::getAmulets),
        ItemStack.CODEC.listOf().fieldOf("totems").forGetter(BoxContents::getTotems),
        Codec.INT.fieldOf("selection").forGetter(BoxContents::getSelection)
    ).apply(ins, BoxContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxContents> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), BoxContents::getAmulets,
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), BoxContents::getTotems,
        ByteBufCodecs.INT, BoxContents::getSelection,
        BoxContents::new
    );
    @Getter
    private final List<ItemStack> amulets;
    @Getter
    private final List<ItemStack> totems;
    @Getter
    private final int selection;
    @Getter
    private final int usage;

    BoxContents(List<ItemStack> amulets, List<ItemStack> totems, int selectedItemIndex) {
        this.amulets = amulets;
        this.totems = totems;
        this.selection = selectedItemIndex;
        this.usage = computeUsage();
    }

    BoxContents(List<ItemStack> amulets, List<ItemStack> totems, int selectedItemIndex, int computedUsage) {
        this.amulets = amulets;
        this.totems = totems;
        this.selection = selectedItemIndex;
        this.usage = computedUsage;
    }

    public int sum(ToIntFunction<ItemStack> fn) {
        int sum = totems.size();
        for (ItemStack it : amulets) {
            int i = fn.applyAsInt(it);
            sum += i;
        }
        return sum;
    }

    int computeUsage() {
        return sum(it -> it.getItem() instanceof AmuletItem ? 6 : 0);
    }

    public List<ItemStack> allItems() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        builder.addAll(amulets);
        builder.addAll(totems);
        return builder.build();
    }

    public BoxContents.Mutable mutable() {
        return new Mutable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoxContents that)) return false;
        return selection == that.selection
               && usage == that.usage
               && Objects.equals(amulets, that.amulets)
               && Objects.equals(totems, that.totems);
    }

    public boolean isEmpty() {
        return usage <= 0;
    }

    public boolean isAmuletEmpty() {
        return usage >= 0 && amulets.isEmpty() && !totems.isEmpty();
    }

    public int getMaxSelection() {
        return amulets.size() + totems.size(); // this makes sense
    }

    @Override
    public int hashCode() {
        return Objects.hash(amulets, totems, selection, usage);
    }

    public static class Mutable {
        private final List<ItemStack> amulets;
        private final List<ItemStack> totems;
        private int selection;
        private int usage;

        Mutable(BoxContents contents) {
            this.amulets = new ArrayList<>(contents.amulets);
            this.totems = new ArrayList<>(contents.totems);
            this.usage = contents.computeUsage();
            this.selection = contents.selection;
        }

        public boolean tryInsert(ItemStack itemStack) {
            if (itemStack.getItem() instanceof AmuletItem item) {
                if (usage + item.getWeight() > AmuletBoxItem.CAPACITY) return false;
                usage += item.getWeight();
                amulets.add(itemStack.copy());
                return true;
            } else if (itemStack.is(ModItemTags.TOTEM)) {
                if (usage + 1 > AmuletBoxItem.CAPACITY) return false;
                usage++;
                totems.add(itemStack.copy());
                return true;
            }
            return false;
        }

        public ItemStack pop() {
            ItemStack stack = ItemStack.EMPTY;

            if (amulets.size() > this.selection) {
                stack = amulets.remove(this.selection);
                if (stack.getItem() instanceof AmuletItem item) {
                    usage -= item.getWeight();
                }
            } else if (totems.size() > this.selection - amulets.size()) {
                stack = totems.remove(this.selection - amulets.size());
                if (stack.is(ModItemTags.TOTEM)) {
                    usage--;
                }
            }

            usage = Math.clamp(usage, 0, AmuletBoxItem.CAPACITY);
            return stack.copy();
        }

        public void select(int selection) {
            this.selection = selection;
        }

        public BoxContents immutable() {
            return new BoxContents(ImmutableList.copyOf(amulets), ImmutableList.copyOf(totems), selection);
        }

        public ItemStack popTotem() {
            if (totems.isEmpty()) return ItemStack.EMPTY;
            ItemStack first = totems.removeFirst();
            if (first.is(ModItemTags.TOTEM)) {
                usage--;
            }
            usage = Math.clamp(usage, 0, AmuletBoxItem.CAPACITY);
            return first;
        }
    }
}
