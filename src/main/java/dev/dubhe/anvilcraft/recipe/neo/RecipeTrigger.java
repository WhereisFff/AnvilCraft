package dev.dubhe.anvilcraft.recipe.neo;

import dev.dubhe.anvilcraft.init.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface RecipeTrigger {
    default ResourceLocation getId() {
        return ModRegistries.BuiltIn.RECIPE_TRIGGER.getKey(this);
    }

    static @NotNull RecipeTrigger of() {
        return new RecipeTrigger() {
        };
    }
}
