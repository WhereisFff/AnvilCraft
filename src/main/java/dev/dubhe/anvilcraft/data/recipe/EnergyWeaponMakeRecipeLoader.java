package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.recipe.EnergyWeaponMakeRecipe;

public class EnergyWeaponMakeRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        EnergyWeaponMakeRecipe.builder()
            .requires(ModBlocks.ACCELERATION_RING, 4)
            .requires(ModBlocks.SLIDING_RAIL, 4)
            .result(ModItems.ANVIL_RAILGUN.asStack())
            .save(provider);
    }
}
