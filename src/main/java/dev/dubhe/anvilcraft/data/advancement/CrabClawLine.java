package dev.dubhe.anvilcraft.data.advancement;

import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceTrigger;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;

import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;

public class CrabClawLine {
    public static final AdvancementHolder crbClaw = Advancement.Builder.advancement()
        .parent(AnvilCraftAdvancement.root)
        .display(
            ModItems.CRAB_CLAW,
            Component.translatable("advancements.anvilcraft.crab_claw.title"),
            Component.translatable("advancements.anvilcraft.crab_claw.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("has_crab_claw", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CRAB_CLAW))
        .build(advancementOf("crab_claw"));

    public static final AdvancementHolder placer = Advancement.Builder.advancement()
        .parent(crbClaw)
        .display(
            ModBlocks.BLOCK_PLACER.asItem(),
            Component.translatable("advancements.anvilcraft.placer.title"),
            Component.translatable("advancements.anvilcraft.placer.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("placer_place_placer", PlacerPlaceTrigger.TriggerInstance.placeBlock(ModBlocks.BLOCK_PLACER.get()))
        .build(advancementOf("block_placer"));

    public static final AdvancementHolder devourer = Advancement.Builder.advancement()
        .parent(placer)
        .display(
            ModBlocks.BLOCK_DEVOURER.asItem(),
            Component.translatable("advancements.anvilcraft.devourer.title"),
            Component.translatable("advancements.anvilcraft.devourer.description"),
            null, AdvancementType.CHALLENGE,
            true, true, false
        )
        .addCriterion("devourer_devour_devourer", DevourerDevourTrigger.TriggerInstance.devourBlock(ModBlocks.BLOCK_DEVOURER.get()))
        .build(advancementOf("block_devourer"));
}
