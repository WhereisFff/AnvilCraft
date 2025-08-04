package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.recipe.neo.wrap.MeshRecipe;
import net.minecraft.world.item.Items;

public class MeshRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        MeshRecipe.builder()
            .requires(Items.GRAVEL)
            .result(Items.GRAVEL, 0.5)
            .result(Items.FLINT, 0.25)
            .result(Items.IRON_NUGGET, 0.2)
            .save(provider);

        MeshRecipe.builder()
            .requires(Items.SAND)
            .result(Items.SAND, 0.5)
            .result(Items.CLAY_BALL, 0.25)
            .result(Items.GOLD_NUGGET, 0.05)
            .save(provider);

        MeshRecipe.builder()
            .requires(Items.RED_SAND)
            .result(Items.RED_SAND, 0.5)
            .result(Items.GLOWSTONE_DUST, 0.1)
            .result(ModItems.COPPER_NUGGET, 0.2)
            .save(provider);

        MeshRecipe.builder()
            .requires(Items.SOUL_SAND)
            .result(Items.SOUL_SAND, 0.5)
            .result(Items.NETHER_WART, 0.005)
            .save(provider);

        MeshRecipe.builder()
            .requires(ModBlocks.NETHER_DUST)
            .result(ModBlocks.NETHER_DUST, 0.5)
            .result(Items.REDSTONE, 0.1)
            .result(ModItems.TUNGSTEN_NUGGET, 0.1)
            .save(provider);

        MeshRecipe.builder()
            .requires(ModBlocks.END_DUST)
            .result(ModBlocks.END_DUST, 0.5)
            .result(Items.CHORUS_FLOWER, 0.005)
            .result(ModItems.TITANIUM_NUGGET, 0.1)
            .result(ModItems.LEVITATION_POWDER, 0.1)
            .save(provider);

        MeshRecipe.builder()
            .requires(ModBlocks.CINERITE)
            .result(ModBlocks.CINERITE, 0.5)
            .result(Items.LAPIS_LAZULI, 0.1)
            .result(Items.GUNPOWDER, 0.1)
            .result(ModItems.ZINC_NUGGET, 0.1)
            .result(ModItems.LEAD_NUGGET, 0.1)
            .result(ModItems.TIN_NUGGET, 0.1)
            .result(ModItems.SILVER_NUGGET, 0.1)
            .save(provider);

        MeshRecipe.builder()
            .requires(ModBlocks.QUARTZ_SAND)
            .result(ModBlocks.QUARTZ_SAND, 0.5)
            .result(Items.QUARTZ)
            .save(provider);
    }
}
