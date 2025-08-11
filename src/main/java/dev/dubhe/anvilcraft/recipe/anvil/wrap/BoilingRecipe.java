package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.util.HasCauldronSimple;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BoilingRecipe extends AbstractProcessRecipe<BoilingRecipe> {
    public BoilingRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            new Vec3(0.0, -1.0, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -1.0, 0.0),
            HasCauldronSimple
                .fluid(ResourceLocation.withDefaultNamespace("water"))
                .build(),
            BlockStatePredicate.builder()
                .of(Blocks.CAMPFIRE)
                .with(CampfireBlock.LIT, true)
                .build()
        );
    }

    @Override
    public @NotNull RecipeSerializer<BoilingRecipe> getSerializer() {
        return ModRecipeTypes.BOILING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<BoilingRecipe> getType() {
        return ModRecipeTypes.BOILING_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<BoilingRecipe> {
        @Override
        protected BoilingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new BoilingRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends SimpleAbstractBuilder<BoilingRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "boiling";
        }

        @Override
        protected BoilingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new BoilingRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
