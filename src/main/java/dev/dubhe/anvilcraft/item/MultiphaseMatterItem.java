package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IMultipleMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MultiphaseMatterItem extends Item implements IMultipleMaterial {
    private static final ResourceLocation EMPTY_SLOT_AXE =
        ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_HOE =
        ResourceLocation.withDefaultNamespace("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE =
        ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL =
        ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_SWORD =
        ResourceLocation.withDefaultNamespace("item/empty_slot_sword");
    private static final Component MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_matter.missing_tools");
    private static final List<ResourceLocation> EMPTY_SLOT_TEXTURES = List.of(
        EMPTY_SLOT_AXE,
        EMPTY_SLOT_HOE,
        EMPTY_SLOT_PICKAXE,
        EMPTY_SLOT_SHOVEL,
        EMPTY_SLOT_SWORD
    );

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
                return List.of(EMPTY_SLOT_AXE);
            } else if (input.is(ItemTags.HOES)) {
                return List.of(EMPTY_SLOT_HOE);
            } else if (input.is(ItemTags.PICKAXES)) {
                return List.of(EMPTY_SLOT_PICKAXE);
            } else if (input.is(ItemTags.SHOVELS)) {
                return List.of(EMPTY_SLOT_SHOVEL);
            } else if (input.is(ItemTags.SWORDS)) {
                return List.of(EMPTY_SLOT_SWORD);
            }
        }
        return EMPTY_SLOT_TEXTURES;
    }
}
