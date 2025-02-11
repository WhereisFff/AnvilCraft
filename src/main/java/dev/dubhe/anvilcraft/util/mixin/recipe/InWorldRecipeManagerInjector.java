package dev.dubhe.anvilcraft.util.mixin.recipe;

import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeManager;

public interface InWorldRecipeManagerInjector {
    default InWorldRecipeManager anvilcraft$getInWorldRecipeManager() {
        throw new AssertionError();
    }
}
