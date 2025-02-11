package dev.dubhe.anvilcraft.recipe.neo.predicate;

import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicateType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AllOf(List<RecipePredicate<?>> predicates) implements RecipePredicate<AllOf> {
    @Override
    public @NotNull RecipePredicateType<AllOf> getType() {
        return null;
    }

    @Override
    public boolean test(InWorldRecipeContext inWorldRecipeContext) {
        for (RecipePredicate<?> predicate : this.predicates) {
            if (!predicate.test(inWorldRecipeContext)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(InWorldRecipeContext inWorldRecipeContext) {
        for (RecipePredicate<?> predicate : this.predicates) {
            predicate.accept(inWorldRecipeContext);
        }
    }
}
