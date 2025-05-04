package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.recipe.anvil.collision.AnvilCollisionCraftRecipe;
import net.minecraft.tags.BlockTags;

public class AnvilCollisionCraftRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        AnvilCollisionCraftRecipe.builder()
                .anvil(BlockTags.ANVIL)
                .hitBlock(ModBlocks.NEGATIVE_MATTER_BLOCK.get())
                .outputItem(ModBlocks.VOID_MATTER_BLOCK.asItem(), 8)
                .save(provider);
    }
}