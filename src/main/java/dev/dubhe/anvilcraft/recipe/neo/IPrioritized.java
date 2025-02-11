package dev.dubhe.anvilcraft.recipe.neo;

public interface IPrioritized {
    default int getPriority() {
        return 1;
    }
}
