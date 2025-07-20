package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.item.IMultipleMaterial;
import dev.dubhe.anvilcraft.api.item.property.Eternal;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.item.template.FourToOneTemplateItem;
import dev.dubhe.anvilcraft.item.template.TwoToOneTemplateItem;
import dev.dubhe.anvilcraft.util.ListUtil;
import dev.dubhe.anvilcraft.util.Util;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.TriState;

import java.util.ArrayList;
import java.util.List;

public class MultiphaseTranscendiumItem extends Item implements IMultipleMaterial {
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
    private static final ResourceLocation EMPTY_SLOT_TRIDENT =
        AnvilCraft.of("item/empty_slot_trident");
    private static final ResourceLocation EMPTY_SLOT_MACE =
        AnvilCraft.of("item/empty_slot_mace");
    private static final ResourceLocation EMPTY_SLOT_RESONATOR =
        AnvilCraft.of("item/empty_slot_resonator");
    private static final ResourceLocation EMPTY_SLOT_HEAVY_HALBERD =
        AnvilCraft.of("item/empty_slot_heavy_halberd");
    private static final Component TWO_MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_transcendium.two_missing_tools");
    private static final Component FOUR_MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_transcendium.four_missing_tools");
    private static final Component RESONATOR_TWO_MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_transcendium.resonator_two_missing_tools");
    private static final Component RESONATOR_FOUR_MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_transcendium.resonator_four_missing_tools");
    private static final Component HEAVY_HALBERD_TWO_MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_transcendium.heavy_halberd_two_missing_tools");
    private static final Component HEAVY_HALBERD_FOUR_MISSING_TOOLS_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.multiphase_transcendium.heavy_halberd_four_missing_tools");
    private static final List<ResourceLocation> TWO_EMPTY_SLOT_TEXTURES = List.of(
        EMPTY_SLOT_RESONATOR,
        EMPTY_SLOT_HEAVY_HALBERD
    );
    private static final List<ResourceLocation> FOUR_EMPTY_SLOT_TEXTURES = List.of(
        EMPTY_SLOT_SWORD,
        EMPTY_SLOT_HOE,
        EMPTY_SLOT_AXE,
        EMPTY_SLOT_PICKAXE,
        EMPTY_SLOT_TRIDENT,
        EMPTY_SLOT_AXE,
        EMPTY_SLOT_MACE,
        EMPTY_SLOT_SHOVEL
    );

    public MultiphaseTranscendiumItem(Properties properties) {
        super(properties.component(ModComponents.ETERNAL, Eternal.INSTANCE));
    }

    @Override
    public Component getInputTooltip(ItemStack template, List<ItemStack> inputs) {
        TriState tool = TriState.DEFAULT;
        for (ItemStack input : inputs) {
            if (input.is(ModItems.EMBER_METAL_RESONATOR)
                || input.is(ModItems.MULTIPHASE_MATTER_SHOVEL)
                || input.is(ModItems.MULTIPHASE_MATTER_HOE)
                || input.is(ModItems.MULTIPHASE_MATTER_PICKAXE)
            ) {
                tool = TriState.TRUE;
            }
            if (input.is(ModItems.EMBER_METAL_HEAVY_HALBERD)
                || input.is(ModItems.MULTIPHASE_MATTER_SWORD)
                || input.is(Items.TRIDENT)
                || input.is(Tags.Items.TOOLS_MACE)
            ) {
                tool = TriState.FALSE;
            }
        }
        return switch (template.getItem()) {
            case TwoToOneTemplateItem ignored -> switch (tool) {
                case DEFAULT -> TWO_MISSING_TOOLS_TOOLTIP;
                case TRUE -> RESONATOR_TWO_MISSING_TOOLS_TOOLTIP;
                case FALSE -> HEAVY_HALBERD_TWO_MISSING_TOOLS_TOOLTIP;
            };
            case FourToOneTemplateItem ignored -> switch (tool) {
                case DEFAULT -> FOUR_MISSING_TOOLS_TOOLTIP;
                case TRUE -> RESONATOR_FOUR_MISSING_TOOLS_TOOLTIP;
                case FALSE -> HEAVY_HALBERD_FOUR_MISSING_TOOLS_TOOLTIP;
            };
            default -> TWO_MISSING_TOOLS_TOOLTIP;
        };
    }

    @Override
    public List<ResourceLocation> getEmptySlotTextures(ItemStack template, int id, List<ItemStack> inputs) {
        if (Util.instanceOfAny(template.getItem(), TwoToOneTemplateItem.class)) return TWO_EMPTY_SLOT_TEXTURES;
        if (!Util.instanceOfAny(template.getItem(), FourToOneTemplateItem.class)) return List.of();
        Int2IntMap slotIdMap = new Int2IntArrayMap();
        int emptyIndex = 0;
        List<ResourceLocation> textures = new ArrayList<>(FOUR_EMPTY_SLOT_TEXTURES);
        boolean isDefault = true;
        for (int i = 0, inputsSize = inputs.size(); i < inputsSize; i++) {
            ItemStack input = inputs.get(i);
            if (input.isEmpty()) {
                slotIdMap.put(i, emptyIndex++);
            }
            if (input.is(ModItems.MULTIPHASE_MATTER_AXE)) {
                textures.remove(EMPTY_SLOT_AXE);
                textures.remove(EMPTY_SLOT_AXE);
            } else if (
                input.is(ModItems.MULTIPHASE_MATTER_SHOVEL)
                || input.is(ModItems.MULTIPHASE_MATTER_HOE)
                || input.is(ModItems.MULTIPHASE_MATTER_PICKAXE)
            ) {
                textures.remove(EMPTY_SLOT_SWORD);
                textures.remove(EMPTY_SLOT_AXE);
                textures.remove(EMPTY_SLOT_TRIDENT);
                textures.remove(EMPTY_SLOT_MACE);
                isDefault = false;
                if (input.is(ModItems.MULTIPHASE_MATTER_SHOVEL)) {
                    textures.remove(EMPTY_SLOT_SHOVEL);
                } else if (input.is(ModItems.MULTIPHASE_MATTER_HOE)) {
                    textures.remove(EMPTY_SLOT_HOE);
                } else if (input.is(ModItems.MULTIPHASE_MATTER_PICKAXE)) {
                    textures.remove(EMPTY_SLOT_PICKAXE);
                }
            } else if (
                input.is(ModItems.MULTIPHASE_MATTER_SWORD)
                || input.is(Items.TRIDENT)
                || input.is(Tags.Items.TOOLS_MACE)
            ) {
                textures.remove(EMPTY_SLOT_AXE);
                textures.remove(EMPTY_SLOT_SHOVEL);
                textures.remove(EMPTY_SLOT_HOE);
                textures.remove(EMPTY_SLOT_PICKAXE);
                isDefault = false;
                if (input.is(ModItems.MULTIPHASE_MATTER_SWORD)) {
                    textures.remove(EMPTY_SLOT_SWORD);
                } else if (input.is(Items.TRIDENT)) {
                    textures.remove(EMPTY_SLOT_TRIDENT);
                } else if (input.is(Tags.Items.TOOLS_MACE)) {
                    textures.remove(EMPTY_SLOT_MACE);
                }
            }
        }
        return ListUtil.cycle(textures, (slotIdMap.containsKey(id) ? slotIdMap.get(id) : id) * (isDefault ? 2 : 1));
    }
}
