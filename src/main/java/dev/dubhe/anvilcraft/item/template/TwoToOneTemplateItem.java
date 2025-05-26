package dev.dubhe.anvilcraft.item.template;

import dev.dubhe.anvilcraft.client.gui.screen.EmberSmithingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TwoToOneTemplateItem extends BaseMultipleToOneTemplateItem {
    public static final Component MISSING_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.two.missing");
    public static final List<ResourceLocation> EMPTY_SLOT_TEXTURES = List.of(
        EmberSmithingScreen.EMPTY_SLOT_MULTIPHASE_MATTER
    );

    public TwoToOneTemplateItem(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Component getMaterialTooltip() {
        return MISSING_TOOLTIP;
    }

    @Override
    public List<ResourceLocation> getEmptySlotTextures() {
        return EMPTY_SLOT_TEXTURES;
    }
}
