package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class UnpackRecipe extends AbstractProcessRecipe<UnpackRecipe> {

    public UnpackRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            new Property()
                .setItemInputOffset(Vec3.ZERO)
                .setInputItems(itemIngredients)
                .setItemOutputOffset(new Vec3(0.0, -1.0, 0.0))
                .setResultItems(results)
                .setBlockInputOffset(new Vec3(0.0, -1.0, 0.0))
                .setInputBlocks(
                    BlockStatePredicate.builder()
                        .of(Blocks.IRON_TRAPDOOR)
                        .with(TrapDoorBlock.HALF, Half.TOP)
                        .with(TrapDoorBlock.OPEN, false)
                        .build()
                )
        );
    }

    @Override
    public @NotNull RecipeSerializer<UnpackRecipe> getSerializer() {
        return ModRecipeTypes.UNPACK_SERIALIZERS.get();
    }

    @Override
    public @NotNull RecipeType<UnpackRecipe> getType() {
        return ModRecipeTypes.UNPACK_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<UnpackRecipe> {
        @Override
        protected UnpackRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new UnpackRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends SimpleAbstractBuilder<UnpackRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "unpack";
        }

        @Override
        protected UnpackRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new UnpackRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
