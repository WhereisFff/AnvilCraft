package dev.dubhe.anvilcraft.integration.kubejs.recipe.anvil;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.AnvilCraftKubeRecipe;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.IDRecipeConstructor;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.components.ChanceItemStackComponent;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.components.HasCauldronSimpleComponent;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.components.ItemIngredientPredicateComponent;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasCauldron;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface BulgingRecipeSchema {
    @SuppressWarnings({"unused"})
    class BulgingKubeRecipe extends AnvilCraftKubeRecipe {
        private final HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

        public BulgingKubeRecipe cauldron(ResourceLocation fluid) {
            this.hasCauldron.fluid(fluid);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe cauldron(Block cauldron) {
            this.cauldron(WrapUtils.cauldron2Fluid(cauldron));
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe transform(ResourceLocation transform) {
            this.hasCauldron.transform(transform);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe transform(Block transform) {
            this.hasCauldron.transform(WrapUtils.cauldron2Fluid(transform));
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe produceFluid(boolean produceFluid) {
            if (!produceFluid) return this;
            this.hasCauldron.consume(-1);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe consumeFluid(boolean consumeFluid) {
            if (!consumeFluid) return this;
            this.hasCauldron.consume(1);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe requires(@NotNull TagKey<Item> ingredient, int count) {
            this.computeIfAbsent(INGREDIENTS, ArrayList::new)
                .add(ItemIngredientPredicate.Builder.item().of(ingredient).withCount(count).build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe requires(@NotNull TagKey<Item> ingredient) {
            return this.requires(ingredient, 1);
        }

        public BulgingKubeRecipe requires(@NotNull ItemStack ingredient) {
            this.computeIfAbsent(INGREDIENTS, ArrayList::new)
                .add(ItemIngredientPredicate.Builder.item().of(ingredient).build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe requires(@NotNull ItemLike ingredient, int count) {
            this.computeIfAbsent(INGREDIENTS, ArrayList::new)
                .add(ItemIngredientPredicate.Builder.item().of(ingredient).withCount(count).build());
            this.save();
            return this;
        }

        public BulgingKubeRecipe requires(@NotNull ItemLike ingredient) {
            return this.requires(ingredient, 1);
        }

        public BulgingKubeRecipe result(@NotNull ItemStack result, NumberProvider count) {
            this.computeIfAbsent(RESULTS, ArrayList::new)
                .add(ChanceItemStack.of(result, count));
            this.save();
            return this;
        }

        public BulgingKubeRecipe result(@NotNull ItemStack result, float chance) {
            return this.result(result, BinomialDistributionGenerator.binomial(result.getCount(), chance));
        }

        public BulgingKubeRecipe result(@NotNull ItemStack result) {
            return this.result(result, ConstantValue.exactly(result.getCount()));
        }

        public BulgingKubeRecipe result(@NotNull ItemLike result, NumberProvider count) {
            this.computeIfAbsent(RESULTS, ArrayList::new)
                .add(ChanceItemStack.of(result, count));
            this.save();
            return this;
        }

        public BulgingKubeRecipe result(@NotNull ItemLike result, int count, float chance) {
            return this.result(result, BinomialDistributionGenerator.binomial(count, chance));
        }

        public BulgingKubeRecipe result(@NotNull ItemLike result, int count) {
            return this.result(result, ConstantValue.exactly(count));
        }

        public BulgingKubeRecipe result(@NotNull ItemLike result, float chance) {
            return this.result(result, 1, chance);
        }

        public BulgingKubeRecipe result(@NotNull ItemLike result) {
            return this.result(result, ConstantValue.exactly(1.0f));
        }

        @Override
        protected void validate() {
            if (computeIfAbsent(INGREDIENTS, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Inputs is Empty!").source(sourceLine);
            }
            if (computeIfAbsent(RESULTS, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Result is Empty!").source(sourceLine);
            }
        }
    }

    RecipeKey<List<ItemIngredientPredicate>> INGREDIENTS = ItemIngredientPredicateComponent.INSTANCE
        .asList()
        .inputKey("ingredients")
        .defaultOptional();
    RecipeKey<List<ChanceItemStack>> RESULTS = ChanceItemStackComponent.INSTANCE
        .asList()
        .inputKey("results")
        .defaultOptional();
    RecipeKey<HasCauldronSimple> CAULDRON = HasCauldronSimpleComponent.INSTANCE
        .outputKey("cauldron")
        .optional(HasCauldronSimple.fluid(HasCauldron.EMPTY).build())
        .alwaysWrite();

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULTS, CAULDRON)
        .factory(new KubeRecipeFactory(AnvilCraft.of("bulging"), BulgingKubeRecipe.class, BulgingKubeRecipe::new))
        .constructor(INGREDIENTS, RESULTS, CAULDRON)
        .constructor(INGREDIENTS, RESULTS)
        .constructor(new IDRecipeConstructor())
        .constructor();
}
