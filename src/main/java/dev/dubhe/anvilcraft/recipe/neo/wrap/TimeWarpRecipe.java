package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.heat.HeatTier;
import dev.dubhe.anvilcraft.block.CorruptedBeaconBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.neo.outcome.ProduceHeat;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.neo.util.Distance;
import dev.dubhe.anvilcraft.recipe.neo.util.HasCauldronSimple;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.WrapUtils;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TimeWarpRecipe extends AbstractProcessRecipe<TimeWarpRecipe> {
    public TimeWarpRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results,
        HasCauldronSimple hasCauldron
    ) {
        super(
            new Vec3(0.0, -1.0, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -1.0, 0.0),
            hasCauldron,
            new Vec3(0.0, -2.0, 0.0),
            List.of(),
            List.of(
                BlockStatePredicate.builder()
                    .of(ModBlocks.CORRUPTED_BEACON.get())
                    .with(CorruptedBeaconBlock.LIT, true)
                    .build()
            )
        );
    }

    @Override
    public @NotNull RecipeSerializer<TimeWarpRecipe> getSerializer() {
        return ModRecipeTypes.TIME_WARP_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<TimeWarpRecipe> getType() {
        return ModRecipeTypes.TIME_WARP_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public boolean isConsumeFluid() {
        return this.hasCauldron.getConsume() > 0;
    }

    public boolean isProduceFluid() {
        return this.hasCauldron.getConsume() < 0;
    }

    public static class Serializer implements RecipeSerializer<TimeWarpRecipe> {
        private static final MapCodec<TimeWarpRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf()
                .optionalFieldOf("ingredients", List.of())
                .forGetter(TimeWarpRecipe::getItemIngredients),
            ChanceItemStack.CODEC.listOf()
                .optionalFieldOf("results", List.of())
                .forGetter(TimeWarpRecipe::getResults),
            HasCauldronSimple.CODEC
                .forGetter(TimeWarpRecipe::getHasCauldron)
        ).apply(instance, TimeWarpRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, TimeWarpRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TimeWarpRecipe::getItemIngredients,
            ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TimeWarpRecipe::getResults,
            HasCauldronSimple.STREAM_CODEC,
            TimeWarpRecipe::getHasCauldron,
            TimeWarpRecipe::new
        );

        @Override
        public @NotNull MapCodec<TimeWarpRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, TimeWarpRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends SimpleAbstractBuilder<TimeWarpRecipe, Builder> {
        HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();
        List<ProduceHeat.HeatData> heatData = new ArrayList<>();
        Distance distance = Distance.DEFAULT;

        public Builder heat(HeatTier tier, int duration) {
            this.heatData.add(new ProduceHeat.HeatData(tier, duration));
            return this;
        }

        public Builder distance(Distance distance) {
            this.distance = distance;
            return this;
        }

        public Builder distance(Distance.Type type, int distance, boolean isHorizontal) {
            return this.distance(new Distance(type, distance, isHorizontal));
        }

        public Builder distanceEuclidean(int distance, boolean isHorizontal) {
            return this.distance(Distance.Type.EUCLIDEAN, distance, isHorizontal);
        }

        public Builder distanceEuclidean(boolean isHorizontal) {
            return this.distance(Distance.Type.EUCLIDEAN, 1, isHorizontal);
        }

        public Builder distanceEuclidean(int distance) {
            return this.distance(Distance.Type.EUCLIDEAN, distance, true);
        }

        public Builder distanceEuclidean() {
            return this.distance(Distance.Type.EUCLIDEAN, 1, true);
        }

        public Builder distanceManhattan(int distance, boolean isHorizontal) {
            return this.distance(Distance.Type.MANHATTAN, distance, isHorizontal);
        }

        public Builder distanceManhattan(boolean isHorizontal) {
            return this.distance(Distance.Type.MANHATTAN, 1, isHorizontal);
        }

        public Builder distanceManhattan(int distance) {
            return this.distance(Distance.Type.MANHATTAN, distance, true);
        }

        public Builder distanceManhattan() {
            return this.distance(Distance.Type.MANHATTAN, 1, true);
        }

        public Builder distanceChebyshev(int distance, boolean isHorizontal) {
            return this.distance(Distance.Type.CHEBYSHEV, distance, isHorizontal);
        }

        public Builder distanceChebyshev(boolean isHorizontal) {
            return this.distance(Distance.Type.CHEBYSHEV, 1, isHorizontal);
        }

        public Builder distanceChebyshev(int distance) {
            return this.distance(Distance.Type.CHEBYSHEV, distance, true);
        }

        public Builder distanceChebyshev() {
            return this.distance(Distance.Type.CHEBYSHEV, 1, true);
        }

        public @NotNull Builder fluid(ResourceLocation fluid) {
            this.hasCauldron.fluid(fluid);
            return this;
        }

        public @NotNull Builder fluid(Block cauldron) {
            this.fluid(WrapUtils.cauldron2Fluid(cauldron));
            return this;
        }

        public @NotNull Builder transform(ResourceLocation transform) {
            this.hasCauldron.transform(transform);
            return this;
        }

        public @NotNull Builder transform(Block cauldron) {
            this.transform(WrapUtils.cauldron2Fluid(cauldron));
            return this;
        }

        public Builder consume(int consume) {
            this.hasCauldron.consume(consume);
            return this;
        }

        @Override
        protected TimeWarpRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new TimeWarpRecipe(itemIngredients, results, this.hasCauldron.build());
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (itemIngredients.isEmpty()) {
                throw new IllegalArgumentException("Recipe ingredients must not be empty, RecipeId: " + pId);
            }
        }

        @Override
        public @NotNull String getType() {
            return "time_warp";
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
