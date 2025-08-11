package dev.dubhe.anvilcraft.util.injection;

import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeManager;

public interface IRecipeManager {
    default void anc$setInWorldRecipeManager(InWorldRecipeManager manager) {
        throw new AssertionError();
    }

    default InWorldRecipeManager anc$getInWorldRecipeManager() {
        throw new AssertionError();
    }
}
