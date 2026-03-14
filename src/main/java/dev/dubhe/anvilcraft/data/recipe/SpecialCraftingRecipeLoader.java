package dev.dubhe.anvilcraft.data.recipe;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.CanningFoodRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;

public class SpecialCraftingRecipeLoader {

    public static void init(RegistrumRecipeProvider provider) {
        SpecialRecipeBuilder.special(CanningFoodRecipe::new)
            .save(provider, AnvilCraft.of("canned_food"));
    }
}
