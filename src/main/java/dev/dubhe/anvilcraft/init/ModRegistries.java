package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModRegistries {
    public static final ResourceKey<Registry<AmuletType>> AMULET_TYPE_KEY = ResourceKey.createRegistryKey(AnvilCraft.of("amulet_type"));
    public static final Registry<AmuletType> AMULET_TYPE_REGISTRY = new RegistryBuilder<>(AMULET_TYPE_KEY)
        .maxId(512)
        .create();

    @SubscribeEvent
    public static void registerRegistries(@NotNull NewRegistryEvent event) {
        event.register(AMULET_TYPE_REGISTRY);
    }
}
