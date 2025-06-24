package dev.dubhe.anvilcraft.recipe.neo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShapelessMatcher {
    public static boolean incompatible(@NotNull List<IRecipePredicate<?>> predicates, @NotNull InWorldRecipeContext ctx) {
        if (predicates.isEmpty()) return true;
        for (IRecipePredicate<?> predicate : predicates) {
            if (!predicate.test(ctx)) continue;
            List<IRecipePredicate<?>> next = new ArrayList<>();
            for (IRecipePredicate<?> predicate1 : predicates) {
                if (predicate1 == predicate) continue;
                next.add(predicate1);
            }
            ctx.push(predicate);
            if (next.isEmpty()) return true;
            boolean flag = incompatible(next, ctx);
            if (flag) return true;
            ctx.pop(predicate);
        }
        return false;
    }

    public static boolean compatible(@NotNull List<IRecipePredicate<?>> predicates, @NotNull InWorldRecipeContext ctx) {
        for (IRecipePredicate<?> predicate : predicates) {
            if (!predicate.test(ctx)) return false;
            ctx.push(predicate);
        }
        return true;
    }
}
