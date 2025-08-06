package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.neo.util.HasCauldronSimple;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.WrapUtils;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -1.0, 0.0),
            hasCauldron
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

        public static final StreamCodec<RegistryFriendlyByteBuf, BulgingRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::encode,
            Serializer::decode
        );

        @Override
        public @NotNull MapCodec<BulgingRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, BulgingRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull BulgingRecipe recipe) {
            WrapUtils.encodeIngredients(buf, recipe.getItemIngredients());
            WrapUtils.encodeResults(buf, recipe.getResults());
            HasCauldronSimple.STREAM_CODEC.encode(buf, recipe.getHasCauldron());
        }

        public static @NotNull BulgingRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            List<ItemIngredientPredicate> ingredients = WrapUtils.decodeIngredients(buf);
            List<ChanceItemStack> results = WrapUtils.decodeResults(buf);
            HasCauldronSimple hasCauldron = HasCauldronSimple.STREAM_CODEC.decode(buf);
            return new BulgingRecipe(ingredients, results, hasCauldron);
        }
    }

    public static class Builder extends AbstractRecipeBuilder<BulgingRecipe> {
        private final List<ItemIngredientPredicate> ingredients = new ArrayList<>();
        private final List<ChanceItemStack> results = new ArrayList<>();
        private final HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

        public Builder requires(ItemIngredientPredicate ingredient) {
            this.ingredients.add(ingredient);
            return this;
        }

        public Builder requires(ItemLike item, int count) {
            return requires(ItemIngredientPredicate.Builder.item().of(item).withCount(count).build());
        }

        public Builder requires(ItemLike pItem) {
            return requires(pItem, 1);
        }

        public Builder requires(TagKey<Item> tag, int count) {
            return requires(ItemIngredientPredicate.Builder.item().of(tag).withCount(count).build());
        }

        public Builder requires(TagKey<Item> pTag) {
            return requires(pTag, 1);
        }

        public Builder result(ItemStack stack, float chance) {
            results.add(ChanceItemStack.of(stack, chance));
            return this;
        }

        public Builder result(ItemStack stack) {
            return this.result(stack, 1.0f);
        }

        public Builder result(@NotNull ItemLike like, double chance, int count) {
            ItemStack stack = like.asItem().getDefaultInstance();
            stack.setCount(count);
            return this.result(stack, 1.0f);
        }

        public Builder result(@NotNull ItemLike like, double chance) {
            return this.result(like, chance, 1);
        }

        public Builder result(@NotNull ItemLike like, int count) {
            return this.result(like, 1.0, count);
        }

        public Builder result(@NotNull ItemLike like) {
            return this.result(like, 1);
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
        public @NotNull BulgingRecipe buildRecipe() {
            return new BulgingRecipe(ingredients, results, hasCauldron.build());
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
            if (this.results.isEmpty()) {
                return Items.ANVIL;
            }
            return this.results.getFirst().getItem();
        }
    }
}
