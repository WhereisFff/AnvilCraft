package dev.dubhe.anvilcraft.recipe.neo.predicate.block;

import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockCache;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class HasBlockIngredient extends HasBlockBase<HasBlockIngredient> {
    public HasBlockIngredient(Vec3 offset, BlockStatePredicate predicate) {
        super(offset, predicate);
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        BlockPos blockPos = BlockPos.containing(context.getPos().add(this.offset));
        BlockCache cache = context.computeIfAbsent(BlockCache.BLOCK_CACHE);
        cache.removeBlock(blockPos);
        context.putAcceptor(BlockCache.BLOCK_CACHE.location(), BlockCache.DEFAULT_ACCEPTOR);
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_BLOCK_INGREDIENT.get();
    }

    public static class Type extends AbstractType<HasBlockIngredient> {
        @Override
        public HasBlockIngredient of(Vec3 offset, BlockStatePredicate predicate) {
            return new HasBlockIngredient(offset, predicate);
        }
    }
}
