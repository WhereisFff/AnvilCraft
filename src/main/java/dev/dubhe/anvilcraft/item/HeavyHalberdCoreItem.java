package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IMultipleToOneSmithingRecipeMaterial;
import dev.dubhe.anvilcraft.client.gui.screen.EmberSmithingScreen;
import dev.dubhe.anvilcraft.util.ListUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

public class HeavyHalberdCoreItem extends Item implements IMultipleToOneSmithingRecipeMaterial {
    private static final Component MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.heavy_halberd_core.missing_tools");
    private static final List<ResourceLocation> EMPTY_SLOT_TEXTURES = List.of(
        EmberSmithingScreen.EMPTY_SLOT_SWORD, EmberSmithingScreen.EMPTY_SLOT_AXE,
        EmberSmithingScreen.EMPTY_SLOT_TRIDENT, EmberSmithingScreen.EMPTY_SLOT_MACE);

    public HeavyHalberdCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getInputTooltip() {
        return MISSING_TOOLS_TOOLTIP;
    }

    @Override
    public List<ResourceLocation> getEmptySlotTextures(int id, List<ItemStack> inputs) {
        List<ResourceLocation> textures = new ArrayList<>(EMPTY_SLOT_TEXTURES);
        for (ItemStack input : inputs) {
            if (input.is(ItemTags.SWORDS)) {
                textures.remove(EmberSmithingScreen.EMPTY_SLOT_SWORD);
            } else if (input.is(ItemTags.AXES)) {
                textures.remove(EmberSmithingScreen.EMPTY_SLOT_AXE);
            } else if (input.is(Items.TRIDENT)) {
                textures.remove(EmberSmithingScreen.EMPTY_SLOT_TRIDENT);
            } else if (input.is(Tags.Items.TOOLS_MACE)) {
                textures.remove(EmberSmithingScreen.EMPTY_SLOT_MACE);
            }
        }
        return textures;
    }
}
