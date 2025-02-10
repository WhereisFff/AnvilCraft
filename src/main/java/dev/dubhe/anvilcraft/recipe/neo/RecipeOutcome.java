package dev.dubhe.anvilcraft.recipe.neo;

import java.util.function.Consumer;

public interface RecipeOutcome extends Consumer<InWorldRecipeContext> {
    <T extends RecipeOutcome> RecipeOutcomeType<T> getType();
}
