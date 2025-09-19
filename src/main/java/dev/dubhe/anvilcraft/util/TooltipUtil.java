package dev.dubhe.anvilcraft.util;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.common.Internal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class TooltipUtil {
    public static List<Component> tooltip(Block block) {
        IModIdHelper modIdHelper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
        List<Component> tooltip = new ArrayList<>();
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        tooltip.add(block.getName());
        tooltip.add(Component.literal(key.toString()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal(modIdHelper.getFormattedModNameForModId(key.getNamespace())).withStyle(ChatFormatting.BLUE).withStyle(Style.EMPTY.withItalic(true)));
        return tooltip;
    }

    public static List<Component> recipeIDTooltip(Block block, ResourceLocation id) {
        IModIdHelper modIdHelper = Internal.getJeiRuntime().getJeiHelpers().getModIdHelper();
        List<Component> tooltip = new ArrayList<>();
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        tooltip.add(block.getName());
        tooltip.add(Component.literal(key.toString()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("jei.tooltip.recipe.id", id.toString()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal(modIdHelper.getFormattedModNameForModId(key.getNamespace())).withStyle(ChatFormatting.BLUE).withStyle(Style.EMPTY.withItalic(true)));
        return tooltip;
    }
}
