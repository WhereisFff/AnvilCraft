package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.BulgingRecipe;
import dev.dubhe.anvilcraft.util.CauldronUtil;
import dev.dubhe.anvilcraft.util.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Blocks;


public class PageBulging extends PageAnvilItemProcess<BulgingRecipe> {
    public PageBulging() {
        super(
                ModRecipeTypes.BULGING_TYPE,
                BulgingRecipe::getMergedIngredients,
                BulgingRecipe::getResults,
                recipe -> CauldronUtil.fullState(Blocks.WATER_CAULDRON),
                null
        );
    }

    @Override
    protected void drawExtra(
            GuiGraphics graphics, BulgingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY,
            boolean second
    ) {
        if (recipe.getResults().isEmpty() && recipe.getCauldron() != null) {
            RenderHelper.renderBlock(
                    graphics,
                    CauldronUtil.fullState(recipe.getCauldron()),
                    recipeX + 90, recipeY + 29, 10,
                    12,
                    RenderHelper.SINGLE_BLOCK
            );
        }
    }
}
