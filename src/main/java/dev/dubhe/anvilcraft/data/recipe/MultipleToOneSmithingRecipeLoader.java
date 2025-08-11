package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.recipe.multiple.EightToOneSmithingRecipe;
import dev.dubhe.anvilcraft.recipe.multiple.FourToOneSmithingRecipe;
import dev.dubhe.anvilcraft.recipe.multiple.TwoToOneSmithingRecipe;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class MultipleToOneSmithingRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        FourToOneSmithingRecipe.builder(ModItems.FROST_METAL_HEAVY_HALBERD, 0)
            .material(ModItems.HEAVY_HALBERD_CORE)
            .input(ModItems.FROST_METAL_SWORD)
            .input(ModItems.FROST_METAL_AXE)
            .input(Items.TRIDENT)
            .input(Tags.Items.TOOLS_MACE)
            .save(provider);
        FourToOneSmithingRecipe.builder(ModItems.EMBER_METAL_HEAVY_HALBERD, 0)
            .material(ModItems.HEAVY_HALBERD_CORE)
            .input(ModItems.EMBER_METAL_SWORD)
            .input(ModItems.EMBER_METAL_AXE)
            .input(Items.TRIDENT)
            .input(Tags.Items.TOOLS_MACE)
            .save(provider);
        TwoToOneSmithingRecipe.builder(ModItems.TRANSCENDENCE_HEAVY_HALBERD, 0)
            .material(ModItems.MULTIPHASE_TRANSCENDIUM)
            .input(ModItems.EMBER_METAL_HEAVY_HALBERD)
            .input(ModItems.FROST_METAL_HEAVY_HALBERD)
            .save(provider);
        FourToOneSmithingRecipe.builder(ModItems.FROST_METAL_RESONATOR, 0)
            .material(ModItems.RESONATOR_CORE)
            .input(ModItems.FROST_METAL_AXE)
            .input(ModItems.FROST_METAL_SHOVEL)
            .input(ModItems.FROST_METAL_HOE)
            .input(ModItems.FROST_METAL_PICKAXE)
            .save(provider);
        FourToOneSmithingRecipe.builder(ModItems.EMBER_METAL_RESONATOR, 0)
            .material(ModItems.RESONATOR_CORE)
            .input(ModItems.EMBER_METAL_AXE)
            .input(ModItems.EMBER_METAL_SHOVEL)
            .input(ModItems.EMBER_METAL_HOE)
            .input(ModItems.EMBER_METAL_PICKAXE)
            .save(provider);
        TwoToOneSmithingRecipe.builder(ModItems.TRANSCENDENCE_RESONATOR, 0)
            .material(ModItems.MULTIPHASE_TRANSCENDIUM)
            .input(ModItems.EMBER_METAL_RESONATOR)
            .input(ModItems.FROST_METAL_RESONATOR)
            .save(provider);
        EightToOneSmithingRecipe.builder(ModItems.MULTITOOL_ITEM, 0)
            .material(ModItems.MULTIPHASE_MATTER)
            .input(Items.SHEARS)
            .input(Items.FLINT_AND_STEEL)
            .input(Items.BRUSH)
            .input(Items.SPYGLASS)
            .input(ModItems.MAGNET)
            .input(Items.FISHING_ROD)
            .input(Items.CARROT_ON_A_STICK)
            .input(Items.WARPED_FUNGUS_ON_A_STICK)
            .save(provider);
    }
}
