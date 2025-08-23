package dev.dubhe.anvilcraft.data.advancement;

import dev.dubhe.anvilcraft.advancements.criteron.AnvilHammerHurtEntityTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHammerLeftClickBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHammerRightClickBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHammerShiftRightClickBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHitPiezoelectricCrystalTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.ConvertBeaconTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.InWorldRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnythingAnvilCraftingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.InWorldSuperHeatingRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.InWorldTimewarpRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlayerKilledEntityByAnvilHammerTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlayerWearAnvilHammerTrigger;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static dev.dubhe.anvilcraft.AnvilCraft.of;
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

    public static final AdvancementHolder allInOne = Advancement.Builder.advancement()
        .parent(dang)
        .display(
            ModItems.ANVIL_HAMMER,
            Component.translatable("advancements.anvilcraft.all_in_one.title"),
            Component.translatable("advancements.anvilcraft.all_in_one.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("anvil_hammer", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("anvil_hammer")))
        .addCriterion("royal_anvil_hammer", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_anvil_hammer")))
        .addCriterion("ember_anvil_hammer", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/ember_anvil_hammer")))
        .addCriterion("transcendence_anvil_hammer", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/transcendence_anvil_hammer")))
        .addCriterion("left_click", AnvilHammerLeftClickBlockTrigger.TriggerInstance.clickBlock())
        .addCriterion("right_click", AnvilHammerRightClickBlockTrigger.TriggerInstance.clickBlock())
        .addCriterion("shift_right_click", AnvilHammerShiftRightClickBlockTrigger.TriggerInstance.clickBlock())
        .addCriterion("hurt_entity", AnvilHammerHurtEntityTrigger.TriggerInstance.hurtEntity())
        .requirements(new AdvancementRequirements(List.of(
            List.of("anvil_hammer", "royal_anvil_hammer", "ember_anvil_hammer", "transcendence_anvil_hammer"),
            List.of("left_click"),
            List.of("right_click"),
            List.of("shift_right_click"),
            List.of("hurt_entity")
        )))
        .build(advancementOf("all_in_one"));

    public static final AdvancementHolder heartsOfIron = Advancement.Builder.advancement()
        .parent(allInOne)
        .display(
            ModBlocks.MAGNETO_ELECTRIC_CORE_BLOCK,
            Component.translatable("advancements.anvilcraft.hearts_of_iron.title"),
            Component.translatable("advancements.anvilcraft.hearts_of_iron.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("craft_magnetoelectric_core", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("magnetoelectric_core")))
        .build(advancementOf("hearts_of_iron"));

    public static final AdvancementHolder notABeacon = Advancement.Builder.advancement()
        .parent(heartsOfIron)
        .display(
            ModBlocks.CHARGE_COLLECTOR,
            Component.translatable("advancements.anvilcraft.not_beacon.title"),
            Component.translatable("advancements.anvilcraft.not_beacon.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("craft_charge_collector", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("charge_collector")))
        .addCriterion("place_charge_collector", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.CHARGE_COLLECTOR.get()))
        .build(advancementOf("not_beacon"));

    public static final AdvancementHolder lighter = Advancement.Builder.advancement()
        .parent(notABeacon)
        .display(
            ModBlocks.PIEZOELECTRIC_CRYSTAL,
            Component.translatable("advancements.anvilcraft.lighter.title"),
            Component.translatable("advancements.anvilcraft.lighter.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("hit_piezoelectric_crystal", AnvilHitPiezoelectricCrystalTrigger.TriggerInstance.hit())
        .build(advancementOf("lighter"));

    public static final AdvancementHolder networking = Advancement.Builder.advancement()
        .parent(heartsOfIron)
        .display(
            ModBlocks.TRANSMISSION_POLE,
            Component.translatable("advancements.anvilcraft.networking.title"),
            Component.translatable("advancements.anvilcraft.networking.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("craft_transmission_pole", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("transmission_pole")))
        .addCriterion("place_transmission_pole", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.TRANSMISSION_POLE.get()))
        .build(advancementOf("networking"));

    public static final AdvancementHolder electricFiledRhythm = Advancement.Builder.advancement()
        .parent(networking)
        .display(
            ModItems.ANVIL_HAMMER,
            Component.translatable("advancements.anvilcraft.electric_filed_rhythm.title"),
            Component.translatable("advancements.anvilcraft.electric_filed_rhythm.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("wear_anvil_hammer", PlayerWearAnvilHammerTrigger.TriggerInstance.wearAnvilHammer())
        .build(advancementOf("electric_filed_rhythm"));

    public static final AdvancementHolder industrialGradeSmelting = Advancement.Builder.advancement()
        .parent(electricFiledRhythm)
        .display(
            ModBlocks.HEATER,
            Component.translatable("advancements.anvilcraft.industrial_grade_smelting.title"),
            Component.translatable("advancements.anvilcraft.industrial_grade_smelting.description"),
            null, AdvancementType.GOAL,
            true, true, false
        )
        .addCriterion("super_heating", InWorldSuperHeatingRecipeTrigger.TriggerInstance.superHeating())
        .build(advancementOf("industrial_grade_smelting"));

    public static final AdvancementHolder nobleMetal = Advancement.Builder.advancement()
        .parent(industrialGradeSmelting)
        .display(
            ModItems.ROYAL_STEEL_INGOT,
            Component.translatable("advancements.anvilcraft.noble_metal.title"),
            Component.translatable("advancements.anvilcraft.noble_metal.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("royal_metal", InWorldSuperHeatingRecipeTrigger.TriggerInstance.superHeating(of("super_heating/royal_steel_ingot")))
        .build(advancementOf("noble_metal"));

    public static final AdvancementHolder smithingTale = Advancement.Builder.advancement()
        .parent(nobleMetal)
        .display(
            ModBlocks.ROYAL_SMITHING_TABLE,
            Component.translatable("advancements.anvilcraft.smithing_table.title"),
            Component.translatable("advancements.anvilcraft.smithing_table.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("craft_smithing",  RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_smithing_table")))
        .build(advancementOf("smithing_table"));

    public static final AdvancementHolder overseer = Advancement.Builder.advancement()
        .parent(nobleMetal)
        .display(
            ModBlocks.OVERSEER_BLOCK,
            Component.translatable("advancements.anvilcraft.overseer.title"),
            Component.translatable("advancements.anvilcraft.overseer.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("craft_overseer", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("overseer")))
        .build(advancementOf("overseer"));

    public static final AdvancementHolder durableGoods = Advancement.Builder.advancement()
        .parent(smithingTale)
        .display(
            ModItems.ROYAL_STEEL_PICKAXE,
            Component.translatable("advancements.anvilcraft.durable_goods.title"),
            Component.translatable("advancements.anvilcraft.durable_goods.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("royal_steel_pickaxe", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_steel_pickaxe")))
        .addCriterion("royal_steel_axe", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_steel_axe")))
        .addCriterion("royal_steel_shovel", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_steel_shovel")))
        .addCriterion("royal_steel_hoe", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_steel_hoe")))
        .addCriterion("royal_steel_sword", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("smithing/royal_steel_sword")))
        .requirements(AdvancementRequirements.Strategy.OR)
        .build(advancementOf("durable_goods"));

    public static final AdvancementHolder royalBlacksmith = Advancement.Builder.advancement()
        .parent(smithingTale)
        .display(
            ModBlocks.ROYAL_ANVIL,
            Component.translatable("advancements.anvilcraft.royal_blacksmith.title"),
            Component.translatable("advancements.anvilcraft.royal_blacksmith.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("royal_anvil", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ROYAL_ANVIL))
        .addCriterion("royal_smithing_table", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ROYAL_SMITHING_TABLE))
        .addCriterion("royal_grindstone", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ROYAL_GRINDSTONE))
        .build(advancementOf("royal_blacksmith"));

    public static final AdvancementHolder wither = Advancement.Builder.advancement()
        .parent(royalBlacksmith)
        .display(
            ModBlocks.CORRUPTED_BEACON,
            Component.translatable("advancements.anvilcraft.wither.title"),
            Component.translatable("advancements.anvilcraft.wither.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("convert_beacon", ConvertBeaconTrigger.TriggerInstance.convertBeacon())
        .build(advancementOf("wither"));

    public static final AdvancementHolder ripVanWinkle = Advancement.Builder.advancement()
        .parent(wither)
        .display(
            ModBlocks.CORRUPTED_BEACON,
            Component.translatable("advancements.anvilcraft.ripVanWinkle.title"),
            Component.translatable("advancements.anvilcraft.ripVanWinkle.description"),
            null, AdvancementType.GOAL,
            true, true, false
        )
        .addCriterion("timewarp_recipe", InWorldTimewarpRecipeTrigger.TriggerInstance.timeWrap())
        .build(advancementOf("rip_van_winkle"));

    public static final AdvancementHolder hammer = Advancement.Builder.advancement()
        .parent(allInOne)
        .display(
            ModItems.ANVIL_HAMMER,
            Component.translatable("advancements.anvilcraft.hammer.title"),
            Component.translatable("advancements.anvilcraft.hammer.description"),
            null, AdvancementType.CHALLENGE,
            true, true, false
        )
        .addCriterion("kill_zombie", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.ZOMBIE))
        .addCriterion("kill_skeleton", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.SKELETON))
        .addCriterion("kill_creeper", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.CREEPER))
        .addCriterion("kill_spider", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.SPIDER))
        .addCriterion("kill_pig", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.PIG))
        .addCriterion("kill_cow", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.COW))
        .addCriterion("kill_sheep", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.SHEEP))
        .addCriterion("kill_chicken", PlayerKilledEntityByAnvilHammerTrigger.TriggerInstance.killedEntity(EntityType.CHICKEN))
        .build(advancementOf("hammer"));

    public static final AdvancementHolder superKill = Advancement.Builder.advancement()
        .parent(hammer)
        .display(
            ModItems.ROYAL_ANVIL_HAMMER,
            Component.translatable("advancements.anvilcraft.super_kill.title"),
            Component.translatable("advancements.anvilcraft.super_kill.description"),
            null, AdvancementType.CHALLENGE,
            true, true, false
        )
        .addCriterion("super_kill", AnvilHammerHurtEntityTrigger.TriggerInstance.hurtEntity(80))
        .build(advancementOf("super_kill"));
}
