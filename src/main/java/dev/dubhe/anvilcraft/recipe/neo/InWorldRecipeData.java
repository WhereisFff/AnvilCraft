package dev.dubhe.anvilcraft.recipe.neo;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record InWorldRecipeData<T>(ResourceLocation location, T defaultValue) {
    public static <T> @NotNull InWorldRecipeData<T> of(ResourceLocation location, T defaultValue) {
        return new InWorldRecipeData<>(location, defaultValue);
    }

    public static <T> @NotNull InWorldRecipeData<T> of(ResourceLocation location) {
        return new InWorldRecipeData<>(location, null);
    }
}
