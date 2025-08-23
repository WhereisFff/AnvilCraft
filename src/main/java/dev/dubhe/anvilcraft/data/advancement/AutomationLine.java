package dev.dubhe.anvilcraft.data.advancement;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingIronGolemTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DispenserRepairIronGolem;
import dev.dubhe.anvilcraft.advancements.criteron.MilkTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

public class AutomationLine {
    public static final AdvancementHolder redstoneMilker = Advancement.Builder.advancement()
        .parent(AnvilCraftAdvancement.root)
        .display(
            Blocks.DISPENSER,
            Component.translatable("advancements.anvilcraft.redstone_milker.title"),
            Component.translatable("advancements.anvilcraft.redstone_milker.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("milk", MilkTrigger.TriggerInstance.milk())
        .build(AnvilCraft.advancementOf("redstone_milker"));

    public static final AdvancementHolder realLooting = Advancement.Builder.advancement()
        .parent(redstoneMilker)
        .display(
            Blocks.ANVIL,
            Component.translatable("advancements.anvilcraft.real_looting.title"),
            Component.translatable("advancements.anvilcraft.real_looting.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("anvil_looting", AnvilLootingTrigger.TriggerInstance.looting())
        .build(AnvilCraft.advancementOf("real_looting"));

    public static final AdvancementHolder ironMeterReversal = Advancement.Builder.advancement()
        .parent(realLooting)
        .display(
            Blocks.IRON_BLOCK,
            Component.translatable("advancements.anvilcraft.iron_meter_reversal.title"),
            Component.translatable("advancements.anvilcraft.iron_meter_reversal.description"),
            null, AdvancementType.GOAL,
            true, true, false
        )
        .addCriterion("anvil_looting_iron_golem", AnvilLootingIronGolemTrigger.TriggerInstance.looting())
        .addCriterion("repair_iron_golem", DispenserRepairIronGolem.TriggerInstance.repair())
        .build(AnvilCraft.advancementOf("iron_meter_reversal"));
}
