package dev.dubhe.anvilcraft.recipe.neo.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class MeshRecipe extends AbstractProcessRecipe<MeshRecipe> {

    public MeshRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            Vec3.ZERO,
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -0.6, 0.0),
            BlockStatePredicate.builder()
                .of(Blocks.SCAFFOLDING)
                .build()
        );
    }

    @Override
    public @NotNull RecipeSerializer<MeshRecipe> getSerializer() {
        return ModRecipeTypes.MESH_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<MeshRecipe> getType() {
        return ModRecipeTypes.MESH_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<MeshRecipe> {
        @Override
        protected MeshRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new MeshRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends SimpleAbstractBuilder<MeshRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "mesh";
        }

        @Override
        protected MeshRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new MeshRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
