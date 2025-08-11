package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.anvil.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

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
            new Vec3(0.0, -1.0, 0.0),
            null,
            new Vec3(0.0, -1.0, 0.0),
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

        private static final StreamCodec<RegistryFriendlyByteBuf, ItemInjectRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemInjectRecipe::getItemIngredients,
            ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ItemInjectRecipe::getResults,
            BlockStatePredicate.STREAM_CODEC,
            ItemInjectRecipe::getBlockIngredient,
            ChanceBlockState.STREAM_CODEC,
            ItemInjectRecipe::getBlockResult,
            ItemInjectRecipe::new
        );

        @Override
        public @NotNull MapCodec<ItemInjectRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemInjectRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }

    public static class Builder extends SimpleAbstractBuilder<ItemInjectRecipe, Builder> {
        BlockStatePredicate.Builder blockIngredient = BlockStatePredicate.builder();
        ChanceBlockState blockResult = null;

        public Builder inputBlock(Block block) {
            this.blockIngredient.of(block);
            return this;
        }

        public Builder inputBlock(@NotNull Supplier<? extends Block> block) {
            return this.inputBlock(block.get());
        }

        public Builder resultBlock(@NotNull Block block) {
            this.blockResult = new ChanceBlockState(block.defaultBlockState(), 1.0F);
            return this;
        }

        public Builder resultBlock(@NotNull Supplier<? extends Block> block) {
            return this.resultBlock(block.get());
        }

        @Override
        public @NotNull String getType() {
            return "item_inject";
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (this.itemIngredients.isEmpty()) {
                throw new IllegalArgumentException("Recipe ingredients must not be empty, RecipeId: " + pId);
            }
        }

        @Override
        protected ItemInjectRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new ItemInjectRecipe(itemIngredients, results, this.blockIngredient.build(), this.blockResult);
        }

        @Override
        public @NotNull Item getResult() {
            return WrapUtils.getItem(blockResult);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
