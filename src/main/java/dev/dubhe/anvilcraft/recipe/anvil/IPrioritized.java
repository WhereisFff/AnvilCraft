package dev.dubhe.anvilcraft.recipe.anvil;

import org.jetbrains.annotations.NotNull;

public interface IPrioritized extends Comparable<IPrioritized> {
    default int getPriority() {
        return 1;
    }

    default int compareTo(@NotNull IPrioritized o) {
        if (this.equals(o)) return 0;
        int compared = Integer.compare(this.getPriority(), o.getPriority());
        return compared == 0 ? 1 : -compared;
    }
}
