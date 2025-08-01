package dev.dubhe.anvilcraft.integration.patchouli.page;

import dev.dubhe.anvilcraft.integration.patchouli.util.PatchouliRenderHelper;
import dev.dubhe.anvilcraft.recipe.anvil.ItemInjectRecipe;
import dev.dubhe.anvilcraft.util.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

import java.util.List;

public class PageItemInject extends PageDoubleRecipeRegistry<ItemInjectRecipe> {
    public PageItemInject(RecipeType<ItemInjectRecipe> recipeType) {
        super(recipeType);
//        super(ModRecipeTypes.ITEM_INJECT_TYPE.get());
    }

    @Override
    protected void drawRecipe(
            GuiGraphics graphics, ItemInjectRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second
    ) {
        PatchouliRenderHelper.render1x1(graphics, recipeX - 4, recipeY + 16);

        List<Object2IntMap.Entry<Ingredient>> ingredients = recipe.getMergedIngredients();
        PatchouliRenderHelper.renderIngredientWithCount(parent, graphics, ingredients.get(0), recipeX, recipeY + 20, mouseX, mouseY);

        PatchouliRenderHelper.renderArray(graphics, recipeX + 25, recipeY + 20);

        PatchouliRenderHelper.renderAnvilWithAnimation(parent, graphics, recipeX + 50, recipeY + 15);

        RenderHelper.renderBlock(graphics, recipe.inputBlock.defaultBlockState(),
                recipeX + 50, recipeY + 31, 0,
                12,
                RenderHelper.SINGLE_BLOCK);

        PatchouliRenderHelper.renderArray(graphics, recipeX + 66, recipeY + 20);

        RenderHelper.renderBlock(graphics, recipe.resultBlock.defaultBlockState(),
                recipeX + 90, recipeY + 31, 0,
                12,
                RenderHelper.SINGLE_BLOCK);

        parent.drawCenteredStringNoShadow(
                graphics, getTitle(second).getVisualOrderText(),
                GuiBook.PAGE_WIDTH / 2, recipeY - 5,
                book.headerColor
        );
    }

    @Override
    protected ItemStack getRecipeOutput(Level level, ItemInjectRecipe recipe) {
        return recipe.getResultItem(level.registryAccess());
    }

    @Override
    protected int getRecipeHeight() {
        return 78;
    }
}
