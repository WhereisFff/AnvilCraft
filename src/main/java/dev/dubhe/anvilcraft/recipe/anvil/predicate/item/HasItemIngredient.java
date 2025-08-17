package dev.dubhe.anvilcraft.recipe.anvil.predicate.item;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.anvil.cache.ItemCache;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.advancements.critereon.ItemSubPredicate;
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
public class HasItemIngredient extends HasItemBase<HasItemIngredient, ItemIngredientPredicate> {
    public HasItemIngredient(Vec3 offset, Vec3 range, ItemIngredientPredicate item) {
        super(offset, range, item);
    }

    @Override
    public IRecipePredicate.Type<HasItemIngredient> getType() {
        return ModRecipePredicateTypes.HAS_ITEM_INGREDIENT.get();
    }

    @Override
    public void snapshot(@NotNull InWorldRecipeContext context) {
        ItemCache.ICacheInput input = this.getItem(context);
        input.shrink(this.item.count());
    }

    @Override
    public void rollback(@NotNull InWorldRecipeContext context) {
        ItemCache.ICacheInput input = this.getItem(context);
        input.rollbackShrink();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        context.putAcceptor(ItemCache.ITEM_CACHE.location(), ItemCache.DEFAULT_ACCEPTOR);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Type extends AbstractType<HasItemIngredient, ItemIngredientPredicate> {
        @Override
        protected HasItemIngredient create(Vec3 offset, Vec3 range, ItemIngredientPredicate item) {
            return new HasItemIngredient(offset, range, item);
        }

        @Override
        protected ItemIngredientPredicate decodeItem(@NotNull FriendlyByteBuf buf) {
            return ItemIngredientPredicate.CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow().getFirst();
        }

        @Override
        protected void encodeItem(@NotNull FriendlyByteBuf buf, ItemIngredientPredicate item) {
            Tag tag = ItemIngredientPredicate.CODEC.encodeStart(NbtOps.INSTANCE, item).getOrThrow();
            buf.writeNbt(tag);
        }

        @Override
        protected RecordCodecBuilder<HasItemIngredient, ItemIngredientPredicate> itemCodec() {
            return ItemIngredientPredicate.CODEC.fieldOf("item").forGetter(HasItemIngredient::getItem);
        }
    }

    public static class Builder {
        private Vec3 offset = Vec3.ZERO;
        private Vec3 range = new Vec3(1.0, 1.0, 1.0);
        private final ItemIngredientPredicate.Builder item = ItemIngredientPredicate.Builder.item();

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

        public Builder count(int count) {
            this.item.withCount(count);
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

        public HasItemIngredient build() {
            return new HasItemIngredient(offset, range, item.build());
        }
    }
}
