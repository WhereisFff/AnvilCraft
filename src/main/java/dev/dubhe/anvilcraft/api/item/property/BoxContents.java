package dev.dubhe.anvilcraft.api.item.property;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    public static final BoxContents EMPTY = new BoxContents(List.of(), 0, 0);
    public static final Codec<BoxContents> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        ItemStack.CODEC.listOf().fieldOf("amulets").forGetter(o -> o.amulets),
        Codec.INT.fieldOf("totems").forGetter(o -> o.totemCount),
        Codec.INT.fieldOf("selection").forGetter(o -> o.selection)
    ).apply(ins, BoxContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BoxContents> STREAM_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
        it -> it.amulets,
        ByteBufCodecs.INT,
        it -> it.totemCount,
        ByteBufCodecs.INT,
        it -> it.selection,
        BoxContents::new
    );
    @Getter
    private final List<ItemStack> amulets;
    @Getter
    private final int totemCount;
    @Getter
    private final int selection;
    @Getter
    private final int usage;

    BoxContents(List<ItemStack> amulets, int totemCount, int selectedItemIndex) {
        this.amulets = amulets;
        this.totemCount = totemCount;
        this.selection = selectedItemIndex;
        this.usage = computeUsage();
    }

    BoxContents(List<ItemStack> amulets, int totemCount, int selectedItemIndex, int computedUsage) {
        this.amulets = amulets;
        this.totemCount = totemCount;
        this.selection = selectedItemIndex;
        this.usage = computedUsage;
    }

    public int sum(ToIntFunction<ItemStack> fn) {
        int sum = 0;
        for (ItemStack it : amulets) {
            int i = fn.applyAsInt(it);
            sum += i;
        }
        return sum + totemCount;
    }

    int computeUsage() {
        return sum(it -> it.getItem() instanceof AmuletItem ? 6 : 0);
    }

    public List<ItemStack> allItems() {
        ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
        builder.addAll(amulets);
        for (int i = 0; i < totemCount; i++) {
            builder.add(Items.TOTEM_OF_UNDYING.getDefaultInstance());
        }
        return builder.build();
    }

    public BoxContents.Mutable mutable() {
        return new Mutable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoxContents that)) return false;
        return totemCount == that.totemCount
            && selection == that.selection
            && usage == that.usage
            && Objects.equals(amulets, that.amulets);
    }

    public boolean isEmpty() {
        return usage <= 0;
    }

    public boolean isAmuletEmpty() {
        return usage >= 0 && amulets.isEmpty() && totemCount > 0;
    }

    public int getMaxSelection() {
        return amulets.size() - 1 + 1; // this makes sense
    }

    @Override
    public int hashCode() {
        return Objects.hash(amulets, totemCount, selection, usage);
    }

    public static class Mutable {
        private final List<ItemStack> amulets;
        private int totemCount;
        private int selection;
        private int usage;

        Mutable(BoxContents contents) {
            this.amulets = new ArrayList<>(contents.amulets);
            this.totemCount = contents.totemCount;
            this.usage = contents.computeUsage();
            this.selection = contents.selection;
        }

        public boolean tryInsert(ItemStack itemStack) {
            if (itemStack.getItem() instanceof AmuletItem item) {
                if (usage + item.getWeight() > AmuletBoxItem.CAPACITY) return false;
                usage += item.getWeight();
                amulets.add(itemStack.copy());
                return true;
            }
            if (itemStack.is(Items.TOTEM_OF_UNDYING)) {
                if (usage + 1 > AmuletBoxItem.CAPACITY) return false;
                usage++;
                totemCount++;
                return true;
            }
            return false;
        }

        public ItemStack pop() {
            if (amulets.isEmpty()) {
                if (totemCount <= 0) return ItemStack.EMPTY;
                totemCount--;
                usage--;
                return Items.TOTEM_OF_UNDYING.getDefaultInstance();
            }
            ItemStack first = amulets.removeFirst();
            if (first.getItem() instanceof AmuletItem item) {
                usage -= item.getWeight();
            }
            usage = Math.clamp(usage, 0, AmuletBoxItem.CAPACITY);
            return first.copy();
        }

        public void select(int selection) {

        }

        public BoxContents immutable() {
            return new BoxContents(ImmutableList.copyOf(this.amulets), totemCount, selection);
        }

        public ItemStack popTotem() {
            if (totemCount > 0) {
                totemCount--;
                usage = Math.clamp(usage - 1, 0, AmuletBoxItem.CAPACITY);
                return Items.TOTEM_OF_UNDYING.getDefaultInstance();
            }
            return ItemStack.EMPTY;
        }
    }
}
