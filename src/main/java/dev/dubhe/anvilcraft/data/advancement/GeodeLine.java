package dev.dubhe.anvilcraft.data.advancement;

import dev.dubhe.anvilcraft.advancements.criteron.AnvilOnGroundTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MagnetLiftingAnvilTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.UseItemTrigger;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.network.chat.Component;

import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;
import static dev.dubhe.anvilcraft.AnvilCraft.of;

public class GeodeLine {
    public static final AdvancementHolder geode = Advancement.Builder.advancement()
        .parent(AnvilCraftAdvancement.root)
        .display(
            ModItems.GEODE,
            Component.translatable("advancements.anvilcraft.geode.title"),
            Component.translatable("advancements.anvilcraft.geode.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("use_geode", UseItemTrigger.TriggerInstance.useItem(ModItems.GEODE.get()))
        .build(advancementOf("geode"));

    public static final AdvancementHolder amethystPickaxe = Advancement.Builder.advancement()
        .parent(geode)
        .display(
            ModItems.AMETHYST_PICKAXE,
            Component.translatable("advancements.anvilcraft.amethyst_pickaxe.title"),
            Component.translatable("advancements.anvilcraft.amethyst_pickaxe.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("crafting_amethyst_pickaxe", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("amethyst_pickaxe")))
        .build(advancementOf("amethyst_pickaxe"));

    public static final AdvancementHolder topaz = Advancement.Builder.advancement()
        .parent(amethystPickaxe)
        .display(
            ModItems.TOPAZ,
            Component.translatable("advancements.anvilcraft.topaz.title"),
            Component.translatable("advancements.anvilcraft.topaz.description"),
            null, AdvancementType.GOAL,
            true, true, false
        )
        .addCriterion("use_topaz", UseItemTrigger.TriggerInstance.useItem(ModItems.TOPAZ.get()))
        .build(advancementOf("topaz"));

    public static final AdvancementHolder liftingAnvil = Advancement.Builder.advancement()
        .parent(topaz)
        .display(
            ModBlocks.MAGNET_BLOCK,
            Component.translatable("advancements.anvilcraft.lifting_anvil.title"),
            Component.translatable("advancements.anvilcraft.lifting_anvil.description"),
            null, AdvancementType.TASK,
            true, true, false
        )
        .addCriterion("lifting_anvil", MagnetLiftingAnvilTrigger.TriggerInstance.liftingAnvil())
        .addCriterion("anvil_on_ground", AnvilOnGroundTrigger.TriggerInstance.onGround())
        .build(advancementOf("lifting_anvil"));
}
