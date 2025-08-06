package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilOnGroundTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MagnetLiftingAnvilTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceTrigger;
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

    public static void register(IEventBus eventBus) {
        CT.register(eventBus);
    }
}
