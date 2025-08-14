package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.block.HeaterBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
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

import java.util.List;

@Getter
public class SuperHeatingRecipe extends AbstractProcessRecipe<SuperHeatingRecipe> {
    public SuperHeatingRecipe(
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
                    .of(ModBlocks.HEATER.get())
                    .with(HeaterBlock.OVERLOAD, false)
                    .build()
            )
        );
    }

    @Override
    public @NotNull RecipeSerializer<SuperHeatingRecipe> getSerializer() {
        return ModRecipeTypes.SUPER_HEATING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<SuperHeatingRecipe> getType() {
        return ModRecipeTypes.SUPER_HEATING_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer implements RecipeSerializer<SuperHeatingRecipe> {
        private static final MapCodec<SuperHeatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf()
                .optionalFieldOf("ingredients", List.of())
                .forGetter(SuperHeatingRecipe::getItemIngredients),
            ChanceItemStack.CODEC.listOf()
                .optionalFieldOf("results", List.of())
                .forGetter(SuperHeatingRecipe::getResults),
            HasCauldronSimple.CODEC
                .forGetter(SuperHeatingRecipe::getHasCauldron)
        ).apply(instance, SuperHeatingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SuperHeatingRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SuperHeatingRecipe::getItemIngredients,
            ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            SuperHeatingRecipe::getResults,
            HasCauldronSimple.STREAM_CODEC,
            SuperHeatingRecipe::getHasCauldron,
            SuperHeatingRecipe::new
        );

        @Override
        public @NotNull MapCodec<SuperHeatingRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, SuperHeatingRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends SimpleAbstractBuilder<SuperHeatingRecipe, Builder> {
        HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

        public @NotNull Builder fluid(ResourceLocation fluid) {
            this.hasCauldron.fluid(fluid);
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
        protected SuperHeatingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new SuperHeatingRecipe(itemIngredients, results, this.hasCauldron.build());
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (itemIngredients.isEmpty()) {
                throw new IllegalArgumentException("Recipe ingredients must not be empty, RecipeId: " + pId);
            }
        }

        @Override
        public @NotNull String getType() {
            return "super_heating";
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
