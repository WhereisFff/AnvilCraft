package dev.dubhe.anvilcraft.api.totem.handler;

import dev.dubhe.anvilcraft.api.totem.TotemOfUndyingHandler;
import dev.dubhe.anvilcraft.init.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

public class TotemManager {
    @Getter
    private final Map<Item, TotemHandler> totemMap = new Object2ObjectOpenHashMap<>() {
        {
            put(Items.TOTEM_OF_UNDYING, new TotemOfUndyingHandler());
            put(ModItems.AMULET_BOX.asItem(), new AmuletBoxHandler());
        }
    };
}
