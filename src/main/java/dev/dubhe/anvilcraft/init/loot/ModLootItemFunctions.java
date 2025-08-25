package dev.dubhe.anvilcraft.init.loot;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.loot.functions.CurseLootItemFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModLootItemFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
        DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, AnvilCraft.MOD_ID);

    public static final Supplier<LootItemFunctionType<CurseLootItemFunction>> CURSE_LOOT =
        LOOT_FUNCTION_TYPES.register("curse_loot", () -> new LootItemFunctionType<>(CurseLootItemFunction.CODEC));
}
