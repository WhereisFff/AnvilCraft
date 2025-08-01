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
public class BlockSmearRecipe extends InWorldRecipe {
    private final List<BlockStatePredicate> inputs;
    private final ChanceBlockState result;

    public BlockSmearRecipe(
        List<BlockStatePredicate> inputs,
        ChanceBlockState result
    ) {
        super(
            WrapUtils.getItemStack(result),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            WrapUtils.getPredicates(inputs),
            List.of(),
            WrapUtils.getOutcomes(result),
            0,
            true
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

        private static final StreamCodec<RegistryFriendlyByteBuf, BlockSmearRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::encode,
            Serializer::decode
        );

        @Override
        public @NotNull MapCodec<BlockSmearRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockSmearRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }

        private static void encode(
            @NotNull RegistryFriendlyByteBuf buf,
            @NotNull BlockSmearRecipe recipe
        ) {
            buf.writeCollection(recipe.inputs, (buf1, input) -> BlockStatePredicate.STREAM_CODEC.encode(buf, input));
            ChanceBlockState.STREAM_CODEC.encode(buf, recipe.result);
        }

        private static @NotNull BlockSmearRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            List<BlockStatePredicate> inputs = buf.readCollection(ArrayList::new, buf1 -> BlockStatePredicate.STREAM_CODEC.decode(buf));
            ChanceBlockState result = ChanceBlockState.STREAM_CODEC.decode(buf);
            return new BlockSmearRecipe(inputs, result);
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
            this.result = new ChanceBlockState(result.defaultBlockState(), 1.0);
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
            return "block_compress";
        }

        @Override
        public @NotNull Item getResult() {
            return WrapUtils.getItem(result);
        }
    }
}
