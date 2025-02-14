package dev.dubhe.anvilcraft.recipe.neo;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModRegistries;
import dev.dubhe.anvilcraft.util.mixin.recipe.InWorldRecipeManagerInjector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class InWorldRecipeManager {
    private final Map<RecipeTrigger, Set<InWorldRecipe>> recipes = new HashMap<>();

    public @NotNull Set<InWorldRecipe> getRecipes(@NotNull RecipeTrigger recipeTrigger) {
        return recipes.getOrDefault(recipeTrigger, Set.of());
    }

    public void trigger(RecipeTrigger recipeTrigger, InWorldRecipeContext context) {
        Set<InWorldRecipe> recipes = getRecipes(recipeTrigger);
        for (InWorldRecipe recipe : recipes) {
            int efficiency = AnvilCraft.config.anvilEfficiency;
            while (efficiency > 0 && recipe.matches(context, context.getLevel())) {
                efficiency--;
                recipe.assemble(context, context.getLevel().registryAccess());
            }
            if (efficiency != AnvilCraft.config.anvilEfficiency) break;
        }
    }

    public void addRecipe(@NotNull InWorldRecipe recipe) {
        recipes.computeIfAbsent(recipe.getTrigger(), k -> new TreeSet<>()).add(recipe);
    }

    @SubscribeEvent
    public static void onNewRegistry(@NotNull NewRegistryEvent event) {
        event.register(ModRegistries.BuiltIn.RECIPE_TRIGGER);
        event.register(ModRegistries.BuiltIn.RECIPE_PREDICATE);
        event.register(ModRegistries.BuiltIn.RECIPE_OUTCOME);
    }

    @SubscribeEvent
    public static void onRecipesUpdated(@NotNull RecipesUpdatedEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        if (!(server instanceof InWorldRecipeManagerInjector injector)) return;
        RecipeManager manager = event.getRecipeManager();
        InWorldRecipeManager inWorldRecipeManager = injector.anvilcraft$getInWorldRecipeManager();
        for (RecipeHolder<?> recipe : manager.getRecipes()) {
            Recipe<?> value = recipe.value();
            if (value instanceof InWorldRecipe inWorldRecipe) {
                inWorldRecipeManager.addRecipe(inWorldRecipe);
            }
        }
    }
}
