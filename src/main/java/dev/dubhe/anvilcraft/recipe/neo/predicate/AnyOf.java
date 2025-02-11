package dev.dubhe.anvilcraft.recipe.neo.predicate;

import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicateType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AnyOf(List<RecipePredicate<?>> predicates) implements RecipePredicate<AnyOf> {
    @Override
    public @NotNull RecipePredicateType<AnyOf> getType() {
        return null;
    }

    @Override
    public boolean test(InWorldRecipeContext inWorldRecipeContext) {
        for (RecipePredicate<?> predicate : this.predicates) {
            if (predicate.test(inWorldRecipeContext)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(InWorldRecipeContext inWorldRecipeContext) {
        for (RecipePredicate<?> predicate : this.predicates) {
            if (predicate.test(inWorldRecipeContext)) predicate.accept(inWorldRecipeContext);
        }
    }
}
