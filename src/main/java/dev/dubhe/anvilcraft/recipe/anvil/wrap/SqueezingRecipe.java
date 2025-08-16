package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SqueezingRecipe extends AbstractProcessRecipe<SqueezingRecipe> {
    private final HasCauldronSimple hasCauldron;

    public SqueezingRecipe(
        List<BlockStatePredicate> ingredients,
        List<ChanceBlockState> results,
        HasCauldronSimple hasCauldron
    ) {
        super(
            new Vec3(0.0, -1.0, 0.0),
            List.of(),
            new Vec3(0.0, -1.0, 0.0),
            List.of(),
            new Vec3(0.0, -2.0, 0.0),
            hasCauldron,
            new Vec3(0.0, -1.0, 0.0),
            results,
            ingredients
        );
        this.hasCauldron = hasCauldron;
    }

    @Override
    public boolean matches(@NotNull InWorldRecipeContext context, @NotNull Level level) {
        return super.matches(context, level);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull InWorldRecipeContext context, HolderLookup.@NotNull Provider provider) {
        return super.assemble(context, provider);
    }

    @Override
    public @NotNull RecipeSerializer<SqueezingRecipe> getSerializer() {
        return ModRecipeTypes.SQUEEZING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<SqueezingRecipe> getType() {
        return ModRecipeTypes.SQUEEZING_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public boolean isProduceFluid() {
        return this.hasCauldron.getConsume() < 0;
    }

    public static class Serializer implements RecipeSerializer<SqueezingRecipe> {
        public static final MapCodec<SqueezingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStatePredicate.CODEC
                .listOf()
                .fieldOf("ingredients")
                .forGetter(SqueezingRecipe::getBlocks),
            ChanceBlockState.CODEC
                .codec()
                .listOf()
                .fieldOf("results")
                .forGetter(SqueezingRecipe::getResultBlocks),
            HasCauldronSimple.CODEC
                .forGetter(SqueezingRecipe::getHasCauldron)
        ).apply(instance, SqueezingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SqueezingRecipe> STREAM_CODEC = StreamCodec.composite(
            BlockStatePredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SqueezingRecipe::getBlocks,
            ChanceBlockState.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SqueezingRecipe::getResultBlocks,
            HasCauldronSimple.STREAM_CODEC,
            SqueezingRecipe::getHasCauldron,
            SqueezingRecipe::new
        );

        @Override
        public @NotNull MapCodec<SqueezingRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, SqueezingRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends AbstractRecipeBuilder<SqueezingRecipe> {
        private final List<BlockStatePredicate> ingredients = new ArrayList<>();
        private final List<ChanceBlockState> results = new ArrayList<>();
        private final HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

        public Builder requires(BlockStatePredicate ingredient) {
            this.ingredients.add(ingredient);
            return this;
        }

        public Builder requires(Block ingredient) {
            return this.requires(BlockStatePredicate.builder().of(ingredient).build());
        }

        public Builder requires(TagKey<Block> ingredient) {
            return this.requires(BlockStatePredicate.builder().of(ingredient).build());
        }

        public Builder result(ChanceBlockState result) {
            this.results.add(result);
            return this;
        }

        public Builder result(@NotNull Block result, float chance) {
            return this.result(new ChanceBlockState(result.defaultBlockState(), chance));
        }

        public Builder result(Block result) {
            return this.result(result, 1.0f);
        }

        public Builder cauldron(ResourceLocation fluid) {
            this.hasCauldron.fluid(fluid);
            return this;
        }

        public Builder cauldron(Block cauldron) {
            this.cauldron(WrapUtils.cauldron2Fluid(cauldron));
            return this;
        }

        public Builder transform(ResourceLocation transform) {
            this.hasCauldron.transform(transform);
            return this;
        }

        public Builder transform(Block transform) {
            this.hasCauldron.transform(WrapUtils.cauldron2Fluid(transform));
            return this;
        }

        public Builder produceFluid(boolean produceFluid) {
            if (!produceFluid) return this;
            this.hasCauldron.consume(-1);
            return this;
        }

        public Builder consumeFluid(boolean consumeFluid) {
            if (!consumeFluid) return this;
            this.hasCauldron.consume(1);
            return this;
        }

        @Override
        public @NotNull SqueezingRecipe buildRecipe() {
            return new SqueezingRecipe(ingredients, results, hasCauldron.build());
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
        }

        @Override
        public @NotNull String getType() {
            return "bulging";
        }

        @Override
        public @NotNull Item getResult() {
            return WrapUtils.getItem(this.results);
        }
    }
}
