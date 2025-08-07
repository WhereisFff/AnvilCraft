package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.ItemCompressRecipe;
import net.minecraft.world.level.block.Blocks;

public class PageItemCompress extends PageAnvilItemProcess<ItemCompressRecipe> {
    public PageItemCompress() {
        super(
                ModRecipeTypes.ITEM_COMPRESS_TYPE,
                ItemCompressRecipe::getMergedIngredients,
                ItemCompressRecipe::getResults,
                recipe -> Blocks.CAULDRON.defaultBlockState(),
                null
        );
    }
}