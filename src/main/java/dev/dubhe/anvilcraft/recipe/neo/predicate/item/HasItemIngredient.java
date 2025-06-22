package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
        context.putAcceptor(HasItemBase.ITEM_CACHE.location(), ctx -> {
            ItemCache itemCache = ctx.get(ITEM_CACHE);
            if (itemCache != null) itemCache.endCache();
        });
    }

    public static class Type implements IRecipePredicate.Type<HasItemIngredient> {
        private static final MapCodec<HasItemIngredient> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(HasItemIngredient::getOffset),
                Vec3.CODEC.fieldOf("range").forGetter(HasItemIngredient::getRange),
                ItemIngredientPredicate.CODEC.fieldOf("item").forGetter(HasItemIngredient::getItem)
            ).apply(instance, HasItemIngredient::new)
        );

        @Override
        public @NotNull MapCodec<HasItemIngredient> codec() {
            return Type.CODEC;
        }

        @Override
        public boolean conflict() {
            return true;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, HasItemIngredient> streamCodec() {
            return StreamCodec.of(Type::encode, Type::decode);
        }

        public static void encode(@NotNull FriendlyByteBuf buf, @NotNull HasItemIngredient hasItem) {
            buf.writeVec3(hasItem.getOffset());
            buf.writeVec3(hasItem.getRange());
            Tag tag = ItemIngredientPredicate.CODEC.encodeStart(NbtOps.INSTANCE, hasItem.item).getOrThrow();
            buf.writeNbt(tag);
        }

        public static @NotNull HasItemIngredient decode(@NotNull FriendlyByteBuf buf) {
            Vec3 offset = buf.readVec3();
            Vec3 range = buf.readVec3();
            ItemIngredientPredicate item = ItemIngredientPredicate.CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow().getFirst();
            return new HasItemIngredient(offset, range, item);
        }
    }
}
