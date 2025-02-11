package dev.dubhe.anvilcraft.recipe.neo;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface RecipePredicate<T extends RecipePredicate<T>> extends Predicate<InWorldRecipeContext>, Consumer<InWorldRecipeContext>, IPrioritized {
    @NotNull RecipePredicateType<T> getType();

    @Override
    default void accept(InWorldRecipeContext inWorldRecipeContext) {
    }
}
