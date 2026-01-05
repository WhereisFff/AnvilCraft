package dev.dubhe.anvilcraft.data.tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.dubhe.anvilcraft.init.item.ModComponentTags;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public class DataComponentTypeTagLoader {
    /**
     * 数据组件类型标签生成器初始化
     *
     * @param provider 提供器
     */
    public static void init(RegistrateTagsProvider<DataComponentType<?>> provider) {
        provider.addTag(ModComponentTags.TOOLS_SPECIAL)
            .add(findResourceKey(ModComponents.MERCILESS)) // 浮霜
            .add(findResourceKey(ModComponents.FIRE_REFORGING)) // 余烬
            .add(findResourceKey(ModComponents.MULTIPHASE)) // 超限
            .add(findResourceKey(ModComponents.ETERNAL)) // 超限
            .add(findResourceKey(ModComponents.PROVIDENCE)); // 超限
    }

    private static ResourceKey<DataComponentType<?>> findResourceKey(DataComponentType<?> type) {
        return ResourceKey.create(Registries.DATA_COMPONENT_TYPE, BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type));
    }
}
