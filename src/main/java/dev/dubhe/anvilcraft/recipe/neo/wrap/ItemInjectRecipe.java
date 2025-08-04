package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.WrapUtils;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ItemInjectRecipe extends AbstractProcessRecipe<ItemInjectRecipe> {
    private final BlockStatePredicate blockIngredient;
    private final ChanceBlockState blockResult;

    public ItemInjectRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results,
        BlockStatePredicate blockIngredient,
        ChanceBlockState blockResult
    ) {
        super(
            Vec3.ZERO,
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -0.6, 0.0),
            null,
            List.of(blockResult),
            List.of(blockIngredient)
        );
        this.blockIngredient = blockIngredient;
        this.blockResult = blockResult;
    }

    @Override
    public @NotNull RecipeSerializer<ItemInjectRecipe> getSerializer() {
        return ModRecipeTypes.ITEM_INJECT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<ItemInjectRecipe> getType() {
        return ModRecipeTypes.ITEM_INJECT_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer implements RecipeSerializer<ItemInjectRecipe> {
        private static final MapCodec<ItemInjectRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC
                .listOf()
                .optionalFieldOf("ingredients", List.of())
                .forGetter(ItemInjectRecipe::getItemIngredients),
            ChanceItemStack.CODEC
                .listOf()
                .optionalFieldOf("results", List.of())
                .forGetter(ItemInjectRecipe::getResults),
            BlockStatePredicate.CODEC
                .fieldOf("block_ingredient")
                .forGetter(ItemInjectRecipe::getBlockIngredient),
            ChanceBlockState.CODEC.codec()
                .fieldOf("block_result")
                .forGetter(ItemInjectRecipe::getBlockResult)
        ).apply(instance, ItemInjectRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ItemInjectRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::encode,
            Serializer::decode
        );

        @Override
        public @NotNull MapCodec<ItemInjectRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemInjectRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ItemInjectRecipe recipe) {
            WrapUtils.encodeIngredients(buf, recipe.getItemIngredients());
            WrapUtils.encodeResults(buf, recipe.getResults());
            BlockStatePredicate.STREAM_CODEC.encode(buf, recipe.getBlockIngredient());
            ChanceBlockState.encode(buf, recipe.getBlockResult());
        }

        public static @NotNull ItemInjectRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new ItemInjectRecipe(
                WrapUtils.decodeIngredients(buf),
                WrapUtils.decodeResults(buf),
                BlockStatePredicate.STREAM_CODEC.decode(buf),
                ChanceBlockState.decode(buf)
            );
        }
    }

    public static class Builder extends AbstractBuilder<ItemInjectRecipe, Builder> {
        BlockStatePredicate.Builder blockIngredient = BlockStatePredicate.builder();
        ChanceBlockState blockResult = null;

        @Override
        public @NotNull ItemInjectRecipe buildRecipe() {
            return new ItemInjectRecipe(itemIngredients, results, blockIngredient.build(), blockResult);
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (itemIngredients.isEmpty()) {
                throw new IllegalArgumentException("Recipe inputs must not be empty, RecipeId: " + pId);
            }
            if (blockResult == null) {
                throw new IllegalArgumentException("Recipe block_result must not be null, RecipeId: " + pId);
            }
        }

        @Override
        public @NotNull String getType() {
            return "item_inject";
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder inputBlock(Block block) {
            this.blockIngredient.of(block);
            return this;
        }

        public Builder resultBlock(@NotNull Block block) {
            this.blockResult = new ChanceBlockState(block.defaultBlockState(), 1.0F);
            return this;
        }

        @Override
        public @NotNull Item getResult() {
            return WrapUtils.getItem(blockResult);
        }
    }
}
