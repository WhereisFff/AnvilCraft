package dev.dubhe.anvilcraft.recipe.neo.predicate.block;

import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
public class HasBlock extends HasBlockBase<HasBlock> {
    public HasBlock(Vec3 offset, BlockStatePredicate predicate) {
        super(offset, predicate);
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_BLOCK.get();
    }

    public static class Type extends AbstractType<HasBlock> {
        @Override
        public HasBlock of(Vec3 offset, BlockStatePredicate predicate) {
            return new HasBlock(offset, predicate);
        }
    }
}
