package dev.dubhe.anvilcraft.inventory;

import com.google.common.collect.ImmutableList;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.reicpe.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.frost.DeformationRecipe;
import dev.dubhe.anvilcraft.recipe.frost.FrostSmithingRecipeInput;
import dev.dubhe.anvilcraft.recipe.frost.IFrostSmithingRecipe;
import dev.dubhe.anvilcraft.recipe.frost.PermutationRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public class FrostSmithingMenu extends ItemCombinerMenu {
    private final Level level;

    @Nullable
    private RecipeHolder<? extends IFrostSmithingRecipe> selectedRecipe;
    private int selected = -1;
    @Unmodifiable
    @Nullable
    private List<Item> results = null;
    @Nullable
    private ItemStack[] resultsCache = null;

    private final List<RecipeHolder<? extends IFrostSmithingRecipe>> recipes;

    public FrostSmithingMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public FrostSmithingMenu(MenuType<FrostSmithingMenu> type, int containerId, Inventory playerInventory) {
        this(type, containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public FrostSmithingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        this(ModMenuTypes.FROST_SMITHING.get(), containerId, playerInventory, access);
    }

    /**
     * 浮霜锻造台菜单
     *
     * @param type            类型
     * @param containerId     容器id
     * @param playerInventory 背包
     * @param access          检查
     */
    public FrostSmithingMenu(
        MenuType<FrostSmithingMenu> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
        this.level = playerInventory.player.level();
        this.recipes = ImmutableList.<RecipeHolder<? extends IFrostSmithingRecipe>>builder()
            .addAll(this.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.PERMUTATION_TYPE.get()))
            .addAll(this.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DEFORMATION_TYPE.get()))
            .build();
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
            .withSlot(
                0,
                8,
                48,
                stack -> this.recipes.stream().anyMatch(recipe -> recipe.value().isTemplate(stack))
            ).withSlot(
                1,
                44,
                48,
                stack -> this.recipes.stream().anyMatch(recipe -> recipe.value().isMaterial(stack))
            ).withSlot(
                2,
                62,
                48,
                stack -> this.recipes.stream().anyMatch(recipe -> recipe.value().isInput(stack))
            ).withResultSlot(3, 106, 48)
            .build();
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModBlocks.FROST_SMITHING_TABLE.get());
    }

    private FrostSmithingRecipeInput createRecipeInput() {
        return new FrostSmithingRecipeInput(
            this.inputSlots.getItem(0),
            this.inputSlots.getItem(1),
            this.inputSlots.getItem(2)
        );
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        return this.selectedRecipe != null
               && this.selectedRecipe.value().matches(this.createRecipeInput(), this.level);
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player.level(), player, stack.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((level, blockPos) -> level.levelEvent(1044, blockPos, 0));
    }

    private @Unmodifiable List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private void shrinkStackInSlot(int index) {
        ItemStack stack = this.inputSlots.getItem(index);
        if (stack.isEmpty()) return;
        stack.shrink(1);
        this.inputSlots.setItem(index, stack);
    }

    @Override
    public void createResult() {
        FrostSmithingRecipeInput input = this.createRecipeInput();

        List<RecipeHolder<PermutationRecipe>> permuts = this.level.getRecipeManager()
            .getRecipesFor(ModRecipeTypes.PERMUTATION_TYPE.get(), input, this.level);
        if (!permuts.isEmpty()) {
            RecipeHolder<PermutationRecipe> holder = permuts.getFirst();
            ItemStack stack = holder.value().assemble(input, this.level.registryAccess());
            if (stack.isItemEnabled(this.level.enabledFeatures())) {
                this.selectedRecipe = holder;
                this.resetDeformRecipe();
                this.resultSlots.setRecipeUsed(holder);
                this.resultSlots.setItem(0, stack);
            }
            return;
        }

        List<RecipeHolder<DeformationRecipe>> deforms = this.level.getRecipeManager()
            .getRecipesFor(ModRecipeTypes.DEFORMATION_TYPE.get(), input, this.level);
        if (!deforms.isEmpty()) {
            RecipeHolder<DeformationRecipe> holder = deforms.getFirst();
            this.selectedRecipe = holder;
            this.initDeformRecipe(holder);
            this.resultSlots.setRecipeUsed(holder);
            this.resultSlots.setItem(0, this.createDeformResult());
            return;
        }

        this.selectedRecipe = null;
        this.resetDeformRecipe();
        this.resultSlots.setItem(0, ItemStack.EMPTY);
    }

    private void resetDeformRecipe() {
        this.selected = -1;
        this.results = null;
        this.resultsCache = null;
    }

    private void initDeformRecipe(RecipeHolder<DeformationRecipe> holder) {
        this.selected = 0;
        this.results = holder.value().getResults(this.getSlot(2).getItem());
        this.resultsCache = new ItemStack[this.results.size()];
    }

    private ItemStack createDeformResult() {
        if (this.selected == -1 || this.results == null || this.resultsCache == null) return ItemStack.EMPTY;
        if (this.resultsCache[this.selected] != null) return this.resultsCache[this.selected];
        return this.resultsCache[this.selected] = this.getSlot(0).getItem()
            .transmuteCopy(this.results.get(this.selected));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public int getSlotToQuickMoveTo(ItemStack stack) {
        return this.recipes.stream()
            .map(smithingRecipe -> FrostSmithingMenu.findSlotMatchingIngredient(smithingRecipe.value(), stack))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.of(0))
            .get();
    }

    private static Optional<Integer> findSlotMatchingIngredient(IFrostSmithingRecipe recipe, ItemStack stack) {
        if (recipe.isTemplate(stack)) return Optional.of(0);
        if (recipe.isMaterial(stack)) return Optional.of(1);
        if (recipe.isInput(stack)) return Optional.of(2);
        return Optional.empty();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack stack) {
        return this.recipes.stream()
            .map(smithingRecipe -> FrostSmithingMenu.findSlotMatchingIngredient(smithingRecipe.value(), stack))
            .anyMatch(Optional::isPresent);
    }

    public boolean canCreateResult() {
        ItemStack template = this.getSlot(0).getItem();

        if (template.is(ModItems.DEFORMATION_TEMPLATE_ITEM)) {
            if (this.getSlot(1).getItem().isEmpty()) return false;
        }

        return this.getSlot(0).hasItem() && this.getSlot(2).hasItem();
    }

    public void sync(int selected) {
        this.selected = selected;
    }

    public void sync(List<Item> results) {
        this.results = results;
        this.resultsCache = new ItemStack[results.size()];
    }
}
