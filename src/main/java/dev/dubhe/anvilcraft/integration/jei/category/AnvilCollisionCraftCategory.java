package dev.dubhe.anvilcraft.integration.jei.category;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.integration.jei.AnvilCraftJeiPlugin;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRecipeUtil;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRenderHelper;
import dev.dubhe.anvilcraft.recipe.anvil.collision.AnvilCollisionCraftRecipe;
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
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
        this.timer = helper.createTickTimer(30, 30, false);
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

        // 将被撞击的方块加入addInvisibleIngredients中
        if (recipe.hitBlock().getBlock() != null)
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                .addItemLike(recipe.hitBlock().getBlock().asItem());

        // 如果有输出物品则添加到输出且加上tooltip显示概率
        for (int i = 0; i < recipe.outputItems().size(); i++) {
            OutputItem outputItem = recipe.outputItems().get(i);
            ItemStack itemStack = outputItem.getItemStack();
            if (itemStack != null) {
                IRecipeSlotBuilder slot = builder.addOutputSlot
                    (110 + 18 * i, 20)
                    .addItemStack(itemStack);
                slot.addRichTooltipCallback(
                    (recipeSlotView, tooltip) ->
                        tooltip.add
                            (Component.translatable
                                ("gui.anvilcraft.category.chance", outputItem.getChance() * 100).withStyle(ChatFormatting.GRAY)
                            )
                );
            }
        }

        // 如果有转换或被转换的方块 则加入addInvisibleIngredients中
        for (int i = 0; i < recipe.transformBlocks().size(); i++) {
            Block inputBlock = recipe.transformBlocks()
                .get(i).inputBlock().getBlock();
            Block outputBlock = recipe.transformBlocks()
                .get(i).outputBlock().getBlockState().getBlock();

            if (inputBlock != null) {
                Item inputItem = inputBlock.asItem();
                builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemLike(inputItem);
            }

            Item outputItem = outputBlock.asItem();
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemLike(outputItem);
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
        float anvilXOffset = JeiRenderHelper.getAnvilAnimationOffset(timer) * 4;

        Block anvil = recipe.anvil().getBlock();
        if (anvil == null) anvil = Blocks.ANVIL;
        RenderHelper.renderBlock(
            guiGraphics,
            anvil.defaultBlockState(),
            70 + anvilXOffset,
            25,
            20,
            12,
            RenderHelper.SINGLE_BLOCK
        );

        if (recipe.hitBlock().getBlock() != null) {
            BlockState blockState = recipe.hitBlock().getBlock().defaultBlockState();
            RenderHelper.renderBlock(
                guiGraphics,
                blockState,
                20,
                25,
                20,
                12,
                RenderHelper.SINGLE_BLOCK
            );
        }

        if (!recipe.transformBlocks().isEmpty()) {

        }
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
