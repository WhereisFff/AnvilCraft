package dev.dubhe.anvilcraft.util.mixin.ref;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.resources.ResourceLocation;

public class ProvidenceRef {
    private static final Object2BooleanMap<ResourceLocation> ID_2_TRIGGERED = new Object2BooleanArrayMap<>();

    public static void register(ResourceLocation id) {
        ID_2_TRIGGERED.put(id, false);
    }

    public static boolean shouldProvidence(ResourceLocation id) {
        return ID_2_TRIGGERED.removeBoolean(id);
    }
}
