package dev.dubhe.anvilcraft.recipe.neo;

import dev.dubhe.anvilcraft.init.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IRecipeOutcome<O extends IRecipeOutcome<O>> extends Consumer<InWorldRecipeContext>, IPrioritized {
    Type<O> getType();

    default double getChance() {
        return 1.0;
    }

    default void acceptWithChance(@NotNull InWorldRecipeContext context) {
        if (context.getLevel().getRandom().nextDouble() > this.getChance()) return;
        this.accept(context);
    }

    interface Type<O extends IRecipeOutcome<O>> extends ISerializer<O> {
        default ResourceLocation getId() {
            return ModRegistries.OUTCOME_TYPE_REGISTRY.getKey(this);
        }
    }
}
