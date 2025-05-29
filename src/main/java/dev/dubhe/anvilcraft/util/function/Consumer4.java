package dev.dubhe.anvilcraft.util.function;

public interface Consumer4<T, U, V, W> {
    void accept(T t, U u, V v, W w);

    static void noop(T t, U u, V v, W w) {
    }
}
