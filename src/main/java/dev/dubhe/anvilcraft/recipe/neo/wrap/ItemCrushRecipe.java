package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
public class ItemCrushRecipe extends AbstractItemProcessRecipe<ItemCrushRecipe> implements Recipe<InWorldRecipeContext> {
    private final List<ItemIngredientPredicate> itemIngredients;
    private final List<ItemStack> results;
    private final List<Double> chances;

    public ItemCrushRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ItemStack> results,
        List<Double> chances
    ) {
        super(
            new Vec3(0.0, -0.0625, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            chances,
            new Vec3(0.0, -0.6, 0.0),
            BlockStatePredicate.Builder
                .of(ModBlocks.CRUSHING_TABLE.get())
                .build()
        );
        this.itemIngredients = itemIngredients;
        this.results = results;
        this.chances = chances;
    }

    @Override
    public @NotNull RecipeSerializer<ItemCrushRecipe> getSerializer() {
        return ModRecipeTypes.ITEM_CRUSH_SERIALIZERS.get();
    }

    @Override
    public @NotNull RecipeType<ItemCrushRecipe> getType() {
        return ModRecipeTypes.ITEM_CRUSH_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer implements RecipeSerializer<ItemCrushRecipe> {
        public static final MapCodec<ItemCrushRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf()
                .fieldOf("ingredients")
                .forGetter(ItemCrushRecipe::getItemIngredients),
            ItemStack.CODEC.listOf()
                .fieldOf("results")
                .forGetter(ItemCrushRecipe::getResults),
            Codec.DOUBLE.listOf()
                .fieldOf("chances")
                .forGetter(ItemCrushRecipe::getChances)
        ).apply(instance, ItemCrushRecipe::new));

        @Override
        public @NotNull MapCodec<ItemCrushRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ItemCrushRecipe> streamCodec() {
            return StreamCodec.of(Serializer::encode, Serializer::decode);
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ItemCrushRecipe recipe) {
            buf.writeVarInt(recipe.itemIngredients.size());
            for (ItemIngredientPredicate itemIngredient : recipe.itemIngredients) {
                RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())).createSerializationContext(NbtOps.INSTANCE);
                DataResult<Tag> encode = ItemIngredientPredicate.CODEC.encode(itemIngredient, ops, ops.empty());
                Tag tag = encode.getOrThrow() ;
                buf.writeNbt(tag);
            }
            buf.writeVarInt(recipe.results.size());
            for (ItemStack result : recipe.results) {
                ItemStack.STREAM_CODEC.encode(buf, result);
            }
            buf.writeVarInt(recipe.chances.size());
            for (double chance : recipe.chances) {
                buf.writeDouble(chance);
            }
        }

        public static @NotNull ItemCrushRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            int i = buf.readVarInt();
            List<ItemIngredientPredicate> ingredients = new ArrayList<>();
            for (; i > 0; i--) {
                RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())).createSerializationContext(NbtOps.INSTANCE);
                ingredients.add(ItemIngredientPredicate.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst());
            }
            List<ItemStack> results = new ArrayList<>();
            i = buf.readVarInt();
            for (; i > 0; i--) {
                results.add(ItemStack.STREAM_CODEC.decode(buf));
            }
            List<Double> chances = new ArrayList<>();
            i = buf.readVarInt();
            for (; i > 0; i--) {
                chances.add(buf.readDouble());
            }
            return new ItemCrushRecipe(ingredients, results, chances);
        }
    }

    public static class Builder extends AbstractRecipeBuilder<ItemCrushRecipe> {
        private final List<ItemIngredientPredicate> itemIngredients = new ArrayList<>();
        private final List<ItemStack> results = new ArrayList<>();
        private final List<Double> chances = new ArrayList<>();

        public Builder requires(@NotNull ItemIngredientPredicate ingredient) {
            this.itemIngredients.add(ingredient);
            return this;
        }

        public Builder requires(@NotNull TagKey<Item> ingredient) {
            this.itemIngredients.add(ItemIngredientPredicate.Builder.item().of(ingredient).build());
            return this;
        }

        public <T> Builder requires(@NotNull ItemStack ingredient) {
            Item item = ingredient.getItem();
            ItemStack defaultInstance = item.getDefaultInstance();
            ItemIngredientPredicate.Builder predicate = ItemIngredientPredicate.Builder.item().of(item);
            for (TypedDataComponent<?> component : item.components()) {
                Object o = defaultInstance.get(component.type());
                if (o != null && o.equals(component.value())) continue;
                //noinspection unchecked
                predicate.hasComponents(
                    DataComponentPredicate.builder()
                        .expect((DataComponentType<T>) component.type(), (T) component.value())
                        .build()
                );
            }
            this.itemIngredients.add(predicate.withCount(ingredient.getCount()).build());
            return this;
        }

        public Builder requires(@NotNull ItemLike ingredient, int count) {
            ItemStack stack = ingredient.asItem().getDefaultInstance();
            stack.setCount(count);
            return this.requires(stack);
        }

        public Builder requires(@NotNull ItemLike ingredient) {
            return this.requires(ingredient, 1);
        }

        public Builder result(@NotNull ItemStack result, double chance) {
            this.results.add(result);
            this.chances.add(chance);
            return this;
        }

        public Builder result(@NotNull ItemStack result) {
            this.results.add(result);
            this.chances.add(1.0);
            return this;
        }

        public Builder result(@NotNull ItemLike result, double chance) {
            return this.result(result.asItem().getDefaultInstance(), chance);
        }

        public Builder result(@NotNull ItemLike result) {
            return this.result(result, 1.0);
        }

        @Override
        public @NotNull String getType() {
            return "item_crush";
        }

        @Override
        public @NotNull ItemCrushRecipe buildRecipe() {
            return new ItemCrushRecipe(itemIngredients, results, chances);
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {

        }

        @Override
        public @NotNull Item getResult() {
            return results.isEmpty() ? Items.ANVIL : results.getFirst().getItem();
        }
    }
}
