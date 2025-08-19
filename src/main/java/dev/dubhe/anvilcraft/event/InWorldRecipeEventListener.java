package dev.dubhe.anvilcraft.event;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.event.InWorldRecipeEvent;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.util.TriggerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class InWorldRecipeEventListener {
    @SubscribeEvent
    public static void inWorldRecipe(InWorldRecipeEvent event) {
        RecipeType<? extends InWorldRecipe> recipeType = event.getRecipeType();
        ResourceLocation id = event.getId();
        InWorldRecipeContext context = event.getContext();
        ServerLevel level = context.getLevel();
        BlockPos pos = BlockPos.containing(context.getPos());
        TriggerUtil.anythingAnvilCrafting(level, pos);
        AnvilCraft.LOGGER.debug("type: {}", recipeType);
        AnvilCraft.LOGGER.debug("id: {}", id);
        TriggerUtil.inWorldRecipe(level, pos, id);
    }
}
