package dev.dubhe.anvilcraft.util.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.Level;

public class ProvidenceRef {
    private static final ThreadLocal<Boolean> SHOULD_TRIGGER = new ThreadLocal<>();

    public static void shouldTrigger() {
        SHOULD_TRIGGER.set(true);
    }

    public static boolean shouldItTrigger() {
        return SHOULD_TRIGGER.get();
    }

    public static void reset() {
        SHOULD_TRIGGER.remove();
    }

    record ProvidenceData(ResourceKey<Level> dimension, int enchantmentLevel, EnchantedItemInUse item, int entityId) {
    }
}
