package dev.dubhe.anvilcraft.data.advancement;

import dev.dubhe.anvilcraft.advancements.criteron.InWorldRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnythingAnvilCraftingTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static dev.dubhe.anvilcraft.AnvilCraft.*;
import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;

public class AnvilProcessingLine {
    public static final AdvancementHolder dang = Advancement.Builder.advancement()
        .parent(AnvilCraftAdvancement.root)
        .display(
            Blocks.ANVIL,
            Component.translatable("advancements.anvilcraft.dang.title"),
            Component.translatable("advancements.anvilcraft.dang.description"),
            null, AdvancementType.GOAL,
            true, true, false
        )
        .addCriterion("anything_anvil_crafting", AnythingAnvilCraftingTrigger.TriggerInstance.anvilCrafting())
        .build(advancementOf("dang"));

    public static final AdvancementHolder stoneCrusher = Advancement.Builder.advancement()
        .parent(dang)
        .display(
            Blocks.SAND,
            Component.translatable("advancements.anvilcraft.stone_crusher.title"),
            Component.translatable("advancements.anvilcraft.stone_crusher.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("crush_cobblestone", InWorldRecipeTrigger.TriggerInstance.recipe(of("block_crush/gravel")))
        .addCriterion("crush_gravel", InWorldRecipeTrigger.TriggerInstance.recipe(of("block_crush/sand")))
        .build(advancementOf("stone_crusher"));

    public static final AdvancementHolder fossick = Advancement.Builder.advancement()
        .parent(stoneCrusher)
        .display(
            Items.GOLD_NUGGET,
            Component.translatable("advancements.anvilcraft.fossick.title"),
            Component.translatable("advancements.anvilcraft.fossick.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("mesh", InWorldRecipeTrigger.TriggerInstance.recipe(of("mesh/sand")))
        .build(advancementOf("fossick"));

    public static final AdvancementHolder iceMaker = Advancement.Builder.advancement()
        .parent(dang)
        .display(
            Items.ICE,
            Component.translatable("advancements.anvilcraft.ice_maker.title"),
            Component.translatable("advancements.anvilcraft.ice_maker.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("make_ice", InWorldRecipeTrigger.TriggerInstance.recipe(of("squeezing/power_snow_from_ice")))
        .build(advancementOf("ice_maker"));

    public static final AdvancementHolder four281 = Advancement.Builder.advancement()
        .parent(iceMaker)
        .display(
            Items.BLUE_ICE,
            Component.translatable("advancements.anvilcraft.four281.title"),
            Component.translatable("advancements.anvilcraft.four281.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("packed_ice", InWorldRecipeTrigger.TriggerInstance.recipe(of("block_compress/packed_ice")))
        .addCriterion("blue_ice", InWorldRecipeTrigger.TriggerInstance.recipe(of("block_compress/blue_ice")))
        .build(advancementOf("4281"));

    public static final AdvancementHolder vanillaIronPlate = Advancement.Builder.advancement()
        .parent(dang)
        .display(
            Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Component.translatable("advancements.anvilcraft.vanilla_iron_plate.title"),
            Component.translatable("advancements.anvilcraft.vanilla_iron_plate.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("heavy_weighted_pressure_plate", InWorldRecipeTrigger.TriggerInstance.recipe(of("stamping/heavy_weighted_pressure_plate")))
        .build(advancementOf("vanilla_iron_plate"));

    public static final AdvancementHolder recyclingDiamonds = Advancement.Builder.advancement()
        .parent(vanillaIronPlate)
        .display(
            Items.DIAMOND,
            Component.translatable("advancements.anvilcraft.recycling_diamonds.title"),
            Component.translatable("advancements.anvilcraft.recycling_diamonds.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .requirements(AdvancementRequirements.Strategy.OR)
        .addCriterion("diamond_pickaxe", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/tool/diamond_pickaxe_2_diamond")))
        .addCriterion("diamond_axe", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/tool/diamond_axe_2_diamond")))
        .addCriterion("diamond_sword", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/tool/diamond_sword_2_diamond")))
        .addCriterion("diamond_hoe", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/tool/diamond_hoe_2_diamond")))
        .addCriterion("diamond_shovel", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/tool/diamond_shovel_2_diamond")))
        .addCriterion("diamond_helmet", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/armor/diamond_helmet_2_diamond")))
        .addCriterion("diamond_chestplate", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/armor/diamond_chestplate_2_diamond")))
        .addCriterion("diamond_leggings", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/armor/diamond_leggings_2_diamond")))
        .addCriterion("diamond_boots", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/armor/diamond_boots_2_diamond")))
        .addCriterion("diamond_horse_armor", InWorldRecipeTrigger.TriggerInstance.recipe(of("item_crush/armor/diamond_horse_armor_2_diamond")))
        .build(advancementOf("recycling_diamonds"));
}
