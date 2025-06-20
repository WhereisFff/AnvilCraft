package dev.dubhe.anvilcraft.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListUtil {
    public static <T> Optional<T> safelyGet(List<T> list, int index) {
        if (index < 0 || list.isEmpty() || index >= list.size()) return Optional.empty();
        return Optional.ofNullable(list.get(index));
    }

    public static <T> List<T> cycle(List<T> original, int times) {
        if (times == 0) return new ArrayList<>(original);
        times %= original.size();
        if (times == 0) return new ArrayList<>(original);
        List<T> cycled = new ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            cycled.add(original.get((i + times) % original.size()));
        }
        return cycled;
    }
}
