package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class CookingRecipe extends AbstractProcessRecipe<CookingRecipe> {
    public CookingRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            new Vec3(0.0, -1.0, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.0, 0.0),
            results,
            new Vec3(0.0, -1.0, 0.0),
            HasCauldronSimple.empty().build(),
            BlockStatePredicate.builder()
                .of(Blocks.CAMPFIRE)
                .with(CampfireBlock.LIT, true)
                .build()
        );
    }

    @Override
    public @NotNull RecipeSerializer<CookingRecipe> getSerializer() {
        return ModRecipeTypes.COOKING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<CookingRecipe> getType() {
        return ModRecipeTypes.COOKING_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<CookingRecipe> {
        @Override
        protected CookingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new CookingRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends SimpleAbstractBuilder<CookingRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "cooking";
        }

        @Override
        protected CookingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new CookingRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
