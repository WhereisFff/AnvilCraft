package dev.dubhe.anvilcraft.util;

public interface AdsorbableItemEntity {
    default void setIsAdsorbable(boolean value) {
    }

    default boolean isAdsorbable() {
        return true;
    }
}
