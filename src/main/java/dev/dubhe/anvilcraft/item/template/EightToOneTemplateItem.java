package dev.dubhe.anvilcraft.item.template;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EightToOneTemplateItem extends BaseMultipleToOneTemplateItem {
    public static final List<ResourceLocation> EMPTY_SLOT_TEXTURES = List.of(
    );

    public EightToOneTemplateItem(Properties properties) {
        super(properties, 8);
    }

    @Override
    public Component getMaterialTooltip() {
        return Component.empty();
    }

    @Override
    public List<ResourceLocation> getEmptySlotTextures() {
        return EMPTY_SLOT_TEXTURES;
    }
}
