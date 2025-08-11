package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BlockCompressRecipe extends InWorldRecipe {
    private final List<BlockStatePredicate> inputs;
    private final List<ChanceBlockState> results;

    public BlockCompressRecipe(
        List<BlockStatePredicate> inputs,
        List<ChanceBlockState> results
    ) {
        super(
            WrapUtils.getItemStack(results),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            WrapUtils.getIngredientPredicates(inputs),
            List.of(),
            WrapUtils.getOutcomes(results),
            0,
            true
        );
        this.inputs = inputs;
        this.results = results;
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

        private static final StreamCodec<RegistryFriendlyByteBuf, BlockCompressRecipe> STREAM_CODEC = StreamCodec.composite(
            BlockStatePredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BlockCompressRecipe::getInputs,
            ChanceBlockState.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BlockCompressRecipe::getResults,
            BlockCompressRecipe::new
        );

        @Override
        public @NotNull MapCodec<BlockCompressRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockCompressRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
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
            this.results.add(new ChanceBlockState(result.defaultBlockState(), 1.0f));
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
            return WrapUtils.getItem(results);
        }
    }
}
