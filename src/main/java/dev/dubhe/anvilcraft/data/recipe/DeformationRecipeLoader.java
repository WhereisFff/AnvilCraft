package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.frost.DeformationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class DeformationRecipeLoader {
    private static final List<String> ARMORS = List.of(
        "helmet",
        "chestplate",
        "leggings",
        "boots"
    );

    public static void init(RegistrateRecipeProvider provider) {
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ResourceLocation.withDefaultNamespace("wood")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ResourceLocation.withDefaultNamespace("stone")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ResourceLocation.withDefaultNamespace("iron")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ResourceLocation.withDefaultNamespace("gold")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ResourceLocation.withDefaultNamespace("diamond")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            ResourceLocation.withDefaultNamespace("netherite")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            AnvilCraft.of("amethyst")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            AnvilCraft.of("royal_steel")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            AnvilCraft.of("frost_metal")
        );
        DeformationRecipeLoader.register(
            provider,
            PermutationRecipeLoader.WEAPONS_AND_TOOLS,
            AnvilCraft.of("ember_metal")
        );

        DeformationRecipeLoader.register(
            provider,
            DeformationRecipeLoader.ARMORS,
            ResourceLocation.withDefaultNamespace("chainmail")
        );
        DeformationRecipeLoader.register(
            provider,
            DeformationRecipeLoader.ARMORS,
            ResourceLocation.withDefaultNamespace("iron")
        );
        DeformationRecipeLoader.register(
            provider,
            DeformationRecipeLoader.ARMORS,
            ResourceLocation.withDefaultNamespace("gold")
        );
        DeformationRecipeLoader.register(
            provider,
            DeformationRecipeLoader.ARMORS,
            ResourceLocation.withDefaultNamespace("diamond")
        );
        DeformationRecipeLoader.register(
            provider,
            DeformationRecipeLoader.ARMORS,
            ResourceLocation.withDefaultNamespace("netherite")
        );

        DeformationRecipe.builder()
            .input(Items.BOW)
            .input(Items.CROSSBOW)
            .save(provider, "bowlikes");
    }

    private static void register(
        RegistrateRecipeProvider provider,
        List<String> bases,
        ResourceLocation id
    ) {
        var builder = DeformationRecipe.builder();
        for (String base : bases) {
            Item input = BuiltInRegistries.ITEM.get(id.withSuffix("_" + base));
            builder.input(input);
        }
        builder.save(provider, id);
    }
}
