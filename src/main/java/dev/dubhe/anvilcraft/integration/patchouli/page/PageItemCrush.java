package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.ItemCrushRecipe;

public class PageItemCrush extends PageAnvilItemProcess<ItemCrushRecipe> {
    public PageItemCrush() {
        super(
            ModRecipeTypes.ITEM_CRUSH_TYPE.get(),
            ItemCrushRecipe::getItemIngredients,
            ItemCrushRecipe::getResults,
            recipe -> ModBlocks.CRUSHING_TABLE.getDefaultState(),
            null);
    }
}
