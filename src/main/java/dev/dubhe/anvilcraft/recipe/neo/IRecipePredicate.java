package dev.dubhe.anvilcraft.recipe.neo;

import dev.dubhe.anvilcraft.init.ModRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IRecipePredicate<P extends IRecipePredicate<P>> extends Predicate<InWorldRecipeContext>, Consumer<InWorldRecipeContext>, IPrioritized {
    @Override
    default void accept(InWorldRecipeContext context) {
    }

    default void snapshot(InWorldRecipeContext context) {
    }

    default void rollback(InWorldRecipeContext context) {
    }

    Type<P> getType();

    interface Type<P extends IRecipePredicate<P>> extends ISerializer<P> {
        default ResourceLocation getId() {
            return ModRegistries.PREDICATE_TYPE_REGISTRY.getKey(this);
        }

        default boolean conflict() {
            return false;
        }
    }
}
