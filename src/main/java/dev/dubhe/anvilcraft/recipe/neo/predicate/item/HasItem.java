package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemPredicate;
import lombok.Getter;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class HasItem extends HasItemBase<HasItem, ItemPredicate> {
    public HasItem(Vec3 offset, Vec3 range, ItemPredicate item) {
        super(offset, range, item);
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public IRecipePredicate.Type<HasItem> getType() {
        return ModRecipePredicateTypes.HAS_ITEM.get();
    }

    public static class Type extends AbstractType<HasItem, ItemPredicate> {
        @Override
        protected HasItem create(Vec3 offset, Vec3 range, ItemPredicate item) {
            return new HasItem(offset, range, item);
        }

        @Override
        protected ItemPredicate decodeItem(@NotNull FriendlyByteBuf buf) {
            return ItemPredicate.CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow().getFirst();
        }

        @Override
        protected void encodeItem(@NotNull FriendlyByteBuf buf, ItemPredicate item) {
            Tag tag = ItemPredicate.CODEC.encodeStart(NbtOps.INSTANCE, item).getOrThrow();
            buf.writeNbt(tag);
        }

        @Override
        protected RecordCodecBuilder<HasItem, ItemPredicate> itemCodec() {
            return ItemPredicate.CODEC.fieldOf("item").forGetter(HasItem::getItem);
        }
    }

    public static class Builder {
        private Vec3 offset = Vec3.ZERO;
        private Vec3 range = new Vec3(1.0, 1.0, 1.0);
        private final ItemPredicate.Builder item = ItemPredicate.Builder.item();

        public Builder offset(Vec3 offset) {
            this.offset = offset;
            return this;
        }

        public Builder offset(double x, double y, double z) {
            return this.offset(new Vec3(x, y, z));
        }

        public Builder below(double below) {
            return this.offset(Vec3.ZERO.subtract(0, below, 0));
        }

        public Builder below() {
            return this.below(1);
        }

        public Builder above(double above) {
            return this.offset(Vec3.ZERO.add(0, above, 0));
        }

        public Builder above() {
            return this.above(1);
        }

        public Builder range(Vec3 range) {
            this.range = range;
            return this;
        }

        public Builder range(double x, double y, double z) {
            this.range = new Vec3(x, y, z);
            return this;
        }

        public Builder range(double range) {
            this.range = new Vec3(range, range, range);
            return this;
        }

        public Builder of(ItemLike... items) {
            this.item.of(items);
            return this;
        }

        public Builder of(TagKey<Item> tag) {
            this.item.of(tag);
            return this;
        }

        public Builder count(MinMaxBounds.Ints count) {
            this.item.withCount(count);
            return this;
        }

        public Builder moreThan(int min) {
            this.item.withCount(MinMaxBounds.Ints.atLeast(min));
            return this;
        }

        public Builder between(int min, int max) {
            this.item.withCount(MinMaxBounds.Ints.between(min, max));
            return this;
        }

        public Builder lessThan(int min) {
            this.item.withCount(MinMaxBounds.Ints.atMost(min));
            return this;
        }

        public <T extends ItemSubPredicate> Builder with(ItemSubPredicate.Type<T> type, T predicate) {
            this.item.withSubPredicate(type, predicate);
            return this;
        }

        public Builder has(DataComponentPredicate components) {
            this.item.hasComponents(components);
            return this;
        }

        public HasItem build() {
            return new HasItem(offset, range, item.build());
        }
    }
}
