package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.neo.util.WrapUtils;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
            WrapUtils.getPredicates(inputs),
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
            buf.writeCollection(recipe.inputs, (buf1, input) -> BlockStatePredicate.STREAM_CODEC.encode(buf, input));
            buf.writeCollection(recipe.results, (buf1, results) -> ChanceBlockState.STREAM_CODEC.encode(buf, results));
        }

        private static @NotNull BlockCompressRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            List<BlockStatePredicate> inputs = buf.readCollection(ArrayList::new, buf1 -> BlockStatePredicate.STREAM_CODEC.decode(buf));
            List<ChanceBlockState> results = buf.readCollection(ArrayList::new, buf1 -> ChanceBlockState.STREAM_CODEC.decode(buf));
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
            return WrapUtils.getItem(results);
        }
    }
}
