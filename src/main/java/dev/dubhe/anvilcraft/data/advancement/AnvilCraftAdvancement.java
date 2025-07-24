package dev.dubhe.anvilcraft.data.advancement;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilCraftingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHandleBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHandleItemTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHurtIronGolemTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilOnLandTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MagnetLiftingAnvilTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.RedstoneMilkerTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.RepairIronGolemTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.UseItemTrigger;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModLootTables;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static dev.dubhe.anvilcraft.AnvilCraft.advancementOf;
import static dev.dubhe.anvilcraft.AnvilCraft.of;

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
            .addCriterion("get_crab_claw", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CRAB_CLAW))
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
            .addCriterion("place_block", PlacerPlaceBlockTrigger.TriggerInstance.placeBlock(ModBlocks.BLOCK_PLACER.get()))
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
            .addCriterion("devour_block", DevourerDevourBlockTrigger.TriggerInstance.devourBlock(ModBlocks.BLOCK_DEVOURER.get()))
            .build(advancementOf("devourer"));

        AdvancementHolder geode = Advancement.Builder.advancement()
            .parent(root)
            .display(
                ModItems.GEODE,
                Component.translatable("advancements.anvilcraft.geode.title"),
                Component.translatable("advancements.anvilcraft.geode.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("use_geode", UseItemTrigger.TriggerInstance.useItem(ModItems.GEODE.asStack()))
            .build(advancementOf("geode"));

        AdvancementHolder amethystPickaxe = Advancement.Builder.advancement()
            .parent(geode)
            .display(
                ModItems.AMETHYST_PICKAXE,
                Component.translatable("advancements.anvilcraft.amethyst_pickaxe.title"),
                Component.translatable("advancements.anvilcraft.amethyst_pickaxe.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("amethyst_pickaxe", RecipeCraftedTrigger.TriggerInstance.craftedItem(of("amethyst_pickaxe")))
            .build(advancementOf("amethyst_pickaxe"));

        AdvancementHolder topaz = Advancement.Builder.advancement()
            .parent(amethystPickaxe)
            .display(
                ModItems.TOPAZ,
                Component.translatable("advancements.anvilcraft.topaz.title"),
                Component.translatable("advancements.anvilcraft.topaz.description"),
                null, AdvancementType.GOAL,
                true, true, false
            )
            .addCriterion("use_topaz", UseItemTrigger.TriggerInstance.useItem(ModItems.TOPAZ.asStack()))
            .build(advancementOf("topaz"));

        AdvancementHolder liftingAnvil = Advancement.Builder.advancement()
            .parent(topaz)
            .display(
                ModBlocks.MAGNET_BLOCK.asItem(),
                Component.translatable("advancements.anvilcraft.lifting_anvil.title"),
                Component.translatable("advancements.anvilcraft.lifting_anvil.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("lifting_anvil", MagnetLiftingAnvilTrigger.TriggerInstance.liftingAnvil())
            .addCriterion("anvil_on_land", AnvilOnLandTrigger.TriggerInstance.anvilOnLand())
            .build(advancementOf("lifting_anvil"));

        AdvancementHolder dang = Advancement.Builder.advancement()
            .parent(root)
            .display(
                Items.ANVIL,
                Component.translatable("advancements.anvilcraft.dang.title"),
                Component.translatable("advancements.anvilcraft.dang.description"),
                null, AdvancementType.GOAL,
                true, true, false
            )
            .addCriterion("anvil_crafting", AnvilCraftingTrigger.TriggerInstance.anvilCrafting())
            .build(advancementOf("dang"));

        AdvancementHolder stoneCrusher = Advancement.Builder.advancement()
            .parent(dang)
            .display(
                Items.SAND,
                Component.translatable("advancements.anvilcraft.stone_crusher.title"),
                Component.translatable("advancements.anvilcraft.stone_crusher.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("cobblestone", AnvilHandleBlockTrigger.TriggerInstance.handleBlock(Blocks.GRAVEL))
            .addCriterion("gravel", AnvilHandleBlockTrigger.TriggerInstance.handleBlock(Blocks.SAND))
            .build(advancementOf("stone_crusher"));

        AdvancementHolder fossick = Advancement.Builder.advancement()
            .parent(stoneCrusher)
            .display(
                Items.GOLD_NUGGET,
                Component.translatable("advancements.anvilcraft.fossick.title"),
                Component.translatable("advancements.anvilcraft.fossick.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("gold_nugget", AnvilHandleItemTrigger.TriggerInstance.handleItem(Items.GOLD_NUGGET.getDefaultInstance()))
            .build(advancementOf("fossick"));

        AdvancementHolder iceMaker = Advancement.Builder.advancement()
            .parent(dang)
            .display(
                Items.ICE,
                Component.translatable("advancements.anvilcraft.ice_maker.title"),
                Component.translatable("advancements.anvilcraft.ice_maker.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("ice", AnvilHandleBlockTrigger.TriggerInstance.handleBlock(Blocks.ICE))
            .build(advancementOf("ice_maker"));

        AdvancementHolder four281 = Advancement.Builder.advancement()
            .parent(iceMaker)
            .display(
                Items.BLUE_ICE,
                Component.translatable("advancements.anvilcraft.four281.title"),
                Component.translatable("advancements.anvilcraft.four281.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("packed_ice", AnvilHandleBlockTrigger.TriggerInstance.handleBlock(Blocks.PACKED_ICE))
            .addCriterion("blue_ice", AnvilHandleBlockTrigger.TriggerInstance.handleBlock(Blocks.BLUE_ICE))
            .build(advancementOf("four281"));



        AdvancementHolder redstoneMilker = Advancement.Builder.advancement()
            .parent(root)
            .display(
                Items.DISPENSER,
                Component.translatable("advancements.anvilcraft.redstone_milker.title"),
                Component.translatable("advancements.anvilcraft.redstone_milker.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("milk", RedstoneMilkerTrigger.TriggerInstance.milk())
            .build(advancementOf("redstone_milker"));

        AdvancementHolder realLooting = Advancement.Builder.advancement()
            .parent(redstoneMilker)
            .display(
                Items.DISPENSER,
                Component.translatable("advancements.anvilcraft.real_looting.title"),
                Component.translatable("advancements.anvilcraft.real_looting.description"),
                null, AdvancementType.TASK,
                true, true, false
            )
            .addCriterion("anvil_looting", AnvilLootingTrigger.TriggerInstance.looting())
            .build(advancementOf("real_looting"));

        AdvancementHolder ironMeterReversal = Advancement.Builder.advancement()
            .parent(realLooting)
            .display(
                Items.IRON_BLOCK,
                Component.translatable("advancements.anvilcraft.iron_meter_reversal.title"),
                Component.translatable("advancements.anvilcraft.iron_meter_reversal.description"),
                null, AdvancementType.GOAL,
                true, true, false
            )
            .addCriterion("anvil_hurt_iron_golem", AnvilHurtIronGolemTrigger.TriggerInstance.hurt())
            .addCriterion("repair_iron_golem", RepairIronGolemTrigger.TriggerInstance.repair())
            .build(advancementOf("iron_meter_reversal"));

        provider.accept(root);
        provider.accept(crabClaw);
        provider.accept(placer);
        provider.accept(devourer);
        provider.accept(geode);
        provider.accept(amethystPickaxe);
        provider.accept(topaz);
        provider.accept(liftingAnvil);
        provider.accept(dang);
        provider.accept(stoneCrusher);
        provider.accept(fossick);
        provider.accept(iceMaker);
        provider.accept(four281);

        provider.accept(redstoneMilker);
        provider.accept(realLooting);
        provider.accept(ironMeterReversal);
    }
}
