package dev.dubhe.anvilcraft.inventory;

import com.google.common.collect.ImmutableList;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.reicpe.ModRecipeTypes;
import dev.dubhe.anvilcraft.network.multiple.FrostSmithingPackets;
import dev.dubhe.anvilcraft.recipe.frost.DeformationRecipe;
import dev.dubhe.anvilcraft.recipe.frost.FrostSmithingRecipeInput;
import dev.dubhe.anvilcraft.recipe.frost.IFrostSmithingRecipe;
import dev.dubhe.anvilcraft.recipe.frost.PermutationRecipe;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.world.Container;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public class FrostSmithingMenu extends ItemCombinerMenu {
    private final Level level;

    @Nullable
    private RecipeHolder<? extends IFrostSmithingRecipe> selectedRecipe;
    public int selected = -1;
    @Unmodifiable
    @Nullable
    public List<Item> results = null;

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
                stack -> this.inputSlots.getItem(0).is(ModItems.PERMUTATION_TEMPLATE_ITEM)
                         && this.recipes.stream().anyMatch(recipe -> recipe.value().isMaterial(stack))
            ).withSlot(
                2,
                62,
                48,
                stack -> !this.inputSlots.getItem(0).isEmpty()
                         && (!this.inputSlots.getItem(0).is(ModItems.PERMUTATION_TEMPLATE_ITEM) || !this.inputSlots.getItem(1).isEmpty())
                         && this.recipes.stream().anyMatch(recipe -> recipe.value().isInput(stack))
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
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        if (inventory == this.inputSlots) {
            if (this.inputSlots.getItem(0).isEmpty()) {
                for (int i = 1; i < 3; i++) {
                    ItemStack stack = this.inputSlots.getItem(i);
                    if (!stack.isEmpty()) {
                        this.inputSlots.removeItemNoUpdate(i);
                        this.moveItemStackTo(stack, 4, 40, false);
                    }
                }
            } else if (this.inputSlots.getItem(0).is(ModItems.PERMUTATION_TEMPLATE_ITEM) && this.inputSlots.getItem(1).isEmpty()) {
                ItemStack stack = this.inputSlots.getItem(2);
                if (!stack.isEmpty()) {
                    this.inputSlots.removeItemNoUpdate(2);
                    this.moveItemStackTo(stack, 4, 40, false);
                }
            }
        }
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player.level(), player, stack.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
        this.shrinkStackInSlot(2);
        this.shrinkStackInSlot(1);
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
            this.selected = 0;
            this.results = holder.value().getResults(this.getSlot(2).getItem());
            if (!this.level.isClientSide) {
                PacketDistributor.sendToPlayer(Util.cast(this.player), new FrostSmithingPackets.OriginalSync(this.selected, this.results));
            }
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
        if (!this.level.isClientSide) {
            PacketDistributor.sendToPlayer(Util.cast(this.player), new FrostSmithingPackets.OriginalSync(this.selected, List.of()));
        }
    }

    private ItemStack createDeformResult() {
        if (this.selected == -1 || this.results == null) return ItemStack.EMPTY;
        return this.getSlot(2).getItem().transmuteCopy(this.results.get(this.selected));
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

    public void sync(int selected, List<Item> results) {
        this.selected = selected;
        this.results = results.isEmpty() ? null : results;
    }

    public void turn(boolean left) {
        if (this.selected == -1 || this.results == null) return;
        this.selected = (this.selected + (left ? -1 : 1)) % this.results.size();
        if (this.selected < 0) this.selected += this.results.size();
        this.resultSlots.setItem(0, this.createDeformResult());
    }
}
