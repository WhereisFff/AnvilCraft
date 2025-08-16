package dev.dubhe.anvilcraft.data.advancement;

import dev.dubhe.anvilcraft.advancements.criteron.BlockCompressingRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.BlockCrushingRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnythingAnvilCraftingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MeshRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.SqueezingRecipeTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;

public class AnvilProcessingLine {
    public static final AdvancementHolder dang = Advancement.Builder.advancement()
        .parent(AnvilCraftAdvancement.root)
        .display(
            Blocks.ANVIL,
            Component.literal("Dang!"),
            Component.literal("Perform anything anvil crafting"),
            null, AdvancementType.GOAL,
            true, true, false
        )
        .addCriterion("anything_anvil_crafting", AnythingAnvilCraftingTrigger.TriggerInstance.anvilCrafting())
        .build(advancementOf("dang"));

    public static final AdvancementHolder stoneCrusher = Advancement.Builder.advancement()
        .parent(dang)
        .display(
            Blocks.SAND,
            Component.literal("Stone Crusher"),
            Component.literal("Use an anvil to cobblestone to obtain gravel, then smash gravel to obtain sand"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("crush_cobblestone", BlockCrushingRecipeTrigger.TriggerInstance.blockCrushing(Items.COBBLESTONE, Items.GRAVEL))
        .addCriterion("crush_gravel", BlockCrushingRecipeTrigger.TriggerInstance.blockCrushing(Items.GRAVEL, Items.SAND))
        .build(advancementOf("stone_crusher"));

    public static final AdvancementHolder fossick = Advancement.Builder.advancement()
        .parent(stoneCrusher)
        .display(
            Items.GOLD_NUGGET,
            Component.literal("Fossick"),
            Component.literal("Use an anvil to smash the sand on the scaffold to obtain gold nugget"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("mesh", MeshRecipeTrigger.TriggerInstance.mesh(Items.SAND, Items.GOLD_NUGGET))
        .build(advancementOf("fossick"));

    public static final AdvancementHolder iceMaker = Advancement.Builder.advancement()
        .parent(dang)
        .display(
            Items.ICE,
            Component.literal("Ice Maker"),
            Component.literal("Use an anvil to smash snow blocks on a cauldron pot to make ice"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("make_ice", SqueezingRecipeTrigger.TriggerInstance.squeezing(Items.SNOW_BLOCK, Items.ICE))
        .build(advancementOf("ice_maker"));

    public static final AdvancementHolder four281 = Advancement.Builder.advancement()
        .parent(iceMaker)
        .display(
            Items.BLUE_ICE,
            Component.literal("4 to 81"),
            Component.literal("Use an anvil to compress two pieces of ice to make packed ice, and then use an anvil to compress two pieces of packed ice to make blue ice"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("packed_ice", BlockCompressingRecipeTrigger.TriggerInstance.blockCompressing(Items.ICE, Items.PACKED_ICE))
        .addCriterion("blue_ice", BlockCompressingRecipeTrigger.TriggerInstance.blockCompressing(Items.PACKED_ICE, Items.BLUE_ICE))
        .build(advancementOf("4281"));
}
