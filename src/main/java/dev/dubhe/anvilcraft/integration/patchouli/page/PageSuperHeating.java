package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.SuperHeatingRecipe;
import dev.dubhe.anvilcraft.util.CauldronUtil;
import dev.dubhe.anvilcraft.util.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

public class PageSuperHeating extends PageAnvilItemProcess<SuperHeatingRecipe> {
    public PageSuperHeating(RecipeType<SuperHeatingRecipe> recipeType) {
        super(
            recipeType,
            SuperHeatingRecipe::getMergedIngredients,
            SuperHeatingRecipe::getResults,
            recipe -> Blocks.CAULDRON.defaultBlockState(),
            recipe -> ModBlocks.HEATER.getDefaultState()
        );
    }

    @Override
    protected void drawExtra(
        GuiGraphics graphics, SuperHeatingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY,
        boolean second
    ) {
        if (recipe.getResults().isEmpty() && recipe.blockResult != null) {
            RenderHelper.renderBlock(
                graphics,
                CauldronUtil.fullState(recipe.blockResult),
                recipeX + 85, recipeY + 29, 10,
                12,
                RenderHelper.SINGLE_BLOCK
            );
        }
    }
}
