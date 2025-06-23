package dev.dubhe.anvilcraft.recipe.neo.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SpawnItem;
import dev.dubhe.anvilcraft.recipe.neo.predicate.item.HasItemIngredient;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItemProcessRecipe extends InWorldRecipe {
    protected final Vec3 inputOffset;
    protected final List<ItemIngredientPredicate> ingredients;
    protected final Vec3 outputOffset;
    protected final List<ItemStack> results;

    public AbstractItemProcessRecipe(
        Vec3 inputOffset,
        List<ItemIngredientPredicate> ingredients,
        Vec3 outputOffset,
        List<ItemStack> results
    ) {
        super(
            AbstractItemProcessRecipe.getIcon(results),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            List.of(),
            AbstractItemProcessRecipe.getPredicates(inputOffset, ingredients),
            AbstractItemProcessRecipe.getOutcomes(outputOffset, results),
            0,
            false
        );
        this.inputOffset = inputOffset;
        this.ingredients = ingredients;
        this.outputOffset = outputOffset;
        this.results = results;
    }

    private static ItemStack getIcon(@NotNull List<ItemStack> results) {
        return results.isEmpty() ? ItemStack.EMPTY : results.getFirst();
    }

    private static @NotNull List<IRecipePredicate<?>> getPredicates(Vec3 inputOffset, @NotNull List<ItemIngredientPredicate> ingredients) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (ItemIngredientPredicate ingredient : ingredients) {
            predicates.add(new HasItemIngredient(inputOffset, new Vec3(0.125, 0.125, 0.125), ingredient));
        }
        return predicates;
    }

    private static @NotNull List<IRecipeOutcome<?>> getOutcomes(Vec3 outputOffset, @NotNull List<ItemStack> results) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (ItemStack stack : results) {
            outcomes.add(new SpawnItem(stack, outputOffset));
        }
        return outcomes;
    }
}
