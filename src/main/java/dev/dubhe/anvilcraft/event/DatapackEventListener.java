package dev.dubhe.anvilcraft.event;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.anvil.outcome.RoyalPreferenceOutcome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class DatapackEventListener {
    @SubscribeEvent
    public static void onDatapack(AddReloadListenerEvent event) {
        event.addListener((barrier, manager, prpProfiler, rldProfiler, bgExec, gmExec) -> {
            RoyalPreferenceOutcome.RoyalPreference.initRoyalPreference(ServerLifecycleHooks.getCurrentServer().overworld());
            return barrier.wait(null);
        });
    }
}
