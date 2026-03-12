package dev.dubhe.anvilcraft.data.recipe;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.PillRecipe;
import net.minecraft.data.recipes.SpecialRecipeBuilder;

public class PillRecipeLoader {
    public static void init(RegistrumRecipeProvider provider) {
        SpecialRecipeBuilder.special(PillRecipe::new)
            .save(provider, AnvilCraft.of("pill"));
    }
}
