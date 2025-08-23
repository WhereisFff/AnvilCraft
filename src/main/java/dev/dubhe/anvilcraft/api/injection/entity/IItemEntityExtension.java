package dev.dubhe.anvilcraft.api.injection.entity;

public interface IItemEntityExtension {
    default void anvilcraft$setIsAdsorbable(boolean value) {
        throw new AssertionError();
    }

    default boolean anvilcraft$isAdsorbable() {
        throw new AssertionError();
    }

    default boolean anvilcraft$getDiscarded() {
        throw new AssertionError();
    }

    default void anvilcraft$setMergeCooldown(int cooldown) {
        throw new AssertionError();
    }
}
