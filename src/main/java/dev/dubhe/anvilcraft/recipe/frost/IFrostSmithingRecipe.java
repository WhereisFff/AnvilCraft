package dev.dubhe.anvilcraft.recipe.frost;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public interface IFrostSmithingRecipe extends Recipe<FrostSmithingRecipeInput> {
    @Override
    default boolean matches(FrostSmithingRecipeInput input, Level level) {
        return this.isTemplate(input.template())
               && (!this.hasMaterial() || this.isMaterial(input.material()))
               && this.isInput(input.input());
    }

    boolean isTemplate(ItemStack template);

    boolean isMaterial(ItemStack material);

    default boolean hasMaterial() {
        return true;
    }

    boolean isInput(ItemStack input);

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }
}
