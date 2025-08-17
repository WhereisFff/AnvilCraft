package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceBlockState;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlockCrushRecipe extends AbstractProcessRecipe<BlockCrushRecipe> {
    private final BlockStatePredicate input;
    private final ChanceBlockState result;

    public BlockCrushRecipe(
        BlockStatePredicate input,
        ChanceBlockState result
    ) {
        super(
            new AbstractProcessRecipe.Property()
                .setBlockInputOffset(new Vec3(0.0, -1.0, 0.0))
                .setInputBlocks(input)
                .setBlockOutputOffset(new Vec3(0.0, -1.0, 0.0))
                .setResultBlocks(result)
        );
        this.input = input;
        this.result = result;
    }

    @Override
    public @NotNull RecipeType<BlockCrushRecipe> getType() {
        return ModRecipeTypes.BLOCK_CRUSH_TYPE.get();
    }

    @Override
    public @NotNull RecipeSerializer<BlockCrushRecipe> getSerializer() {
        return ModRecipeTypes.BLOCK_CRUSH_SERIALIZER.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer implements RecipeSerializer<BlockCrushRecipe> {
        private static final MapCodec<BlockCrushRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStatePredicate.CODEC
                .fieldOf("input")
                .forGetter(BlockCrushRecipe::getInput),
            ChanceBlockState.CODEC.codec()
                .fieldOf("result")
                .forGetter(BlockCrushRecipe::getResult)
        ).apply(instance, BlockCrushRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BlockCrushRecipe> STREAM_CODEC = StreamCodec.composite(
            BlockStatePredicate.STREAM_CODEC,
            BlockCrushRecipe::getInput,
            ChanceBlockState.STREAM_CODEC,
            BlockCrushRecipe::getResult,
            BlockCrushRecipe::new
        );

        @Override
        public @NotNull MapCodec<BlockCrushRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockCrushRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends AbstractRecipeBuilder<BlockCrushRecipe> {
        private BlockStatePredicate input = null;
        private ChanceBlockState result = null;

        public Builder input(BlockStatePredicate input) {
            this.input = (input);
            return this;
        }

        public Builder input(TagKey<Block> input) {
            this.input = BlockStatePredicate.builder().of(input).build();
            return this;
        }

        public Builder input(Block input) {
            this.input = (BlockStatePredicate.builder().of(input).build());
            return this;
        }

        public Builder result(ChanceBlockState result) {
            this.result = (result);
            return this;
        }

        public Builder result(@NotNull Block result) {
            this.result = (new ChanceBlockState(result.defaultBlockState(), 1.0f));
            return this;
        }

        @Override
        public @NotNull BlockCrushRecipe buildRecipe() {
            return new BlockCrushRecipe(this.input, this.result);
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (input == null) {
                throw new IllegalArgumentException("Recipe input must not be null, RecipeId: " + pId);
            }
            if (result == null) {
                throw new IllegalArgumentException("Recipe result must not be null, RecipeId: " + pId);
            }
        }

        @Override
        public @NotNull String getType() {
            return "block_crush";
        }

        @Override
        public @NotNull Item getResult() {
            return WrapUtils.getItem(result);
        }
    }
}
