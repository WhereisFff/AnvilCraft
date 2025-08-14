package dev.dubhe.anvilcraft.integration.kubejs.recipe.anvil;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.AnvilCraftKubeRecipe;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.IDRecipeConstructor;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.components.BlockStatePredicateComponent;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.components.ChanceBlockStateComponent;
import dev.dubhe.anvilcraft.integration.kubejs.recipe.components.HasCauldronSimpleComponent;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasCauldron;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface SqueezingRecipeSchema {
    @SuppressWarnings("unused")
    class SqueezingKubeRecipe extends AnvilCraftKubeRecipe {
        private final HasCauldronSimple.Builder hasCauldron = HasCauldronSimple.empty();

        public SqueezingKubeRecipe cauldron(ResourceLocation fluid) {
            this.hasCauldron.fluid(fluid);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe cauldron(Block cauldron) {
            this.cauldron(WrapUtils.cauldron2Fluid(cauldron));
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe transform(ResourceLocation transform) {
            this.hasCauldron.transform(transform);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe transform(Block transform) {
            this.hasCauldron.transform(WrapUtils.cauldron2Fluid(transform));
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe produceFluid(boolean produceFluid) {
            if (!produceFluid) return this;
            this.hasCauldron.consume(-1);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe consumeFluid(boolean consumeFluid) {
            if (!consumeFluid) return this;
            this.hasCauldron.consume(1);
            this.setValue(CAULDRON, hasCauldron.build());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe input(Block... block) {
            this.computeIfAbsent(INGREDIENTS, ArrayList::new)
                .add(BlockStatePredicate.builder().of(block).build());
            this.save();
            return this;
        }

        @SafeVarargs
        public final SqueezingKubeRecipe inputTag(TagKey<Block>... block) {
            this.computeIfAbsent(INGREDIENTS, ArrayList::new)
                .addAll(Arrays.stream(block).map(tag -> BlockStatePredicate.builder().of(tag).build()).toList());
            this.save();
            return this;
        }

        public SqueezingKubeRecipe result(Block... block) {
            this.computeIfAbsent(RESULTS, ArrayList::new)
                .addAll(Arrays.stream(block).map(b -> new ChanceBlockState(b.defaultBlockState(), 1.0f)).toList());
            this.save();
            return this;
        }

        @Override
        protected void validate() {
            if (getValue(CAULDRON) == null) {
                throw new KubeRuntimeException("input is empty!").source(sourceLine);
            }
            if (this.computeIfAbsent(INGREDIENTS, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Inputs is Empty!").source(sourceLine);
            }
            if (this.computeIfAbsent(RESULTS, ArrayList::new).isEmpty()) {
                throw new KubeRuntimeException("Result is Empty!").source(sourceLine);
            }
        }
    }

    RecipeKey<List<BlockStatePredicate>> INGREDIENTS = BlockStatePredicateComponent.INSTANCE
        .asList()
        .key("ingredients", ComponentRole.INPUT)
        .defaultOptional();
    RecipeKey<List<ChanceBlockState>> RESULTS = ChanceBlockStateComponent.INSTANCE
        .asList()
        .key("results", ComponentRole.OUTPUT)
        .defaultOptional();
    RecipeKey<HasCauldronSimple> CAULDRON = HasCauldronSimpleComponent.INSTANCE
        .key("cauldron", ComponentRole.OUTPUT)
        .optional(HasCauldronSimple.fluid(HasCauldron.EMPTY).build())
        .alwaysWrite();

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULTS, CAULDRON)
        .factory(new KubeRecipeFactory(AnvilCraft.of("squeezing"), SqueezingKubeRecipe.class, SqueezingKubeRecipe::new))
        .constructor(INGREDIENTS, RESULTS, CAULDRON)
        .constructor(INGREDIENTS, RESULTS)
        .constructor(new IDRecipeConstructor())
        .constructor();
}
