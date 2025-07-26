package dev.dubhe.anvilcraft.integration.patchouli.page;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.mixin.accessor.ScreenAccessor;
import dev.dubhe.anvilcraft.recipe.anvil.TimeWarpRecipe;
import dev.dubhe.anvilcraft.util.CauldronUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PageTimeWarp extends PageAnvilItemProcess<TimeWarpRecipe> {
    public PageTimeWarp() {
        super(
            ModRecipeTypes.TIME_WARP_TYPE,
            TimeWarpRecipe::getMergedIngredients,
            TimeWarpRecipe::getResults,
            PageTimeWarp::getCauldron,
            recipe -> ModBlocks.CORRUPTED_BEACON.getDefaultState());
    }

    @Override
    protected void drawExtra(
        GuiGraphics graphics, TimeWarpRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second
    ) {
        List<Object2IntMap.Entry<Ingredient>> inputs = recipe.getMergedIngredients();
        if (inputs.size() <= 4) {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            if (recipe.isConsumeFluid()) {
                pose.translate(recipeX - 6, recipeY + 52, 100);
                pose.scale(0.5f, 0.5f, 1);
                graphics.drawString(
                    ((ScreenAccessor) parent).anvilcraft$getFont(),
                    Component.translatable(
                        "gui.anvilcraft.category.time_warp.consume_fluid", recipe.getCauldron().getName()),
                    0,
                    0,
                    0xFF000000,
                    false
                );
            } else if (recipe.isProduceFluid()) {
                pose.translate(recipeX - 6, recipeY + 52, 100);
                pose.scale(0.5f, 0.5f, 1);
                graphics.drawString(
                    ((ScreenAccessor) parent).anvilcraft$getFont(),
                    Component.translatable(
                        "gui.anvilcraft.category.time_warp.produce_fluid", recipe.getCauldron().getName()),
                    0,
                    0,
                    0xFF000000,
                    false
                );
            }
            pose.popPose();
        }
    }

    static BlockState getCauldron(TimeWarpRecipe recipe) {
        if (recipe.isProduceFluid()) {
            return Blocks.CAULDRON.defaultBlockState();
        } else {
            return CauldronUtil.fullState(recipe.getCauldron());
        }
    }
}
