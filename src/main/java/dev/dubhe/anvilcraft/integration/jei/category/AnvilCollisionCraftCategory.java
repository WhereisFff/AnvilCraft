package dev.dubhe.anvilcraft.integration.jei.category;

import dev.dubhe.anvilcraft.block.GiantAnvilBlock;
import dev.dubhe.anvilcraft.block.state.Cube3x3PartHalf;
import dev.dubhe.anvilcraft.block.state.GiantAnvilCube;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.integration.jei.AnvilCraftJeiPlugin;
import dev.dubhe.anvilcraft.integration.jei.util.BlockTagUtil;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRecipeUtil;
import dev.dubhe.anvilcraft.integration.jei.util.JeiRenderHelper;
import dev.dubhe.anvilcraft.integration.jei.util.TextureConstants;
import dev.dubhe.anvilcraft.recipe.anvil.collision.AnvilCollisionCraftRecipe;
import dev.dubhe.anvilcraft.recipe.elements.InputBlock;
import dev.dubhe.anvilcraft.recipe.elements.OutputItem;
import dev.dubhe.anvilcraft.util.BlockStateUtil;
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
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AnvilCollisionCraftCategory implements IRecipeCategory<RecipeHolder<AnvilCollisionCraftRecipe>> {
    public static final int WIDTH = 162;
    public static final int HEIGHT = 64;

    private final IDrawable progress;
    protected final IDrawable arrowOut;

    private final IDrawable icon;
    private final ITickTimer timer;
    private final Component title;

    public AnvilCollisionCraftCategory(IGuiHelper helper) {
        progress = helper.drawableBuilder(TextureConstants.PROGRESS, 0, 0, 24, 16)
            .setTextureSize(24, 16)
            .build();
        this.arrowOut = helper.createDrawable(TextureConstants.ANVIL_CRAFT_SPRITES, 0, 31, 16, 8);

        this.icon = helper.createDrawableItemStack(ModBlocks.ACCELERATION_RING.asStack());
        this.timer = helper.createTickTimer(30, 100, true);
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
                IRecipeSlotBuilder slot = builder.addOutputSlot(107 + 18 * i, 21).addItemStack(itemStack);
                slot.addRichTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(Component.translatable("gui.anvilcraft.category.chance", outputItem.getChance() * 100).withStyle(ChatFormatting.GRAY)));
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

        // 将此配方需要的铁砧加入输入槽
        if (recipe.anvil().getTag() != null) {
            AtomicInteger slotId = new AtomicInteger(0);
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addIngredients(BlockTagUtil.toIngredient(recipe.anvil().getTag()));
            RegistryUtil.getRegistry(Registries.BLOCK)
                .getTag(recipe.anvil().getTag())
                .stream()
                .flatMap(HolderSet.ListBacked::stream)
                .map(h -> h.value().asItem().getDefaultInstance())
                .forEach(stack -> {
                    int id = slotId.getAndAdd(1);
                    builder.addSlot(RecipeIngredientRole.RENDER_ONLY,
                            (id % 9) * 18 + 66,
                            40)
                        .addItemStack(stack);
                });
        } else {
            builder.addInputSlot(10, 20).addItemLike(recipe.anvil().getBlock().asItem());
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

        // 铁砧左右移动模拟碰撞
        Block anvil = recipe.anvil().getBlock();
        if (anvil == null) anvil = Blocks.ANVIL;
        RenderHelper.renderBlock(
            guiGraphics,
            anvil.defaultBlockState(),
            45 - anvilXOffset,
            25,
            20,
            12,
            RenderHelper.SINGLE_BLOCK
        );

        if (recipe.hitBlock().getBlock() != null) {
            BlockState blockState = recipe.hitBlock().getBlock().defaultBlockState();
            // 特判: 如果是大铁砧 则将BlockState改为cube=center,half=mid_center 并修改scale使其大小合理
            // 建议下次写类似大铁砧的方块的时候 把registerDefaultState注册成有材质的中心位置
            // 当然也可以不RenderHelper.renderBlock 直接加进setRecipe的输入输出槽当物品看
            int scale = 12;
            if (blockState.is(ModBlocks.GIANT_ANVIL)) {
                scale = 6;
                blockState = ModBlocks.GIANT_ANVIL.getDefaultState()
                    .trySetValue(GiantAnvilBlock.HALF, Cube3x3PartHalf.MID_CENTER)
                    .trySetValue(GiantAnvilBlock.CUBE, GiantAnvilCube.CENTER);
            }

            RenderHelper.renderBlock(
                guiGraphics,
                blockState,
                70,
                25,
                20,
                scale,
                RenderHelper.SINGLE_BLOCK
            );
        }

        if (!recipe.transformBlocks().isEmpty() && recipe.outputItems().isEmpty()) {
            // 渲染转换与被转换的方块
            for (int i = 0; i < recipe.transformBlocks().size(); i++) {
                Block inputBlock = recipe.transformBlocks()
                    .get(i).inputBlock().getBlock();
                Block outputBlock = recipe.transformBlocks()
                    .get(i).outputBlock().getBlockState().getBlock();
                if (inputBlock != null) {
                    BlockState inputBlockState = inputBlock.defaultBlockState();
                    RenderHelper.renderBlock(
                        guiGraphics,
                        inputBlockState,
                        95,
                        25,
                        10,
                        14,
                        RenderHelper.SINGLE_BLOCK
                    );

                    progress.draw(guiGraphics, 107, 21);

                    BlockState outputBlockState = outputBlock.defaultBlockState();
                    RenderHelper.renderBlock(
                        guiGraphics,
                        outputBlockState,
                        143,
                        25,
                        10,
                        14,
                        RenderHelper.SINGLE_BLOCK
                    );
                }
            }
        } else {
            // 如果没有转换与被转换的方块 则添加箭头并指向输出物品
            progress.draw(guiGraphics, 82, 21);
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
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TRANSCENDENCE_ANVIL), AnvilCraftJeiPlugin.ANVIL_COLLISION);
    }
}
