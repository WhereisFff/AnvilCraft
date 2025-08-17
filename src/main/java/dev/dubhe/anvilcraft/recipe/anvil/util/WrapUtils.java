package dev.dubhe.anvilcraft.recipe.anvil.util;

import dev.dubhe.anvilcraft.recipe.anvil.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasBlockIngredient;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceBlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class WrapUtils {
    public static @NotNull List<IRecipePredicate<?>> getPredicates(
        @NotNull List<BlockStatePredicate> results
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            BlockStatePredicate result = results.get(i);
            predicates.add(new HasBlock(new Vec3(0, -i - 1, 0), result));
        }
        return predicates;
    }

    public static @NotNull IRecipePredicate<?> getIngredientPredicate(
        @NotNull BlockStatePredicate block
    ) {
        return new HasBlockIngredient(new Vec3(0, -1, 0), block);
    }

    public static @NotNull @Unmodifiable List<IRecipePredicate<?>> getIngredientPredicates(
        @NotNull BlockStatePredicate block
    ) {
        return List.of(getIngredientPredicate(block));
    }

    public static @NotNull List<IRecipePredicate<?>> getIngredientPredicates(
        @NotNull List<BlockStatePredicate> results
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            BlockStatePredicate result = results.get(i);
            predicates.add(new HasBlockIngredient(new Vec3(0, -i - 1, 0), result));
        }
        return predicates;
    }

    public static @NotNull @Unmodifiable List<IRecipeOutcome<?>> getOutcomes(
        @NotNull ChanceBlockState result,
        int yOffset
    ) {
        return List.of(result.toSetBlock(new Vec3(0, yOffset, 0)));
    }

    public static @NotNull @Unmodifiable List<IRecipeOutcome<?>> getOutcomes(
        @NotNull ChanceBlockState result
    ) {
        return WrapUtils.getOutcomes(result, -1);
    }

    public static @NotNull List<IRecipeOutcome<?>> getOutcomes(
        @NotNull List<ChanceBlockState> results
    ) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ChanceBlockState result = results.get(i);
            outcomes.add(result.toSetBlock(new Vec3(0, -i - 1, 0)));
        }
        return outcomes;
    }

    public static @NotNull Item getItem(@NotNull ChanceBlockState result) {
        BlockState state = result.getState();
        if (state.isEmpty() || state.isAir()) return Items.ANVIL;
        Item item = state.getBlock().asItem();
        if (item == Items.AIR) item = Items.ANVIL;
        return item;
    }

    public static @NotNull Item getItem(@NotNull List<ChanceBlockState> results) {
        if (results.isEmpty()) return Items.ANVIL;
        return WrapUtils.getItem(results.getFirst());
    }

    public static @NotNull ItemStack getItemStack(@NotNull ChanceBlockState result) {
        return WrapUtils.getItem(result).getDefaultInstance();
    }

    public static @NotNull ItemStack getItemStack(@NotNull List<ChanceBlockState> results) {
        if (results.isEmpty()) return Items.ANVIL.getDefaultInstance();
        return WrapUtils.getItem(results.getFirst()).getDefaultInstance();
    }

    public static @NotNull ResourceLocation cauldron2Fluid(Block cauldron) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(cauldron);
        String namespace = key.getNamespace();
        String path = key.getPath();
        if (path.endsWith("_cauldron")) path = path.substring(0, path.length() - 9);
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
