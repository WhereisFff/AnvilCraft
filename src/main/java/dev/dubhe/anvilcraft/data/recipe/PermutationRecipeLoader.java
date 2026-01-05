package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.recipe.frost.PermutationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class PermutationRecipeLoader {
    static final List<String> WEAPONS_AND_TOOLS = List.of(
        "sword",
        "axe",
        "pickaxe",
        "shovel",
        "hoe"
    );
    static final List<String> ANC_WEAPONS_AND_TOOLS = List.of(
        "heavy_halberd",
        "resonator"
    );
    private static final List<String> WORKSTATIONS = List.of(
        "anvil",
        "grindstone",
        "smithing_table"
    );

    public static void init(RegistrateRecipeProvider provider) {
        PermutationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ModItems.ROYAL_STEEL_INGOT,
            ResourceLocation.withDefaultNamespace("diamond"),
            AnvilCraft.of("royal_steel")
        );

        PermutationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ModItems.EMBER_METAL_INGOT,
            ResourceLocation.withDefaultNamespace("netherite"),
            AnvilCraft.of("ember_metal")
        );

        PermutationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ModItems.MULTIPHASE_MATTER,
            AnvilCraft.of("frost_metal"),
            AnvilCraft.of("ember_metal")
        );
        PermutationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.ANC_WEAPONS_AND_TOOLS,
            ModItems.MULTIPHASE_MATTER,
            AnvilCraft.of("frost_metal"),
            AnvilCraft.of("ember_metal")
        );

        PermutationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WORKSTATIONS,
            ModBlocks.MULTIPHASE_MATTER_BLOCK,
            AnvilCraft.of("frost_metal"),
            AnvilCraft.of("ember_metal")
        );
    }

    private static void register(
        RegistrateRecipeProvider provider,
        List<String> bases,
        ItemLike material,
        ResourceLocation idA,
        ResourceLocation idB
    ) {
        for (String base : bases) {
            Item inputA = BuiltInRegistries.ITEM.get(idA.withSuffix("_" + base));
            Item inputB = BuiltInRegistries.ITEM.get(idB.withSuffix("_" + base));
            PermutationRecipe.builder(inputA, inputB)
                .material(material)
                .save(provider);
        }
    }
}
