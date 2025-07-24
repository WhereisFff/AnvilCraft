package dev.dubhe.anvilcraft.init;

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
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCriterionTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> CT =
        DeferredRegister.create(Registries.TRIGGER_TYPE, AnvilCraft.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, UseItemTrigger> USE_ITEM =
        CT.register("use_item", UseItemTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, PlacerPlaceBlockTrigger> PLACER_PLACE_BLOCK =
        CT.register("placer_place_block", PlacerPlaceBlockTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, DevourerDevourBlockTrigger> DEVOURER_DEVOUR_BLOCK =
        CT.register("devourer_devour_block", DevourerDevourBlockTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MagnetLiftingAnvilTrigger> MAGNET_LIFTING_ANVIL =
        CT.register("magnet_lifting_anvil", MagnetLiftingAnvilTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilOnLandTrigger> ANVIL_ON_LAND =
        CT.register("anvil_on_land", AnvilOnLandTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilCraftingTrigger> ANVIL_CRAFTING =
        CT.register("anvil_crafting", AnvilCraftingTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilHandleBlockTrigger> ANVIL_HANDLE_BLOCK =
        CT.register("anvil_handle_block", AnvilHandleBlockTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilHandleItemTrigger> ANVIL_HANDLE_ITEM =
        CT.register("anvil_handle_item", AnvilHandleItemTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, RedstoneMilkerTrigger> REDSTONE_MILKER =
        CT.register("redstone_milker", RedstoneMilkerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilLootingTrigger> ANVIL_LOOTING =
        CT.register("anvil_looting", AnvilLootingTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilHurtIronGolemTrigger> ANVIL_HURT_IRON_GOLEM =
        CT.register("anvil_hurt_iron_golem", AnvilHurtIronGolemTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, RepairIronGolemTrigger> REPAIR_IRON_GOLEM =
        CT.register("repair_iron_golem", RepairIronGolemTrigger::new);

    public static void register(IEventBus eventBus) {
        CT.register(eventBus);
    }
}
