package dev.dubhe.anvilcraft.recipe.neo.util;

import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SetBlock;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class WrapUtils {
    public static @NotNull IRecipePredicate<?> getPredicate(
        @NotNull BlockStatePredicate block
    ) {
        return new HasBlock(new Vec3(0, -1, 0), block);
    }

    public static @NotNull @Unmodifiable List<IRecipePredicate<?>> getPredicates(
        @NotNull BlockStatePredicate block
    ) {
        return List.of(getPredicate(block));
    }

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

    public static @NotNull IRecipeOutcome<?> getOutcome(
        @NotNull ChanceBlockState result
    ) {
        return new SetBlock(result.getState(), new Vec3(0, -1, 0), 1);
    }

    public static @NotNull @Unmodifiable List<IRecipeOutcome<?>> getOutcomes(
        @NotNull ChanceBlockState result
    ) {
        return List.of(getOutcome(result));
    }

    public static @NotNull List<IRecipeOutcome<?>> getOutcomes(
        @NotNull List<ChanceBlockState> results
    ) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ChanceBlockState result = results.get(i);
            outcomes.add(new SetBlock(result.getState(), new Vec3(0, -i - 1, 0), 1));
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
        return getItem(results.getFirst());
    }

    public static @NotNull ItemStack getItemStack(@NotNull ChanceBlockState result) {
        return getItem(result).getDefaultInstance();
    }

    public static @NotNull ItemStack getItemStack(@NotNull List<ChanceBlockState> results) {
        if (results.isEmpty()) return Items.ANVIL.getDefaultInstance();
        return getItem(results.getFirst()).getDefaultInstance();
    }
}
