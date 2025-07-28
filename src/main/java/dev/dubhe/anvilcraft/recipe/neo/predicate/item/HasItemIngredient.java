package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemCache;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
}
