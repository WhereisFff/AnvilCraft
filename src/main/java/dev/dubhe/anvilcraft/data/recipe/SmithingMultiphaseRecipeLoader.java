package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.data.AnvilCraftDatagen;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.recipe.SmithingMultiphaseRecipe;

public class SmithingMultiphaseRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        SmithingMultiphaseRecipe.builder()
            .input(ModItems.EMBER_METAL_AXE)
            .result(ModItems.MULTIPHASE_MATTER_AXE)
            .unlockedBy(AnvilCraftDatagen.hasItem(ModItems.MULTIPHASE_MATTER), AnvilCraftDatagen.has(ModItems.MULTIPHASE_MATTER))
            .save(provider);
        SmithingMultiphaseRecipe.builder()
            .input(ModItems.EMBER_METAL_HOE)
            .result(ModItems.MULTIPHASE_MATTER_HOE)
            .unlockedBy(AnvilCraftDatagen.hasItem(ModItems.MULTIPHASE_MATTER), AnvilCraftDatagen.has(ModItems.MULTIPHASE_MATTER))
            .save(provider);
        SmithingMultiphaseRecipe.builder()
            .input(ModItems.EMBER_METAL_PICKAXE)
            .result(ModItems.MULTIPHASE_MATTER_PICKAXE)
            .unlockedBy(AnvilCraftDatagen.hasItem(ModItems.MULTIPHASE_MATTER), AnvilCraftDatagen.has(ModItems.MULTIPHASE_MATTER))
            .save(provider);
        SmithingMultiphaseRecipe.builder()
            .input(ModItems.EMBER_METAL_SHOVEL)
            .result(ModItems.MULTIPHASE_MATTER_SHOVEL)
            .unlockedBy(AnvilCraftDatagen.hasItem(ModItems.MULTIPHASE_MATTER), AnvilCraftDatagen.has(ModItems.MULTIPHASE_MATTER))
            .save(provider);
        SmithingMultiphaseRecipe.builder()
            .input(ModItems.EMBER_METAL_SWORD)
            .result(ModItems.MULTIPHASE_MATTER_SWORD)
            .unlockedBy(AnvilCraftDatagen.hasItem(ModItems.MULTIPHASE_MATTER), AnvilCraftDatagen.has(ModItems.MULTIPHASE_MATTER))
            .save(provider);
    }
}
