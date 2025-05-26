package dev.dubhe.anvilcraft.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListUtil {
    public static <T> @Nullable T safelyGet(List<T> list, int index) {
        if (index < 0 || list.isEmpty() || index >= list.size()) return null;
        return list.get(index);
    }

    public static <T> List<T> cycle(List<T> original, int times) {
        if (times == 0) return original;
        times = original.size() % times;
        if (times == 0) return original;
        List<T> cycled = new ArrayList<>(original.size());
        for (int i = 0; i < original.size(); i++) {
            cycled.add(original.get((i + times) % times));
        }
        return cycled;
    }
}
