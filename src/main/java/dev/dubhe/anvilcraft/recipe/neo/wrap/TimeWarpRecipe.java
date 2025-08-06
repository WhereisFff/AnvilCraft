package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.block.CorruptedBeaconBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.neo.util.HasCauldronSimple;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.WrapUtils;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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

        private static final StreamCodec<RegistryFriendlyByteBuf, TimeWarpRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::encode,
            Serializer::decode
        );

        @Override
        public @NotNull MapCodec<TimeWarpRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, TimeWarpRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull TimeWarpRecipe recipe) {
            WrapUtils.encodeIngredients(buf, recipe.getItemIngredients());
            WrapUtils.encodeResults(buf, recipe.getResults());
            HasCauldronSimple.STREAM_CODEC.encode(buf, recipe.getHasCauldron());
        }

        public static @NotNull TimeWarpRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            List<ItemIngredientPredicate> ingredients = WrapUtils.decodeIngredients(buf);
            List<ChanceItemStack> results = WrapUtils.decodeResults(buf);
            return new TimeWarpRecipe(ingredients, results, HasCauldronSimple.STREAM_CODEC.decode(buf));
        }
    }

    public static class Builder extends AbstractBuilder<TimeWarpRecipe, Builder> {
        HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

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
        public @NotNull TimeWarpRecipe buildRecipe() {
            return new TimeWarpRecipe(itemIngredients, results, hasCauldron.build());
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
