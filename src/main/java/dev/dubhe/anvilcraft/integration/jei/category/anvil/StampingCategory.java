package dev.dubhe.anvilcraft.integration.jei.category.anvil;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.integration.jei.AnvilCraftJeiPlugin;
import dev.dubhe.anvilcraft.integration.jei.drawable.DrawableBlockStateIcon;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRecipeUtil;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRenderHelper;
import dev.dubhe.anvilcraft.integration.jei.util.JeiSlotUtil;
import dev.dubhe.anvilcraft.recipe.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.StampingRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.StampingUniqueItemsRecipe;
import dev.dubhe.anvilcraft.util.RenderHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StampingCategory extends AbstractItemProgressCategory<StampingRecipe> {
    public StampingCategory(IGuiHelper helper) {
        super(
            helper,
            new DrawableBlockStateIcon(Blocks.ANVIL.defaultBlockState(), ModBlocks.STAMPING_PLATFORM.getDefaultState()),
            Component.translatable("gui.anvilcraft.category.stamping")
        );
    }

    @Override
    public RecipeType<RecipeHolder<StampingRecipe>> getRecipeType() {
        return AnvilCraftJeiPlugin.STAMPING;
    }

    @Override
    public void draw(
        RecipeHolder<StampingRecipe> recipeHolder,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics guiGraphics,
        double mouseX,
        double mouseY) {
        StampingRecipe recipe = recipeHolder.value();
        float anvilYOffset = JeiRenderHelper.getAnvilAnimationOffset(timer);
        RenderHelper.renderBlock(
            guiGraphics,
            Blocks.ANVIL.defaultBlockState(),
            81,
            22 + anvilYOffset,
            20,
            12,
            RenderHelper.SINGLE_BLOCK);
        RenderHelper.renderBlock(
            guiGraphics, ModBlocks.STAMPING_PLATFORM.getDefaultState(), 81, 40, 0, 12, RenderHelper.SINGLE_BLOCK);

        arrowIn.draw(guiGraphics, 54, 32);
        arrowOut.draw(guiGraphics, 92, 31);

        JeiSlotUtil.drawInputSlots(guiGraphics, slot, recipe.mergedIngredients.size());

        List<ChanceItemStack> results = this.getResults(recipe);
        if (recipe instanceof StampingUniqueItemsRecipe) {
            ChanceItemStack result = results.getFirst();
            JeiSlotUtil.drawOutputSlots(guiGraphics, slot, result.getStack().getCount());
        } else {
            JeiSlotUtil.drawOutputSlots(guiGraphics, slot, results.size());
        }
    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
            AnvilCraftJeiPlugin.STAMPING,
            JeiRecipeUtil.getRecipeHoldersFromType(ModRecipeTypes.STAMPING_TYPE.get()));
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.ANVIL), AnvilCraftJeiPlugin.STAMPING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROYAL_ANVIL), AnvilCraftJeiPlugin.STAMPING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EMBER_ANVIL), AnvilCraftJeiPlugin.STAMPING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GIANT_ANVIL), AnvilCraftJeiPlugin.STAMPING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPECTRAL_ANVIL), AnvilCraftJeiPlugin.STAMPING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.STAMPING_PLATFORM), AnvilCraftJeiPlugin.STAMPING);
    }
}
