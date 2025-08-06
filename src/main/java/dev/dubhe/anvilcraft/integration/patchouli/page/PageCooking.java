package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.neo.wrap.CookingRecipe;
import net.minecraft.world.level.block.Blocks;

public class PageCooking extends PageAnvilItemProcess<CookingRecipe> {
    public PageCooking() {
        super(
            ModRecipeTypes.COOKING_TYPE.get(),
            CookingRecipe::getItemIngredients,
            CookingRecipe::getResults,
            recipe -> Blocks.CAULDRON.defaultBlockState(),
            recipe -> Blocks.CAMPFIRE.defaultBlockState());
    }
}
