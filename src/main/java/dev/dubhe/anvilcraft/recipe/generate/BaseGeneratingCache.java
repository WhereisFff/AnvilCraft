package dev.dubhe.anvilcraft.recipe.generate;

import com.google.common.collect.Lists;
import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseGeneratingCache<T extends Recipe<?>> {
    protected static final List<CacheFactory> FACTORIES = new ArrayList<>();

    public static List<BaseGeneratingCache<?>> buildCaches(HolderLookup.Provider registries) {
        FACTORIES.add(JewelCraftingRecipeGeneratingCache::new);
        FACTORIES.add(MeshRecipeGeneratingCache::new);
        return Lists.transform(FACTORIES, factory -> factory.buildCache(registries));
    }

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    protected final HolderLookup.Provider registries;
    protected final String recipeId;
    protected final String recipeName;

    protected BaseGeneratingCache(HolderLookup.Provider registries, String recipeId, String recipeName) {
        this.registries = registries;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        logger().debug("Initializing {} building cache", recipeName);
    }

    protected static Logger logger() {
        return LoggerFactory.getLogger(STACK_WALKER.getCallerClass());
    }

    protected ResourceLocation generateRecipeId(String type, Item recipeInput, Item recipeResult) {
        ResourceLocation inputId = BuiltInRegistries.ITEM.getKey(recipeInput);
        ResourceLocation resultId = BuiltInRegistries.ITEM.getKey(recipeResult);
        logger().debug("Generating {} for {}", this.recipeName, resultId);
        ResourceLocation newId = AnvilCraft.of("%s/%s_from_%s_for_%s".formatted(
            this.recipeId, resultId.toString().replace(':', '_'), inputId.toString().replace(':', '_'), type));
        logger().debug("The generated recipe id is {}", newId);
        return newId;
    }

    public abstract RecipeType<T> getType();

    public abstract Optional<List<RecipeHolder<T>>> buildRecipes();

    @FunctionalInterface
    public interface CacheFactory {
        BaseGeneratingCache<?> buildCache(HolderLookup.Provider registries);
    }
}
