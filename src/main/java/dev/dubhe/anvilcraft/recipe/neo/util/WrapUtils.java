package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.DataResult;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlockIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        @NotNull ChanceBlockState result
    ) {
        return List.of(result.toSetBlock(new Vec3(0, -1, 0)));
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
        return getItem(results.getFirst());
    }

    public static @NotNull ItemStack getItemStack(@NotNull ChanceBlockState result) {
        return getItem(result).getDefaultInstance();
    }

    public static @NotNull ItemStack getItemStack(@NotNull List<ChanceBlockState> results) {
        if (results.isEmpty()) return Items.ANVIL.getDefaultInstance();
        return getItem(results.getFirst()).getDefaultInstance();
    }

    public static void encodeIngredients(@NotNull RegistryFriendlyByteBuf buf, @NotNull List<ItemIngredientPredicate> ingredients) {
        buf.writeVarInt(ingredients.size());
        for (ItemIngredientPredicate itemIngredient : ingredients) {
            RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())).createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> encode = ItemIngredientPredicate.CODEC.encode(itemIngredient, ops, ops.empty());
            Tag tag = encode.getOrThrow();
            buf.writeNbt(tag);
        }
    }

    public static void encodeResults(@NotNull RegistryFriendlyByteBuf buf, @NotNull List<ChanceItemStack> results) {
        buf.writeVarInt(results.size());
        for (ChanceItemStack result : results) {
            ChanceItemStack.STREAM_CODEC.encode(buf, result);
        }
    }

    public static @NotNull List<ItemIngredientPredicate> decodeIngredients(@NotNull RegistryFriendlyByteBuf buf) {
        int i = buf.readVarInt();
        List<ItemIngredientPredicate> ingredients = new ArrayList<>();
        for (; i > 0; i--) {
            RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())).createSerializationContext(NbtOps.INSTANCE);
            ingredients.add(ItemIngredientPredicate.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst());
        }
        return ingredients;
    }

    public static @NotNull List<ChanceItemStack> decodeResults(@NotNull RegistryFriendlyByteBuf buf) {
        int i = buf.readVarInt();
        List<ChanceItemStack> results = new ArrayList<>();
        for (; i > 0; i--) {
            results.add(ChanceItemStack.STREAM_CODEC.decode(buf));
        }
        return results;
    }
}
