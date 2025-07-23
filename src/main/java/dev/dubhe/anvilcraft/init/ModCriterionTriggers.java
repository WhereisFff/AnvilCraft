package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourBlockTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceBlockTrigger;
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

    public static void register(IEventBus eventBus) {
        CT.register(eventBus);
    }
}
