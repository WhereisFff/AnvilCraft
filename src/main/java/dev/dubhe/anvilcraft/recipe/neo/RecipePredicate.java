package dev.dubhe.anvilcraft.recipe.neo;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface RecipePredicate extends Predicate<InWorldRecipeContext>, Consumer<InWorldRecipeContext> {
    <T extends RecipePredicate> RecipePredicateType<T> getType();
}
