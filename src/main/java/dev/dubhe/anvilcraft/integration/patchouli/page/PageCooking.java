package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.CookingRecipe;
import net.minecraft.world.level.block.Blocks;


public class PageCooking extends PageAnvilItemProcess<CookingRecipe> {
    public PageCooking() {
        super(
                ModRecipeTypes.COOKING_TYPE,
                CookingRecipe::getMergedIngredients,
                CookingRecipe::getResults,
                recipe -> Blocks.CAULDRON.defaultBlockState(),
                recipe -> Blocks.CAMPFIRE.defaultBlockState()
        );
    }
}
