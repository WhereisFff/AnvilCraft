package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SetBlock;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceBlockState;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class BlockCompressRecipe extends InWorldRecipe {
    private final List<BlockStatePredicate> inputs;
    private final List<ChanceBlockState> results;

    public BlockCompressRecipe(
        List<BlockStatePredicate> inputs,
        List<ChanceBlockState> results
    ) {
        super(
            BlockCompressRecipe.getIcon(results),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            BlockCompressRecipe.getPredicates(inputs),
            List.of(),
            BlockCompressRecipe.getOutcomes(results),
            0,
            true
        );
        this.inputs = inputs;
        this.results = results;
    }

    private static @NotNull List<IRecipePredicate<?>> getPredicates(
        @NotNull List<BlockStatePredicate> blocks
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            BlockStatePredicate block = blocks.get(i);
            predicates.add(new HasBlock(new Vec3(0, -i - 1, 0), block));
        }
        return predicates;
    }

    private static @NotNull List<IRecipeOutcome<?>> getOutcomes(
        @NotNull List<ChanceBlockState> results
    ) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            ChanceBlockState result = results.get(i);
            outcomes.add(new SetBlock(result.getState(), new Vec3(0, -i - 1, 0), 1));
        }
        return outcomes;
    }

    private static @NotNull ItemStack getIcon(@NotNull List<ChanceBlockState> results) {
        if (results.isEmpty()) return Items.ANVIL.getDefaultInstance();
        Item item = results.getFirst().getState().getBlock().asItem();
        if (item == Items.AIR) item = Items.ANVIL;
        return item.getDefaultInstance();
    }

    @Override
    public @NotNull RecipeSerializer<BlockCompressRecipe> getSerializer() {
        return ModRecipeTypes.BLOCK_COMPRESS_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<BlockCompressRecipe> getType() {
        return ModRecipeTypes.BLOCK_COMPRESS_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer implements RecipeSerializer<BlockCompressRecipe> {
        private static final MapCodec<BlockCompressRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStatePredicate.CODEC
                .listOf()
                .fieldOf("inputs")
                .forGetter(BlockCompressRecipe::getInputs),
            ChanceBlockState.CODEC.codec()
                .listOf()
                .fieldOf("results")
                .forGetter(BlockCompressRecipe::getResults)
        ).apply(instance, BlockCompressRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BlockCompressRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::encode,
            Serializer::decode
        );

        @Override
        public @NotNull MapCodec<BlockCompressRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockCompressRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }

        private static void encode(
            @NotNull RegistryFriendlyByteBuf buf,
            @NotNull BlockCompressRecipe recipe
        ) {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.BLOCK.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> encode = BlockStatePredicate.CODEC.listOf().encode(recipe.inputs, ops, ops.empty());
            Tag tag = encode.getOrThrow();
            buf.writeNbt(tag);
            buf.writeVarInt(recipe.results.size());
            for (ChanceBlockState result : recipe.results) {
                ChanceBlockState.STREAM_CODEC.encode(buf, result);
            }
        }

        private static @NotNull BlockCompressRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.BLOCK.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            List<BlockStatePredicate> inputs = BlockStatePredicate.CODEC.listOf().decode(ops, buf.readNbt()).getOrThrow().getFirst();
            List<ChanceBlockState> results = new ArrayList<>();
            int i = buf.readVarInt();
            for (; i > 0; i--) {
                results.add(ChanceBlockState.STREAM_CODEC.decode(buf));
            }
            return new BlockCompressRecipe(inputs, results);
        }
    }

    public static class Builder extends AbstractRecipeBuilder<BlockCompressRecipe> {
        private final List<BlockStatePredicate> inputs = new ArrayList<>();
        private final List<ChanceBlockState> results = new ArrayList<>();

        public Builder input(BlockStatePredicate input) {
            this.inputs.add(input);
            return this;
        }

        public Builder input(TagKey<Block> input) {
            this.inputs.add(BlockStatePredicate.builder().of(input).build());
            return this;
        }

        public Builder input(Block input) {
            this.inputs.add(BlockStatePredicate.builder().of(input).build());
            return this;
        }

        public Builder result(ChanceBlockState result) {
            this.results.add(result);
            return this;
        }

        public Builder result(@NotNull Block result) {
            this.results.add(new ChanceBlockState(result.defaultBlockState(), 1.0));
            return this;
        }

        @Override
        public @NotNull BlockCompressRecipe buildRecipe() {
            return new BlockCompressRecipe(inputs, results);
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (inputs.isEmpty()) {
                throw new IllegalArgumentException("Recipe inputs must not be empty, RecipeId: " + pId);
            }
            if (results.isEmpty()) {
                throw new IllegalArgumentException("Recipe result must not be empty, RecipeId: " + pId);
            }
        }

        @Override
        public @NotNull String getType() {
            return "block_compress";
        }

        @Override
        public @NotNull Item getResult() {
            if (results.isEmpty()) return Items.ANVIL;
            Item item = results.getFirst().getState().getBlock().asItem();
            if (item == Items.AIR) item = Items.ANVIL;
            return item;
        }
    }
}
