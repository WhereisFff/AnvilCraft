package dev.dubhe.anvilcraft.data.advancement;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumAdvancementProvider;

public class AdvancementHandler {
    public static void init(RegistrumAdvancementProvider provider) {
        AnvilCraftAdvancement.init(provider);
    }
}
