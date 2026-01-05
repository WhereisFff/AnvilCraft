package dev.dubhe.anvilcraft.init.item;

import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class ModComponentTags {
    public static final TagKey<DataComponentType<?>> TOOLS_SPECIAL = bind("tools_special");

    public static TagKey<DataComponentType<?>> bind(String id) {
        return TagKey.create(Registries.DATA_COMPONENT_TYPE, AnvilCraft.of(id));
    }
}
