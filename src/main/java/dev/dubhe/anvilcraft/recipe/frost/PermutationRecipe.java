package dev.dubhe.anvilcraft.recipe.frost;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.anvilcraft.lib.recipe.component.ItemIngredientPredicate;
import dev.anvilcraft.lib.util.CodecUtil;
import dev.dubhe.anvilcraft.init.item.ModComponentTags;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.recipe.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import java.util.HashSet;
import java.util.Set;

public record PermutationRecipe(
    ItemIngredientPredicate template,
    ItemIngredientPredicate material,
    Item inputA,
    Item inputB
) implements IFrostSmithingRecipe {
    private static final Set<DataComponentType<?>> SPECIALS = new HashSet<>();
    private static final Runnable INVALIDATOR = () -> {
        SPECIALS.clear();
        specialsValid = false;
    };
    private static boolean addedInvalidator = false;
    private static boolean specialsValid = false;
    public static final ItemIngredientPredicate DEFAULT_TEMPLATE = ItemIngredientPredicate.of(ModItems.PERMUTATION_TEMPLATE_ITEM).build();

    public static Builder builder(ItemLike inputA, ItemLike inputB) {
        return new Builder(inputA, inputB);
    }

    @Override
    public boolean isTemplate(ItemStack template) {
        return this.template.test(template);
    }

    @Override
    public boolean isMaterial(ItemStack material) {
        return this.material.test(material);
    }

    @Override
    public boolean isInput(ItemStack input) {
        return input.is(this.inputA) || input.is(this.inputB);
    }

    @Override
    public ItemStack assemble(FrostSmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack result = input.input().transmuteCopy(input.input().is(this.inputA) ? this.inputB : this.inputA);
        if (!PermutationRecipe.specialsValid) {
            var lookup = registries.lookup(Registries.DATA_COMPONENT_TYPE).orElseThrow();
            var specials = lookup.getOrThrow(ModComponentTags.TOOLS_SPECIAL);
            if (!PermutationRecipe.addedInvalidator) {
                specials.addInvalidationListener(PermutationRecipe.INVALIDATOR);
                PermutationRecipe.addedInvalidator = true;
            }
            for (Holder<DataComponentType<?>> special : specials) {
                PermutationRecipe.SPECIALS.add(special.value());
            }
            PermutationRecipe.specialsValid = true;
        }
        result.applyComponents(result.getComponentsPatch().forget(PermutationRecipe.SPECIALS::contains));
        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.inputB.getDefaultInstance();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.PERMUTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.PERMUTATION_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<PermutationRecipe> {
        private static final MapCodec<PermutationRecipe> CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            ItemIngredientPredicate.CODEC
                .optionalFieldOf("template", PermutationRecipe.DEFAULT_TEMPLATE)
                .forGetter(PermutationRecipe::template),
            ItemIngredientPredicate.CODEC
                .fieldOf("material")
                .forGetter(PermutationRecipe::material),
            CodecUtil.ITEM_CODEC
                .fieldOf("inputA")
                .forGetter(PermutationRecipe::inputA),
            CodecUtil.ITEM_CODEC
                .fieldOf("inputB")
                .forGetter(PermutationRecipe::inputB)
        ).apply(ins, PermutationRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PermutationRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC,
            PermutationRecipe::template,
            ItemIngredientPredicate.STREAM_CODEC,
            PermutationRecipe::material,
            CodecUtil.ITEM_STREAM_CODEC,
            PermutationRecipe::inputA,
            CodecUtil.ITEM_STREAM_CODEC,
            PermutationRecipe::inputB,
            PermutationRecipe::new
        );

        @Override
        public MapCodec<PermutationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PermutationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    @Accessors(fluent = true, chain = true)
    @Setter
    public static class Builder extends AbstractRecipeBuilder<PermutationRecipe> {
        private ItemIngredientPredicate template = PermutationRecipe.DEFAULT_TEMPLATE;
        private ItemIngredientPredicate material;
        private final Item inputA;
        private final Item inputB;

        public Builder(ItemLike inputA, ItemLike inputB) {
            this.inputA = inputA.asItem();
            this.inputB = inputB.asItem();
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

        public final Builder material(ItemIngredientPredicate.Builder materialBuilder) {
            this.material = materialBuilder.build();
            return this;
        }

        public final Builder material(int count, ItemStack material) {
            return this.material(
                ItemIngredientPredicate.of(material.getItem())
                    .withCount(count)
                    .hasComponents(DataComponentPredicate.allOf(material.getComponents())));
        }

        public final Builder material(ItemStack material) {
            return this.material(1, material);
        }

        public final Builder material(int count, ItemLike... materials) {
            return this.material(ItemIngredientPredicate.of(materials).withCount(count));
        }

        public final Builder material(ItemLike... materials) {
            return this.material(1, materials);
        }

        public final Builder material(int count, TagKey<Item> materialTag) {
            return this.material(ItemIngredientPredicate.of(materialTag).withCount(count));
        }

        public final Builder material(TagKey<Item> materialTag) {
            return this.material(1, materialTag);
        }

        @Override
        public PermutationRecipe buildRecipe() {
            return new PermutationRecipe(
                this.template,
                this.material,
                this.inputA,
                this.inputB
            );
        }

        @Override
        public void validate(ResourceLocation id) {
            if (this.material.items().isEmpty()) {
                throw new IllegalArgumentException("The material of permutation recipe must not be empty, RecipeId: " + id);
            }
        }

        @Override
        public String getType() {
            return "permutation";
        }

        @Override
        public Item getResult() {
            return this.inputB;
        }

        /**
         * 保存配方到指定位置
         *
         * @param output 配方输出
         */
        @Override
        public void save(RecipeOutput output) {
            this.save(output, Builder.defaultId(this.inputA, this.inputB));
        }

        private static String defaultId(Item inputA, Item inputB) {
            ResourceLocation inputAId = BuiltInRegistries.ITEM.getKey(inputA);
            String inputBPath = BuiltInRegistries.ITEM.getKey(inputB).getPath();
            return inputAId.withSuffix("_and_" + inputBPath).getPath();
        }
    }
}
