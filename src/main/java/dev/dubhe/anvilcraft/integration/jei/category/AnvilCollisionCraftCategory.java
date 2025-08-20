package dev.dubhe.anvilcraft.integration.jei.category;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.integration.jei.AnvilCraftJeiPlugin;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRecipeUtil;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRenderHelper;
import dev.dubhe.anvilcraft.recipe.anvil.collision.AnvilCollisionCraftRecipe;
import dev.dubhe.anvilcraft.recipe.elements.OutputBlock;
import dev.dubhe.anvilcraft.recipe.elements.OutputItem;
import dev.dubhe.anvilcraft.util.RenderHelper;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AnvilCollisionCraftCategory implements IRecipeCategory<RecipeHolder<AnvilCollisionCraftRecipe>> {
    public static final int WIDTH = 162;
    public static final int HEIGHT = 64;

    private final IDrawable icon;
    private final ITickTimer timer;
    private final Component title;

    public AnvilCollisionCraftCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(ModBlocks.ACCELERATION_RING.asStack());
        this.timer = helper.createTickTimer(0, 60, false);
        this.title = Component.translatable("gui.anvilcraft.category.anvil_collision");
    }

    @Override
    public RecipeType<RecipeHolder<AnvilCollisionCraftRecipe>> getRecipeType() {
        return AnvilCraftJeiPlugin.ANVIL_COLLISION;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
        IRecipeLayoutBuilder builder,
        RecipeHolder<AnvilCollisionCraftRecipe> recipeHolder,
        IFocusGroup focuses) {
        AnvilCollisionCraftRecipe recipe = recipeHolder.value();

        if (recipe.anvil().getBlock() != null)
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemLike(recipe.anvil().getBlock().asItem());
        if (recipe.hitBlock().getBlock() != null)
            builder.addOutputSlot(0, 0).addItemLike(recipe.hitBlock().getBlock().asItem());

        for (int i = 0; i < recipe.outputItems().size(); i++) {
            OutputItem outputItem = recipe.outputItems().get(i);
            ItemStack itemStack = outputItem.getItemStack();
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT,
                1 + (i % 9) * 18, 1 + 44 + 18 * (i / 9)).addItemStack(itemStack);
        }

        for (int i = 0; i < recipe.transformBlocks().size(); i++) {
            OutputBlock outputBlock = recipe.transformBlocks().get(i).outputBlock();
            Item item = outputBlock.getBlockState().getBlock().asItem();
            ItemStack itemStack = new ItemStack(item);
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT,
                1 + (i % 9) * 18, 1 + 44 + 18 * (i / 9)).addItemStack(itemStack);
        }
    }

    @Override
    public void draw(
        RecipeHolder<AnvilCollisionCraftRecipe> recipeHolder,
        IRecipeSlotsView recipeSlotsView,
        GuiGraphics guiGraphics,
        double mouseX,
        double mouseY) {
        AnvilCollisionCraftRecipe recipe = recipeHolder.value();
        float anvilXOffset = JeiRenderHelper.getAnvilAnimationOffset(timer);

        Block anvil = recipe.anvil().getBlock();
        if (anvil == null) anvil = Blocks.ANVIL;
        System.out.println(anvil);
        RenderHelper.renderBlock(
            guiGraphics,
            anvil.defaultBlockState(),
            50 + anvilXOffset,
            12,
            20,
            12,
            RenderHelper.SINGLE_BLOCK
        );
    }

    @Override
    public void getTooltip(
        ITooltipBuilder tooltip,
        RecipeHolder<AnvilCollisionCraftRecipe> recipe,
        IRecipeSlotsView recipeSlotsView,
        double mouseX,
        double mouseY) {

    }

    public static void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
            AnvilCraftJeiPlugin.ANVIL_COLLISION,
            JeiRecipeUtil.getRecipeHoldersFromType(ModRecipeTypes.ANVIL_COLLISION_CRAFT.get())
        );
    }

    public static void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.ACCELERATION_RING.asStack(), AnvilCraftJeiPlugin.ANVIL_COLLISION);
        registration.addRecipeCatalyst(ModBlocks.DEFLECTION_RING.asStack(), AnvilCraftJeiPlugin.ANVIL_COLLISION);
        registration.addRecipeCatalyst(new ItemStack(Items.ANVIL), AnvilCraftJeiPlugin.ANVIL_COLLISION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ROYAL_ANVIL), AnvilCraftJeiPlugin.ANVIL_COLLISION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.EMBER_ANVIL), AnvilCraftJeiPlugin.ANVIL_COLLISION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GIANT_ANVIL), AnvilCraftJeiPlugin.ANVIL_COLLISION);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TRANSCENDENCE_ANVIL), AnvilCraftJeiPlugin.ANVIL_COLLISION);
    }
}
