package dev.dubhe.anvilcraft.init;

import com.mojang.serialization.Lifecycle;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.RecipeOutcomeType;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicateType;
import dev.dubhe.anvilcraft.recipe.neo.RecipeTrigger;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ModRegistries {
    public static final ResourceKey<Registry<RecipeTrigger>> RECIPE_TRIGGER = ResourceKey.createRegistryKey(AnvilCraft.of("recipe_trigger"));
    public static final ResourceKey<Registry<RecipeOutcomeType<?>>> RECIPE_OUTCOME = ResourceKey.createRegistryKey(AnvilCraft.of("recipe_outcome"));
    public static final ResourceKey<Registry<RecipePredicateType<?>>> RECIPE_PREDICATE = ResourceKey.createRegistryKey(AnvilCraft.of("recipe_predicate"));

    public static class BuiltIn {
        public static final MappedRegistry<RecipeTrigger> RECIPE_TRIGGER = new MappedRegistry<>(
            ModRegistries.RECIPE_TRIGGER, Lifecycle.stable(), false
        );
        public static final MappedRegistry<RecipeOutcomeType<?>> RECIPE_OUTCOME = new MappedRegistry<>(
            ModRegistries.RECIPE_OUTCOME, Lifecycle.stable(), false
        );
        public static final MappedRegistry<RecipePredicateType<?>> RECIPE_PREDICATE = new MappedRegistry<>(
            ModRegistries.RECIPE_PREDICATE, Lifecycle.stable(), false
        );
    }
}
