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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BlockSmearRecipe extends AbstractProcessRecipe<BlockSmearRecipe> {
    private final List<BlockStatePredicate> inputs;
    private final ChanceBlockState result;

    public BlockSmearRecipe(
        List<BlockStatePredicate> inputs,
        ChanceBlockState result
    ) {
        super(
            new Property()
                .setBlockInputOffset(new Vec3(0.0, -1.0, 0.0))
                .setInputBlocks(inputs)
                .setBlockOutputOffset(new Vec3(0.0, -1.0, 0.0))
                .setResultBlocks(result)
        );
        this.inputs = inputs;
        this.result = result;
    }

    @Override
    public @NotNull RecipeSerializer<BlockSmearRecipe> getSerializer() {
        return ModRecipeTypes.BLOCK_SMEAR_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<BlockSmearRecipe> getType() {
        return ModRecipeTypes.BLOCK_SMEAR_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer implements RecipeSerializer<BlockSmearRecipe> {
        private static final MapCodec<BlockSmearRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStatePredicate.CODEC
                .listOf()
                .fieldOf("inputs")
                .forGetter(BlockSmearRecipe::getInputs),
            ChanceBlockState.CODEC.codec()
                .fieldOf("result")
                .forGetter(BlockSmearRecipe::getResult)
        ).apply(instance, BlockSmearRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, BlockSmearRecipe> STREAM_CODEC = StreamCodec.composite(
            BlockStatePredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BlockSmearRecipe::getInputs,
            ChanceBlockState.STREAM_CODEC,
            BlockSmearRecipe::getResult,
            BlockSmearRecipe::new
        );

        @Override
        public @NotNull MapCodec<BlockSmearRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockSmearRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends AbstractRecipeBuilder<BlockSmearRecipe> {
        private final List<BlockStatePredicate> inputs = new ArrayList<>();
        private ChanceBlockState result = null;

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
            this.result = result;
            return this;
        }

        public Builder result(@NotNull Block result) {
            this.result = new ChanceBlockState(result.defaultBlockState(), 1.0f);
            return this;
        }

        @Override
        public @NotNull BlockSmearRecipe buildRecipe() {
            return new BlockSmearRecipe(this.inputs, this.result);
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (inputs.isEmpty()) {
                throw new IllegalArgumentException("Recipe inputs must not be empty, RecipeId: " + pId);
            }
            if (result == null) {
                throw new IllegalArgumentException("Recipe result must not be null, RecipeId: " + pId);
            }
        }

        @Override
        public @NotNull String getType() {
            return "block_smear";
        }

        @Override
        public @NotNull Item getResult() {
            return WrapUtils.getItem(result);
        }
    }
}
