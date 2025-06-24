package dev.dubhe.anvilcraft.recipe.neo.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SpawnItem;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.neo.predicate.item.HasItemIngredient;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItemProcessRecipe<T extends InWorldRecipe> extends InWorldRecipe {
    protected final Vec3 inputOffset;
    protected final List<ItemIngredientPredicate> ingredients;
    protected final Vec3 outputOffset;
    protected final List<ItemStack> results;
    protected final List<Double> chances;
    protected final Vec3 blockOffset;
    protected final BlockStatePredicate block;

    public AbstractItemProcessRecipe(
        Vec3 inputOffset,
        List<ItemIngredientPredicate> ingredients,
        Vec3 outputOffset,
        List<ItemStack> results,
        List<Double> chances,
        Vec3 blockOffset,
        BlockStatePredicate block
    ) {
        super(
            AbstractItemProcessRecipe.getIcon(results),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            List.of(),
            AbstractItemProcessRecipe.getPredicates(inputOffset, ingredients, blockOffset, block),
            AbstractItemProcessRecipe.getOutcomes(outputOffset, results, chances),
            0,
            false
        );
        this.inputOffset = inputOffset;
        this.ingredients = ingredients;
        this.outputOffset = outputOffset;
        this.results = results;
        this.chances = chances;
        this.blockOffset = blockOffset;
        this.block = block;
    }

    private static ItemStack getIcon(@NotNull List<ItemStack> results) {
        return results.isEmpty() ? Items.ANVIL.getDefaultInstance() : results.getFirst();
    }

    private static @NotNull List<IRecipePredicate<?>> getPredicates(
        Vec3 inputOffset,
        @NotNull List<ItemIngredientPredicate> ingredients,
        Vec3 blockOffset,
        BlockStatePredicate block
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (ItemIngredientPredicate ingredient : ingredients) {
            predicates.add(new HasItemIngredient(inputOffset, new Vec3(1, 0.5, 1), ingredient));
        }
        predicates.add(new HasBlock(blockOffset, block));
        return predicates;
    }

    private static @NotNull List<IRecipeOutcome<?>> getOutcomes(
        Vec3 outputOffset,
        @NotNull List<ItemStack> results,
        List<Double> chances
    ) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ItemStack stack = results.get(i);
            double chance = chances.isEmpty() ? 1.0 : chances.size() > i ? chances.get(i) : 1.0;
            outcomes.add(new SpawnItem(stack, outputOffset, chance));
        }
        return outcomes;
    }

    @Override
    public abstract @NotNull RecipeSerializer<T> getSerializer();

    @Override
    public abstract @NotNull RecipeType<T> getType();
}
