package dev.dubhe.anvilcraft.data.advancement;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import dev.dubhe.anvilcraft.init.ModAdvancements;

public class AnvilCraftAdvancement {
    public static void init(RegistrateAdvancementProvider provider) {
        provider.accept(ModAdvancements.ROOT);
        provider.accept(ModAdvancements.CRAB_CLAW);
        provider.accept(ModAdvancements.PLACER);
        provider.accept(ModAdvancements.DEVOURER);
        provider.accept(ModAdvancements.GEODE);
        provider.accept(ModAdvancements.AMETHYST_PICKAXE);
        provider.accept(ModAdvancements.TOPAZ);
        provider.accept(ModAdvancements.LIFTING_ANVIL);
        provider.accept(ModAdvancements.REDSTONE_MILKER);
        provider.accept(ModAdvancements.REAL_LOOTING);
        provider.accept(ModAdvancements.IRON_METER_REVERSAL);
        provider.accept(ModAdvancements.DANG);
        provider.accept(ModAdvancements.STONE_CRUSHER);
        provider.accept(ModAdvancements.FOSSICK);
        provider.accept(ModAdvancements.ICE_MAKER);
        provider.accept(ModAdvancements._4_TO_81);
        provider.accept(ModAdvancements.VANILLA_IRON_PLATE);
        provider.accept(ModAdvancements.RECYCLING_DIAMONDS);
        provider.accept(ModAdvancements.ALL_IN_ONE);
        provider.accept(ModAdvancements.HAMMER_AND_NAIL);
        provider.accept(ModAdvancements.SUPERKILL);
        provider.accept(ModAdvancements.HERTS_OF_IRON);
        provider.accept(ModAdvancements.NOT_BEACON);
        provider.accept(ModAdvancements.LIGHTER);
        provider.accept(ModAdvancements.NETWORKING);
        provider.accept(ModAdvancements.ELECTRIC_FIELD_RHYTHM);
        provider.accept(ModAdvancements.INDUSTRIAL_GRADE_SMELTING);
        provider.accept(ModAdvancements.NOBLE_METAL);
        provider.accept(ModAdvancements.OVERSEER);
        provider.accept(ModAdvancements.SMITHING_TABLE);
        provider.accept(ModAdvancements.DURABLE_GOODS);
        provider.accept(ModAdvancements.ROYAL_BLACKSMITH);
        provider.accept(ModAdvancements.WITHER);
        provider.accept(ModAdvancements.RIP_VAN_WINKLE);
    }
}
