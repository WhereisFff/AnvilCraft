package dev.dubhe.anvilcraft.recipe;

import com.google.common.collect.Collections2;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Getter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmithingMultiphaseRecipe implements SmithingRecipe {
    final Ingredient first;
    final Ingredient second;
    final ItemStack result;

    public SmithingMultiphaseRecipe(Ingredient first, Ingredient second, ItemStack result) {
        this.first = first;
        this.second = second;
        this.result = result;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        return this.first.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return this.second.test(stack);
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return input.template().isEmpty() && this.first.test(input.base()) && this.second.test(input.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        Pair<Component, ItemEnchantments> first = new Pair<>(
            input.base().getHoverName(), input.base().get(DataComponents.ENCHANTMENTS)
        );
        Pair<Component, ItemEnchantments> second = new Pair<>(
            input.addition().getHoverName(), input.addition().get(DataComponents.ENCHANTMENTS)
        );

        if (first.getFirst().getContents().equals(second.getFirst().getContents())) {
            boolean firstHasCustomName = input.base().has(DataComponents.CUSTOM_NAME);
            boolean secondHasCustomName = input.addition().has(DataComponents.CUSTOM_NAME);
            if (firstHasCustomName && secondHasCustomName) {
                first = new Pair<>(first.getFirst().copy(), first.getSecond());
                second = new Pair<>(second.getFirst().copy(), second.getSecond());
            } else {
                if (!firstHasCustomName) {
                    first = new Pair<>(this.result.getHoverName().copy(), first.getSecond());
                }
                if (!secondHasCustomName) {
                    second = new Pair<>(this.result.getHoverName().copy(), second.getSecond());
                }
            }
        }

        IToolProperties.Multiphase multiphase = IToolProperties.Multiphase.make(this.result, first, second);
        ItemStack result = this.result.copy();
        result.set(ModComponents.MULTIPHASE, multiphase);

        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SMITHING_MULTIPHASE_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<SmithingMultiphaseRecipe> {
        private static final MapCodec<SmithingMultiphaseRecipe> CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.first),
            Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.second),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
        ).apply(ins, SmithingMultiphaseRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingMultiphaseRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<SmithingMultiphaseRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingMultiphaseRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static SmithingMultiphaseRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient ingredient2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            return new SmithingMultiphaseRecipe(ingredient1, ingredient2, itemstack);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, SmithingMultiphaseRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.first);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.second);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }

    public static class Builder extends AbstractRecipeBuilder<SmithingMultiphaseRecipe> {
        private Ingredient first;
        private Ingredient second = Ingredient.EMPTY;
        private ItemStack result;

        @SafeVarargs
        public final Builder input(NonNullSupplier<? extends Item>... base) {
            this.first = Ingredient.of(Collections2.transform(List.of(base), NonNullSupplier::get).toArray(new Item[0]));
            this.second = Ingredient.of(Collections2.transform(List.of(base), NonNullSupplier::get).toArray(new Item[0]));
            return this;
        }

        public Builder input(ItemStack... base) {
            this.first = Ingredient.of(base);
            this.second = Ingredient.of(base);
            return this;
        }

        public Builder input(Ingredient base) {
            this.first = base;
            this.second = base;
            return this;
        }

        @SafeVarargs
        public final Builder first(NonNullSupplier<? extends Item>... base) {
            this.first = Ingredient.of(Collections2.transform(List.of(base), NonNullSupplier::get).toArray(new Item[0]));
            return this;
        }

        public Builder first(ItemStack... base) {
            this.first = Ingredient.of(base);
            return this;
        }

        public Builder first(Ingredient base) {
            this.first = base;
            return this;
        }

        @SafeVarargs
        public final Builder second(NonNullSupplier<? extends Item>... addition) {
            this.second = Ingredient.of(Collections2.transform(List.of(addition), NonNullSupplier::get).toArray(new Item[0]));
            return this;
        }

        public Builder second(ItemStack... addition) {
            this.second = Ingredient.of(addition);
            return this;
        }

        public Builder second(Ingredient addition) {
            this.second = addition;
            return this;
        }

        public Builder result(Item result) {
            this.result = result.getDefaultInstance();
            return this;
        }

        public Builder result(ItemEntry<? extends Item> result) {
            this.result = result.asStack();
            return this;
        }

        public Builder result(ItemStack result) {
            this.result = result;
            return this;
        }

        @Override
        public SmithingMultiphaseRecipe buildRecipe() {
            return new SmithingMultiphaseRecipe(this.first, this.second, this.result);
        }

        @Override
        public void validate(ResourceLocation pId) {
            if (this.first.isEmpty()) {
                throw new IllegalArgumentException("The first part of smithing recipe must not be empty, RecipeId: " + pId);
            }
            if (this.second.isEmpty()) {
                throw new IllegalArgumentException("The second part of smithing recipe must not be empty, RecipeId: " + pId);
            }
            if (this.result.isEmpty()) {
                throw new IllegalArgumentException("Recipe result must not be empty, RecipeId: " + pId);
            }
        }

        @Override
        public String getType() {
            return "smithing_multiphase";
        }

        @Override
        public Item getResult() {
            return this.result.getItem();
        }
    }
}
