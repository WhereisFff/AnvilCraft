package dev.dubhe.anvilcraft.data.advancement;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModLootTables;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;

public class AnvilCraftAdvancement {
    public static final  AdvancementHolder root = Advancement.Builder.advancement()
        .display(
            Items.ANVIL,
            Component.translatable("advancements.anvilcraft.root.title"),
            Component.translatable("advancements.anvilcraft.root.description"),
            AnvilCraft.of("textures/gui/advancements/background.png"),
            AdvancementType.TASK,
            false, true, false
        )
        .addCriterion("join", PlayerTrigger.TriggerInstance.tick())
        .rewards(AdvancementRewards.Builder.loot(ModLootTables.ADVANCEMENT_ROOT))
        .build(advancementOf("root"));

    public static void init(RegistrateAdvancementProvider provider) {
        provider.accept(root);
        provider.accept(CrabClawLine.crbClaw);
        provider.accept(CrabClawLine.placer);
        provider.accept(CrabClawLine.devourer);
        provider.accept(GeodeLine.geode);
        provider.accept(GeodeLine.amethystPickaxe);
        provider.accept(GeodeLine.topaz);
        provider.accept(GeodeLine.liftingAnvil);
    }
}
