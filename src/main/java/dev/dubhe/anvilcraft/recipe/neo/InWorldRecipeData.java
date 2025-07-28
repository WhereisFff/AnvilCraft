package dev.dubhe.anvilcraft.recipe.neo;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public record InWorldRecipeData<T>(
    ResourceLocation location,
    BiFunction<InWorldRecipeContext, InWorldRecipeData<T>, T> supplier
) {
    public static <T> @NotNull InWorldRecipeData<T> of(ResourceLocation location, T defaultValue) {
        return new InWorldRecipeData<>(location, (ctx, self) -> defaultValue);
    }

    public static <T> @NotNull InWorldRecipeData<T> of(
        ResourceLocation location,
        BiFunction<InWorldRecipeContext, InWorldRecipeData<T>, T> supplier
    ) {
        return new InWorldRecipeData<>(location, supplier);
    }

    public static <T> @NotNull InWorldRecipeData<T> of(ResourceLocation location) {
        return new InWorldRecipeData<>(location, null);
    }
}
