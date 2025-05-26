package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IMultipleToOneSmithingRecipeMaterial;
import dev.dubhe.anvilcraft.client.gui.screen.EmberSmithingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MultiphaseMatterItem extends Item implements IMultipleToOneSmithingRecipeMaterial {
    public static final Component MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_matter.missing_tools");

    public MultiphaseMatterItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getInputTooltip() {
        return MISSING_TOOLS_TOOLTIP;
    }

    @Override
    public List<ResourceLocation> getEmptySlotTextures(int id, List<ItemStack> inputs) {
        for (ItemStack input : inputs) {
            if (input.is(ItemTags.AXES)) {
                return List.of(EmberSmithingScreen.EMPTY_SLOT_AXE);
            } else if (input.is(ItemTags.HOES)) {
                return List.of(EmberSmithingScreen.EMPTY_SLOT_HOE);
            } else if (input.is(ItemTags.PICKAXES)) {
                return List.of(EmberSmithingScreen.EMPTY_SLOT_PICKAXE);
            } else if (input.is(ItemTags.SHOVELS)) {
                return List.of(EmberSmithingScreen.EMPTY_SLOT_SHOVEL);
            } else if (input.is(ItemTags.SWORDS)) {
                return List.of(EmberSmithingScreen.EMPTY_SLOT_SWORD);
            }
        }
        return EmberSmithingScreen.EMPTY_SLOT_TOOLS;
    }
}
