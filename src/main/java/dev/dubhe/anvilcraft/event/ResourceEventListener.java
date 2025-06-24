package dev.dubhe.anvilcraft.event;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeManager;
import dev.dubhe.anvilcraft.util.injection.IRecipeManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class ResourceEventListener {
    @SubscribeEvent
    public static void onRecipeLoad(@NotNull RecipesUpdatedEvent event) {
        ResourceEventListener.initManager(event.getRecipeManager());
    }

    @SubscribeEvent
    public static void onServerStarted(@NotNull ServerStartedEvent event) {
        ResourceEventListener.initManager(event.getServer().getRecipeManager());
    }

    @SubscribeEvent
    public static void onDatapackSync(@NotNull OnDatapackSyncEvent event) {
        ResourceEventListener.initManager(event.getPlayerList().getServer().getRecipeManager());
    }

    public static void initManager(@NotNull RecipeManager manager) {
        InWorldRecipeManager manager1 = new InWorldRecipeManager();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            Recipe<?> value = holder.value();
            if (!(value instanceof InWorldRecipe recipe)) continue;
            manager1.register(recipe);
        }
        ((IRecipeManager) manager).anc$setInWorldRecipeManager(manager1);
    }
}
