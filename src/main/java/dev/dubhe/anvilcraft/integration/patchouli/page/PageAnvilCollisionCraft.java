package dev.dubhe.anvilcraft.integration.patchouli.page;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.block.GiantAnvilBlock;
import dev.dubhe.anvilcraft.block.state.Cube3x3PartHalf;
import dev.dubhe.anvilcraft.block.state.GiantAnvilCube;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.integration.patchouli.util.PatchouliRenderHelper;
import dev.dubhe.anvilcraft.mixin.accessor.ScreenAccessor;
import dev.dubhe.anvilcraft.recipe.anvil.collision.AnvilCollisionCraftRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.collision.BlockTransform;
import dev.dubhe.anvilcraft.recipe.elements.OutputItem;
import dev.dubhe.anvilcraft.util.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

import java.util.List;

public class PageAnvilCollisionCraft extends PageDoubleRecipeRegistry<AnvilCollisionCraftRecipe> {
    private static final int COLLISION_LENGTH = 44;
    private static final int COLLISION_TIME = 40;

    public PageAnvilCollisionCraft() {
        super(ModRecipeTypes.ANVIL_COLLISION_CRAFT.get());
    }

    @Override
    protected void drawRecipe(GuiGraphics graphics, AnvilCollisionCraftRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(recipeX, recipeY + 18, 50);
        pose.scale(0.8f, 0.8f, 1);
        // 铁砧
        Block anvil = recipe.anvil().getBlock();
        if (anvil == null) anvil = Blocks.ANVIL;
        renderAnvil(parent, graphics, anvil.defaultBlockState(), recipe.consume());
        // 撞击方块
        if (parent.ticksInBook % COLLISION_TIME <= 20 && recipe.hitBlock().getBlock() != null) {
            int scale = 12;
            BlockState hitBlockState = recipe.hitBlock().getBlock().defaultBlockState();
            if (hitBlockState.is(ModBlocks.GIANT_ANVIL)) {
                scale = 6;
                hitBlockState = ModBlocks.GIANT_ANVIL.getDefaultState()
                    .trySetValue(GiantAnvilBlock.HALF, Cube3x3PartHalf.MID_CENTER)
                    .trySetValue(GiantAnvilBlock.CUBE, GiantAnvilCube.CENTER);
            }
            RenderHelper.renderBlock(graphics, hitBlockState, COLLISION_LENGTH + 5, 0, 0, scale, RenderHelper.SINGLE_BLOCK);

        }
        PatchouliRenderHelper.renderArray(graphics, COLLISION_LENGTH + 16, 0);

        pose.scale(0.5f, 0.5f, 1);
        graphics.drawString(
            ((ScreenAccessor) parent).anvilcraft$getFont(),
            Component.translatable(
                "gui.anvilcraft.category.anvil_collision_craft_speed",
                recipe.speed()),
            -4,
            26,
            0xFF000000,
            false
        );
        pose.popPose();


        // 标题
        parent.drawCenteredStringNoShadow(
            graphics, getTitle(second).getVisualOrderText(),
            GuiBook.PAGE_WIDTH / 2, recipeY - 10,
            book.headerColor
        );

        // 产出物
        List<OutputItem> results = recipe.outputItems();
        if (!results.isEmpty()) {
            PatchouliRenderHelper.render2x2(graphics, recipeX + COLLISION_LENGTH + 12, recipeY);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2 && i * 2 + j < results.size(); j++) {
                    parent.renderItemStack(graphics, recipeX + COLLISION_LENGTH + 16 + j * 19, recipeY + 4 + i * 19,
                        mouseX, mouseY, results.get(i * 2 + j).getItemStack());
                }
            }
        }
        // 转化方块
        if (!recipe.transformBlocks().isEmpty()) {
            for (int i = 0; i < recipe.transformBlocks().size() && i < 2; i++) {
                int x = recipeX + COLLISION_LENGTH + 20 + i * 15;
                int y = recipeY + 7;
                pose.pushPose();
                pose.translate(x, y, 10);
                pose.scale(0.8f, 0.8f, 1);
                BlockTransform transformBlock = recipe.transformBlocks().get(i);
                // 被转化方块
                Block inputBlock = transformBlock.inputBlock().getBlock();
                if (inputBlock != null)
                    RenderHelper.renderBlock(graphics, inputBlock.defaultBlockState(), 0, 0, 0, 12, RenderHelper.SINGLE_BLOCK);
                // 转化出方块
                BlockState outputBlockState = transformBlock.outputBlock().getBlockState();
                RenderHelper.renderBlock(graphics, outputBlockState, 0, 30, 0, 12, RenderHelper.SINGLE_BLOCK);
                pose.popPose();
                // 箭头
                pose.pushPose();
                pose.translate(x + 2, y + 11, 0);
                pose.scale(0.4f, 0.8f, 1);
                pose.mulPose(Axis.ZP.rotationDegrees(90));
                PatchouliRenderHelper.renderArray(graphics, 0, 0);
                pose.popPose();
                // 最大转化量
                pose.pushPose();
                pose.translate(x + 4, y + 5, 100);
                pose.scale(0.5f, 0.5f, 1);
                graphics.drawString(
                    ((ScreenAccessor) parent).anvilcraft$getFont(),
                    Integer.toString(transformBlock.maxCount()),
                    0,
                    0,
                    0xFFEEEEEE,
                    false
                );
                graphics.drawString(
                    ((ScreenAccessor) parent).anvilcraft$getFont(),
                    Integer.toString(transformBlock.maxCount()),
                    0,
                    48,
                    0xFFEEEEEE,
                    false
                );
                pose.popPose();
            }
        }
    }

    private void renderAnvil(GuiBookEntry parent, GuiGraphics guiGraphics, BlockState anvil, Boolean consume) {
        int time = parent.ticksInBook % COLLISION_TIME;
        float anvilXOffset;
        if (time < 10)
            anvilXOffset = 0;
        else if (time < 20) {
            float returnTime = time - 10;
            anvilXOffset = (float) (COLLISION_LENGTH * Math.pow(returnTime / 10.0, 2));
        } else {
            float returnTime = time - 20;
            anvilXOffset = (float) (COLLISION_LENGTH * (Math.cos((returnTime) / 20 * Math.PI) + 1) / 2);
        }
        if (!(consume && time > 20)) {
            RenderHelper.renderBlock(
                guiGraphics, anvil,
                anvilXOffset,
                0,
                20,
                12,
                RenderHelper.SINGLE_BLOCK);
        }
    }

    @Override
    protected ItemStack getRecipeOutput(Level level, AnvilCollisionCraftRecipe recipe) {
        return recipe.getResultItem(level.registryAccess());
    }

    @Override
    protected int getRecipeHeight() {
        return 87;
    }
}
