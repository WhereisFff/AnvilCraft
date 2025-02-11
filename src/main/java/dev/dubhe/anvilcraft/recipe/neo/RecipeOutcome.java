package dev.dubhe.anvilcraft.recipe.neo;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface RecipeOutcome<T extends RecipeOutcome<T>> extends Consumer<InWorldRecipeContext>, IPrioritized {
    @NotNull RecipeOutcomeType<T> getType();
}
