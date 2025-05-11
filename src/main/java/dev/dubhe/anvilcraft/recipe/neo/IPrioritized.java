package dev.dubhe.anvilcraft.recipe.neo;

import org.jetbrains.annotations.NotNull;

public interface IPrioritized extends Comparable<IPrioritized> {
    default int getPriority() {
        return 1;
    }

    default int compareTo(@NotNull IPrioritized o) {
        return Integer.compare(this.getPriority(), o.getPriority());
    }
}
