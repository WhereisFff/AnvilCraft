package dev.dubhe.anvilcraft.data.recipe;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.recipe.EnergyWeaponMakeRecipe;

public class EnergyWeaponMakeRecipeLoader {
    public static void init(RegistrumRecipeProvider provider) {
        EnergyWeaponMakeRecipe.builder()
            .requires(ModBlocks.ACCELERATION_RING, 4)
            .requires(ModBlocks.SLIDING_RAIL, 4)
            .result(ModItems.ANVIL_RAILGUN.asStack())
            .save(provider);
    }
}
