package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.UnpackRecipe;
import net.minecraft.world.level.block.Blocks;

public class PageUnpack extends PageAnvilItemProcess<UnpackRecipe>{
    public PageUnpack() {
        super(
                ModRecipeTypes.UNPACK_TYPE,
                UnpackRecipe::getMergedIngredients,
                UnpackRecipe::getResults,
                recipe -> Blocks.IRON_TRAPDOOR.defaultBlockState(),
                null
        );
    }
}
