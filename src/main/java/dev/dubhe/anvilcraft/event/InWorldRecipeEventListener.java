package dev.dubhe.anvilcraft.event;

import dev.anvilcraft.lib.event.InWorldRecipeEvent;
import dev.anvilcraft.lib.event.InWorldRecipeManagerEvent;
import dev.anvilcraft.lib.event.ItemCacheEvent;
import dev.anvilcraft.lib.injection.IRecipeManagerExtension;
import dev.anvilcraft.lib.recipe.InWorldRecipe;
import dev.anvilcraft.lib.recipe.util.InWorldRecipeContext;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.MeshRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.VanillaRecipesWrap;
import dev.dubhe.anvilcraft.recipe.generate.MeshRecipeGeneratingCache;
import dev.dubhe.anvilcraft.util.TriggerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class InWorldRecipeEventListener {
    @SubscribeEvent
    public static void inWorldRecipe(InWorldRecipeManagerEvent.Init event) {
        RecipeManager manager = event.getRecipeManager();
        IRecipeManagerExtension extension = (IRecipeManagerExtension) manager;
        List<RecipeHolder<InWorldRecipe>> init = VanillaRecipesWrap.init(
            extension.anvillib$getRegistries(),
            manager.getRecipes()
        );
        new MeshRecipeGeneratingCache(extension.anvillib$getRegistries())
            .buildRecipes()
            .ifPresent(recipeHolders -> {
                for (RecipeHolder<MeshRecipe> holder : recipeHolders) {
                    init.add(new RecipeHolder<>(holder.id(), holder.value()));
                }
            });
        extension.anvillib$addRecipes(init);
    }

    @SubscribeEvent
    public static void inWorldRecipe(InWorldRecipeEvent event) {
        RecipeType<? extends InWorldRecipe> recipeType = event.getRecipeType();
        ResourceLocation id = event.getId();
        InWorldRecipeContext context = event.getContext();
        ServerLevel level = context.getLevel();
        BlockPos pos = BlockPos.containing(context.getPos());
        AnvilCraft.LOGGER.debug("type: {}", recipeType);
        AnvilCraft.LOGGER.debug("id: {}", id);
        TriggerUtil.anythingAnvilCrafting(level, pos);
        TriggerUtil.inWorldRecipe(level, pos, id);
        if (recipeType == ModRecipeTypes.SUPER_HEATING_TYPE.get()) {
            TriggerUtil.inWorldSuperHeatingRecipe(level, pos, id);
        }
        if (recipeType == ModRecipeTypes.TIME_WARP_TYPE.get()) {
            TriggerUtil.inWorldTimeWrapRecipe(level, pos, id);
        }
    }

    @SubscribeEvent
    public static void spawnItemEntity(ItemCacheEvent.SpawnItemEntity event) {
        event.getEntity().anvilcraft$setIsAdsorbable(false);
    }
}
