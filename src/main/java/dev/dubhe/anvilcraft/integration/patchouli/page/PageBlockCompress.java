package dev.dubhe.anvilcraft.integration.patchouli.page;

import com.mojang.datafixers.util.Either;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.integration.patchouli.util.PatchouliRenderHelper;
import dev.dubhe.anvilcraft.recipe.anvil.BlockCompressRecipe;
import dev.dubhe.anvilcraft.util.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

import java.util.List;

public class PageBlockCompress extends PageDoubleRecipeRegistry<BlockCompressRecipe> {
    public PageBlockCompress() {
        super(ModRecipeTypes.BLOCK_COMPRESS_TYPE.get());
    }

    @Override
    protected void drawRecipe(
            GuiGraphics graphics, BlockCompressRecipe recipe,
            int recipeX, int recipeY, int mouseX, int mouseY, boolean second
    ) {
        PatchouliRenderHelper.renderAnvilWithAnimation(parent, graphics, recipeX + 20, recipeY + 10);

        List<Either<TagKey<Block>, Block>> inputs = recipe.inputs;
        for (int i = 0; i < Math.min(inputs.size(), 2); i++) {
            RenderHelper.renderBlock(graphics, recipe.inputs.get(i).right().get().defaultBlockState(),
                    recipeX + 20, recipeY + 26 + i * 11, 0,
                    12,
                    RenderHelper.SINGLE_BLOCK);
        }

        PatchouliRenderHelper.renderArray(graphics, recipeX + 45, recipeY + 25);

        RenderHelper.renderBlock(graphics, recipe.result.defaultBlockState(),
                recipeX + 80, recipeY + 37, 0,
                12,
                RenderHelper.SINGLE_BLOCK);

        parent.drawCenteredStringNoShadow(
                graphics, getTitle(second).getVisualOrderText(),
                GuiBook.PAGE_WIDTH / 2, recipeY - 5,
                book.headerColor
        );
    }

    @Override
    protected ItemStack getRecipeOutput(Level level, BlockCompressRecipe recipe) {
        return recipe.getResultItem(level.registryAccess());
    }

    @Override
    protected int getRecipeHeight() {
        return 78;
    }
}
