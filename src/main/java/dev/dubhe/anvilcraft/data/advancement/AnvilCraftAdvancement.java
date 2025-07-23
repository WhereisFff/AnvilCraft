package dev.dubhe.anvilcraft.data.advancement;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceBlockTrigger;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModLootTables;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.network.chat.Component;

import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;

public class AnvilCraftAdvancement {
    public static void init(RegistrateAdvancementProvider provider) {
        AdvancementHolder root = Advancement.Builder.advancement()
            .display(
                ModBlocks.ROYAL_ANVIL.asItem(),
                Component.translatable("advancements.anvilcraft.root.title"),
                Component.translatable("advancements.anvilcraft.root.description"),
                AnvilCraft.of("textures/gui/advancements/background.png"),
                AdvancementType.TASK,
                false, true, false
            )
            .addCriterion("join", PlayerTrigger.TriggerInstance.tick())
            .rewards(AdvancementRewards.Builder.loot(ModLootTables.ADVANCEMENT_ROOT))
            .build(advancementOf("root"));

        AdvancementHolder crabClaw = Advancement.Builder.advancement()
            .parent(root)
            .display(
                ModItems.CRAB_CLAW,
                Component.translatable("advancements.anvilcraft.crab_claw.title"),
                Component.translatable("advancements.anvilcraft.crab_claw.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("crab_claw", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CRAB_CLAW))
            .build(advancementOf("crab_claw"));

        AdvancementHolder placer = Advancement.Builder.advancement()
            .parent(crabClaw)
            .display(
                ModBlocks.BLOCK_PLACER.asItem(),
                Component.translatable("advancements.anvilcraft.placer.title"),
                Component.translatable("advancements.anvilcraft.placer.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("placer", PlacerPlaceBlockTrigger.TriggerInstance.placeBlock(ModBlocks.BLOCK_PLACER.get()))
            .build(advancementOf("placer"));

        AdvancementHolder devourer = Advancement.Builder.advancement()
            .parent(placer)
            .display(
                ModBlocks.BLOCK_DEVOURER.asItem(),
                Component.translatable("advancements.anvilcraft.devourer.title"),
                Component.translatable("advancements.anvilcraft.devourer.description"),
                null, AdvancementType.CHALLENGE,
                true, true, false
            )
            .addCriterion("devourer", DevourerDevourBlockTrigger.TriggerInstance.devourBlock(ModBlocks.BLOCK_DEVOURER.get()))
            .build(advancementOf("devourer"));

        provider.accept(root);
        provider.accept(crabClaw);
        provider.accept(placer);
        provider.accept(devourer);
    }
}
