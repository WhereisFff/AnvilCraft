package dev.dubhe.anvilcraft.block;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.block.IFrostBlock;
import dev.dubhe.anvilcraft.api.item.IMultipleMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class FrostMetalBlock extends Block implements IFrostBlock, IMultipleMaterial {
    private static final Component MISSING_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.frost_metal_block.missing_tools"
    );
    private static final List<ResourceLocation> EMPTY_SLOT_TEXTURES = List.of(
        AnvilCraft.of("item/empty_slot_amulet")
    );

    public FrostMetalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getInputTooltip(ItemStack template, List<ItemStack> inputs) {
        return FrostMetalBlock.MISSING_TOOLTIP;
    }

    @Override
    public List<ResourceLocation> getEmptySlotTextures(ItemStack template, int id, List<ItemStack> inputs) {
        return FrostMetalBlock.EMPTY_SLOT_TEXTURES;
    }
}
