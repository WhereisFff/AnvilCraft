package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingIronGolemTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilLootingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnvilOnGroundTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.BlockCompressingRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.BlockCrushingRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.AnythingAnvilCraftingTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DevourerDevourTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.DispenserRepairIronGolem;
import dev.dubhe.anvilcraft.advancements.criteron.MagnetLiftingAnvilTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MeshRecipeTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.MilkTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.PlacerPlaceTrigger;
import dev.dubhe.anvilcraft.advancements.criteron.SqueezingRecipeTrigger;
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

    public static final DeferredHolder<CriterionTrigger<?>, AnythingAnvilCraftingTrigger> ANYTHING_ANVIL_CRAFTING =
        CT.register("anything_anvil_crafting", AnythingAnvilCraftingTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, BlockCrushingRecipeTrigger> BLOCK_CRUSHING =
        CT.register("block_crushing", BlockCrushingRecipeTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MeshRecipeTrigger> MESH =
        CT.register("mesh", MeshRecipeTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, SqueezingRecipeTrigger> SQUEEZING =
        CT.register("squeezing", SqueezingRecipeTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, BlockCompressingRecipeTrigger> BLOCK_COMPRESSING =
        CT.register("block_compressing", BlockCompressingRecipeTrigger::new);

    public static void register(IEventBus eventBus) {
        CT.register(eventBus);
    }
}
