package dev.dubhe.anvilcraft.recipe.frost;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.recipe.component.ItemIngredientPredicate;
import dev.anvilcraft.lib.util.CodecUtil;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.recipe.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public record DeformationRecipe(
    ItemIngredientPredicate template,
    List<Item> inputs
) implements IFrostSmithingRecipe {
    public static final ItemIngredientPredicate DEFAULT_TEMPLATE = ItemIngredientPredicate.of(ModItems.DEFORMATION_TEMPLATE_ITEM).build();

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean isTemplate(ItemStack template) {
        return this.template.test(template);
    }

    @Override
    public boolean isMaterial(ItemStack material) {
        return material.isEmpty();
    }

    @Override
    public boolean isInput(ItemStack input) {
        for (Item input1 : this.inputs) {
            if (input.is(input1)) return true;
        }
        return false;
    }

    @Deprecated
    @Override
    public ItemStack assemble(FrostSmithingRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    public @Unmodifiable List<Item> getResults(ItemStack input) {
        int head;
        for (head = 0; head < this.inputs.size(); head++) {
            if (input.is(this.inputs.get(head))) break;
        }

        ImmutableList.Builder<Item> results = ImmutableList.builder();
        for (int i = 1; i < this.inputs.size(); i++) {
            results.add(this.inputs.get((head + i) % this.inputs.size()));
        }
        return results.build();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.inputs.getFirst().getDefaultInstance();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.DEFORMATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.DEFORMATION_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DeformationRecipe> {
        private static final MapCodec<DeformationRecipe> CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            ItemIngredientPredicate.CODEC
                .optionalFieldOf("template", DeformationRecipe.DEFAULT_TEMPLATE)
                .forGetter(DeformationRecipe::template),
            CodecUtil.ITEM_CODEC
                .listOf()
                .fieldOf("inputs")
                .forGetter(DeformationRecipe::inputs)
        ).apply(ins, DeformationRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DeformationRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC,
            DeformationRecipe::template,
            CodecUtil.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()),
            DeformationRecipe::inputs,
            DeformationRecipe::new
        );

        @Override
        public MapCodec<DeformationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DeformationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    @Accessors(fluent = true, chain = true)
    @Setter
    public static class Builder extends AbstractRecipeBuilder<DeformationRecipe> {
        private ItemIngredientPredicate template = DeformationRecipe.DEFAULT_TEMPLATE;
        private final List<Item> inputs = new ArrayList<>();

        public Builder() {
        }

        public final Builder template(ItemIngredientPredicate.Builder templateBuilder) {
            this.template = templateBuilder.build();
            return this;
        }

        public final Builder template(int count, ItemStack template) {
            return this.template(
                ItemIngredientPredicate.of(template.getItem())
                    .withCount(count)
                    .hasComponents(DataComponentPredicate.allOf(template.getComponents())));
        }

        public final Builder template(ItemStack template) {
            return this.template(1, template);
        }

        public final Builder template(int count, ItemLike... templates) {
            return this.template(ItemIngredientPredicate.of(templates).withCount(count));
        }

        public final Builder template(ItemLike... templates) {
            return this.template(1, templates);
        }

        public final Builder template(int count, TagKey<Item> templateTag) {
            return this.template(ItemIngredientPredicate.of(templateTag).withCount(count));
        }

        public final Builder template(TagKey<Item> templateTag) {
            return this.template(1, templateTag);
        }

        public final Builder input(ItemLike input) {
            this.inputs.add(input.asItem());
            return this;
        }

        public final Builder input(ItemLike... inputs) {
            for (ItemLike input : inputs) {
                this.inputs.add(input.asItem());
            }
            return this;
        }

        @Override
        public DeformationRecipe buildRecipe() {
            return new DeformationRecipe(
                this.template,
                this.inputs
            );
        }

        @Override
        public void validate(ResourceLocation id) {
            if (this.inputs.isEmpty()) {
                throw new IllegalArgumentException("The inputs of permutation recipe must not be empty, RecipeId: " + id);
            }
        }

        @Override
        public String getType() {
            return "deformation";
        }

        @Override
        public Item getResult() {
            return this.inputs.getFirst();
        }

        @Deprecated
        @Override
        public void save(RecipeOutput output) {
            this.save(output, BuiltInRegistries.ITEM.getKey(this.inputs.getFirst()).withPrefix("deformation/"));
        }
    }
}
