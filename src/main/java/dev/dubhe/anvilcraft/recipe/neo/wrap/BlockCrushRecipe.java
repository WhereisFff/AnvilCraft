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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.stream.Stream;

@Getter
public class BlockCrushRecipe extends InWorldRecipe {
    private final BlockStatePredicate input;
    private final ChanceBlockState result;

    public BlockCrushRecipe(
        BlockStatePredicate input,
        ChanceBlockState result
    ) {
        super(
            BlockCrushRecipe.getIcon(result),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            BlockCrushRecipe.getPredicates(input),
            List.of(),
            BlockCrushRecipe.getOutcomes(result),
            0,
            true
        );
        this.input = input;
        this.result = result;
    }

    private static @NotNull @Unmodifiable List<IRecipePredicate<?>> getPredicates(
        @NotNull BlockStatePredicate block
    ) {
        return List.of(new HasBlock(new Vec3(0, -1, 0), block));
    }

    private static @NotNull @Unmodifiable List<IRecipeOutcome<?>> getOutcomes(
        @NotNull ChanceBlockState result
    ) {
        return List.of(new SetBlock(result.getState(), new Vec3(0, -1, 0), 1));
    }

    private static @NotNull ItemStack getIcon(@NotNull ChanceBlockState result) {
        Item item = result.getState().getBlock().asItem();
        if (item == Items.AIR) item = Items.ANVIL;
        return item.getDefaultInstance();
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

        private static final StreamCodec<RegistryFriendlyByteBuf, BlockCrushRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::encode,
            Serializer::decode
        );

        @Override
        public @NotNull MapCodec<BlockCrushRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockCrushRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }

        private static void encode(
            @NotNull RegistryFriendlyByteBuf buf,
            @NotNull BlockCrushRecipe recipe
        ) {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.BLOCK.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> encode = BlockStatePredicate.CODEC.encode(recipe.input, ops, ops.empty());
            Tag tag = encode.getOrThrow();
            buf.writeNbt(tag);
            ChanceBlockState.STREAM_CODEC.encode(buf, recipe.result);
        }

        private static @NotNull BlockCrushRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.BLOCK.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            BlockStatePredicate input = BlockStatePredicate.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst();
            ChanceBlockState result = ChanceBlockState.STREAM_CODEC.decode(buf);
            return new BlockCrushRecipe(input, result);
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
            this.result = (new ChanceBlockState(result.defaultBlockState(), 1.0));
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
            BlockState state = result.getState();
            if (state.isEmpty() || state.isAir()) return Items.ANVIL;
            Item item = state.getBlock().asItem();
            if (item == Items.AIR) item = Items.ANVIL;
            return item;
        }
    }
}
