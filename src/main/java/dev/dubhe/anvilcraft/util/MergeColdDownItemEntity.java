package dev.dubhe.anvilcraft.util;

/**
 * 用于给ItemEntity设置合并冷却
 */
public interface MergeColdDownItemEntity {
    default void setMergeColdDown(@SuppressWarnings("unused") int coldDown) {
    }
}
