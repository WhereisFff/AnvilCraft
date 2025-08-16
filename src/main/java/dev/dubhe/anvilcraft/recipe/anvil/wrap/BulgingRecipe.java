package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BulgingRecipe extends AbstractProcessRecipe<BulgingRecipe> {
    private final HasCauldronSimple hasCauldron;

    public BulgingRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results,
        HasCauldronSimple hasCauldron
    ) {
        super(
            new Vec3(0.0, -1.0, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.0, 0.0),
            results,
            new Vec3(0.0, -1.0, 0.0),
            hasCauldron
        );
        this.hasCauldron = hasCauldron;
    }

    @Override
    public @NotNull RecipeSerializer<BulgingRecipe> getSerializer() {
        return ModRecipeTypes.BULGING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<BulgingRecipe> getType() {
        return ModRecipeTypes.BULGING_TYPE.get();
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

    public boolean isFromWater() {
        return this.hasCauldron.getFluid().equals(BuiltInRegistries.FLUID.getKey(Fluids.WATER));
    }

    public static class Serializer implements RecipeSerializer<BulgingRecipe> {
        public static final MapCodec<BulgingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf()
                .fieldOf("ingredients")
                .forGetter(BulgingRecipe::getItemIngredients),
            ChanceItemStack.CODEC.listOf()
                .fieldOf("results")
                .forGetter(BulgingRecipe::getResults),
            HasCauldronSimple.CODEC
                .forGetter(BulgingRecipe::getHasCauldron)
        ).apply(instance, BulgingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BulgingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BulgingRecipe::getItemIngredients,
            ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BulgingRecipe::getResults,
            HasCauldronSimple.STREAM_CODEC,
            BulgingRecipe::getHasCauldron,
            BulgingRecipe::new
        );

        @Override
        public @NotNull MapCodec<BulgingRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BulgingRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends SimpleAbstractBuilder<BulgingRecipe, Builder> {
        private final HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

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
        protected BulgingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new BulgingRecipe(itemIngredients, results, this.hasCauldron.build());
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
        }

        @Override
        public @NotNull String getType() {
            return "bulging";
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
