package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHammerHurtEntityTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHammerClickBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilHitPiezoelectricCrystalTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingIronGolemTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilOnGroundTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.ConvertBeaconTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DispenserRepairIronGolem;
import dev.dubhe.anvilcraft.advancements.criteron.InWorldRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MagnetLiftingAnvilTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MilkTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlayerKilledEntityByAnvilHammerTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlayerWearAnvilHammerTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.UseItemTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCriterionTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> CT =
        DeferredRegister.create(Registries.TRIGGER_TYPE, AnvilCraft.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, PlacerPlaceTrigger> PLACER_PLACE_BLOCK =
        CT.register("placer_place_block", PlacerPlaceTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, DevourerDevourTrigger> DEVOURER_DEVOUR_BLOCK =
        CT.register("devourer_devour_block", DevourerDevourTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, UseItemTrigger> USE_ITEM =
        CT.register("use_item", UseItemTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MagnetLiftingAnvilTrigger> LIFTING_ANVIL =
        CT.register("lifting_anvil", MagnetLiftingAnvilTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilOnGroundTrigger> ANVIL_ON_GROUND =
        CT.register("anvil_on_ground", AnvilOnGroundTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MilkTrigger> MILK =
        CT.register("milk", MilkTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilLootingTrigger> ANVIL_LOOTING =
        CT.register("anvil_looting", AnvilLootingTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilLootingIronGolemTrigger> ANVIL_LOOTING_IRON_GOLEM =
        CT.register("anvil_looting_iron_golem", AnvilLootingIronGolemTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, DispenserRepairIronGolem> REPAIR_IRON_GOLEM =
        CT.register("repair_iron_golem", DispenserRepairIronGolem::new);

    public static final DeferredHolder<CriterionTrigger<?>, InWorldRecipeTrigger> IN_WORLD_RECIPE =
        CT.register("in_world_recipe", InWorldRecipeTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilHammerClickBlockTrigger> ANVIL_HAMMER_CLICK_BLOCK =
        CT.register("anvil_hammer_click_block", AnvilHammerClickBlockTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilHammerHurtEntityTrigger> ANVIL_HAMMER_HURT_ENTITY =
        CT.register("anvil_hammer_hurt_entity", AnvilHammerHurtEntityTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, PlayerKilledEntityByAnvilHammerTrigger> PLAYER_KILLED_ENTITY_BY_ANVIL_HAMMER =
        CT.register("player_killed_entity_by_anvil_hammer", PlayerKilledEntityByAnvilHammerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilHitPiezoelectricCrystalTrigger> ANVIL_HIT_PIEZOELECTRIC_CRYSTAL =
        CT.register("anvil_hit_piezoelectric_crystal", AnvilHitPiezoelectricCrystalTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, PlayerWearAnvilHammerTrigger> PLAYER_WEAR_ANVIL_HAMMER =
        CT.register("player_wear_anvil_hammer", PlayerWearAnvilHammerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, ConvertBeaconTrigger> CONVERT_BEACON =
        CT.register("convert_beacon", ConvertBeaconTrigger::new);

    public static void register(IEventBus eventBus) {
        CT.register(eventBus);
    }
}
