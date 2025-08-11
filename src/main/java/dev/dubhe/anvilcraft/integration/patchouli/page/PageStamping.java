package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.StampingRecipe;


public class PageStamping extends PageAnvilItemProcess<StampingRecipe> {
    public PageStamping() {
        super(
                ModRecipeTypes.STAMPING_TYPE,
                StampingRecipe::getMergedIngredients,
                StampingRecipe::getResults,
                recipe -> ModBlocks.STAMPING_PLATFORM.getDefaultState(),
                null
        );
    }
}
