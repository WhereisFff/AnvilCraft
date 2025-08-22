package dev.dubhe.anvilcraft.integration.jei.util;

import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.resources.ResourceLocation;

public class TextureConstants {
    public static final String BASE_PATH = "textures/gui/";

    public static final ResourceLocation PROGRESS =
        ResourceLocation.parse(BASE_PATH + "sprites/container/furnace/burn_progress.png");

    public static final ResourceLocation ANVIL_CRAFT_SPRITES = AnvilCraft.of(BASE_PATH + "sprites/jei.png");

    // Arrow
    public static final ResourceLocation ARROW_DEFAULT = AnvilCraft.of(BASE_PATH + "sprites/jei/arrow_default.png");
    public static final ResourceLocation ARROW_BLOCK_CONVERSION = AnvilCraft.of(BASE_PATH + "sprites/jei/block_conversion.png");
    public static final ResourceLocation ARROW_INPUT = AnvilCraft.of(BASE_PATH + "sprites/jei/input.png");
    public static final ResourceLocation ARROW_OUTPUT = AnvilCraft.of(BASE_PATH + "sprites/jei/output.png");
    public static final ResourceLocation ARROW_OUTPUT_FROM_BELOW = AnvilCraft.of(BASE_PATH + "sprites/jei/output_from_below.png");

    // Slot
    public static final ResourceLocation SLOT_CHOICE = AnvilCraft.of(BASE_PATH + "sprites/jei/slot_choice.png");
    public static final ResourceLocation SLOT_DEFAULT = AnvilCraft.of(BASE_PATH + "sprites/jei/slot_default.png");
    public static final ResourceLocation SLOT_PROBABILITY = AnvilCraft.of(BASE_PATH + "sprites/jei/slot_probability.png");

    // Other
    public static final ResourceLocation EXPLOSION = AnvilCraft.of(BASE_PATH + "sprites/jei/explosion.png");


    public static final ResourceLocation PRE_RENDERED_END_PORTAL =
        AnvilCraft.of(BASE_PATH + "pre_rendered_end_portal.png");

    public static final ResourceLocation BLOCK_CONVERSION = AnvilCraft.of(BASE_PATH + "block_conversion.png");
    public static final ResourceLocation BLOCK_CRAFTING = AnvilCraft.of(BASE_PATH + "block_craft.png");
}
