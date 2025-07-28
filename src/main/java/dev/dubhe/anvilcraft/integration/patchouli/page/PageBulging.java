package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.BulgingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;


public class PageBulging extends PageAnvilItemProcess<BulgingRecipe> {
    public PageBulging() {
        super(
                ModRecipeTypes.BULGING_TYPE,
                BulgingRecipe::getMergedIngredients,
                BulgingRecipe::getResults,
                recipe -> Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 3),
                null
        );
    }
}
