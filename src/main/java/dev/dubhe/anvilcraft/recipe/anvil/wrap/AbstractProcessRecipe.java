package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.anvil.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.util.HasCauldronSimple;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class AbstractProcessRecipe<T extends InWorldRecipe> extends InWorldRecipe {
    protected final Vec3 inputOffset;
    protected final List<ItemIngredientPredicate> itemIngredients;
    protected final Vec3 outputOffset;
    protected final List<ChanceItemStack> results;
    protected final Vec3 cauldronOffset;
    protected final HasCauldronSimple hasCauldron;
    protected final Vec3 blockOffset;
    protected final List<BlockStatePredicate> blocks;
    protected final List<ChanceBlockState> resultBlocks;

    public AbstractProcessRecipe(
        Vec3 inputOffset,
        List<ItemIngredientPredicate> itemIngredients,
        Vec3 outputOffset,
        List<ChanceItemStack> results,
        Vec3 cauldronOffset,
        HasCauldronSimple hasCauldron,
        Vec3 blockOffset,
        List<ChanceBlockState> resultBlocks,
        List<BlockStatePredicate> blocks
    ) {
        super(
            AbstractProcessRecipe.getIcon(results),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            AbstractProcessRecipe.getPredicates(cauldronOffset, hasCauldron, blockOffset, blocks),
            AbstractProcessRecipe.getPredicates(inputOffset, itemIngredients),
            AbstractProcessRecipe.getOutcomes(outputOffset, results, resultBlocks),
            0,
            false
        );
        this.inputOffset = inputOffset;
        this.itemIngredients = itemIngredients;
        this.outputOffset = outputOffset;
        this.results = results;
        this.cauldronOffset = cauldronOffset;
        this.hasCauldron = hasCauldron;
        this.blockOffset = blockOffset;
        this.blocks = blocks;
        this.resultBlocks = resultBlocks;
    }

    public AbstractProcessRecipe(
        Vec3 inputOffset,
        List<ItemIngredientPredicate> itemIngredients,
        Vec3 outputOffset,
        List<ChanceItemStack> results,
        Vec3 blockOffset,
        HasCauldronSimple hasCauldron,
        BlockStatePredicate... blocks
    ) {
        this(
            inputOffset,
            itemIngredients,
            outputOffset,
            results,
            blockOffset,
            hasCauldron,
            blockOffset.subtract(0, 1, 0),
            null,
            List.of(blocks)
        );
    }

    public AbstractProcessRecipe(
        Vec3 inputOffset,
        List<ItemIngredientPredicate> itemIngredients,
        Vec3 outputOffset,
        List<ChanceItemStack> results,
        Vec3 blockOffset,
        BlockStatePredicate... blocks
    ) {
        this(inputOffset, itemIngredients, outputOffset, results, blockOffset, null, blocks);
    }

    private static ItemStack getIcon(@NotNull List<ChanceItemStack> results) {
        return results.isEmpty() ? Items.ANVIL.getDefaultInstance() : results.getFirst().getStack();
    }

    private static @NotNull List<IRecipePredicate<?>> getPredicates(
        Vec3 cauldronOffset,
        HasCauldronSimple hasCauldron,
        Vec3 blockOffset,
        List<BlockStatePredicate> blocks
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        if (hasCauldron != null) {
            predicates.add(hasCauldron.toHasCauldron(cauldronOffset));
        }
        if (blocks != null) {
            for (int i = 0; i < blocks.size(); i++) {
                BlockStatePredicate block = blocks.get(i);
                predicates.add(new HasBlock(blockOffset.subtract(0, i, 0), block));
            }
        }
        return predicates;
    }

    private static @NotNull List<IRecipePredicate<?>> getPredicates(
        Vec3 inputOffset,
        @NotNull List<ItemIngredientPredicate> ingredients
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (ItemIngredientPredicate ingredient : ingredients) {
            predicates.add(ingredient.toHasItemIngredient(inputOffset, new Vec3(1, 0.5, 1)));
        }
        return predicates;
    }

    private static @NotNull List<IRecipeOutcome<?>> getOutcomes(
        Vec3 outputOffset,
        @NotNull List<ChanceItemStack> results,
        List<ChanceBlockState> setBlocks
    ) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (ChanceItemStack chanceItemStack : results) {
            outcomes.add(chanceItemStack.toSpawnItem(outputOffset));
        }
        if (setBlocks != null) {
            for (int i = 0; i < setBlocks.size(); i++) {
                ChanceBlockState chanceBlockState = setBlocks.get(i);
                outcomes.add(chanceBlockState.toSetBlock(new Vec3(0, -i - 1, 0)));
            }
        }
        return outcomes;
    }

    @Override
    public abstract @NotNull RecipeSerializer<T> getSerializer();

    @Override
    public abstract @NotNull RecipeType<T> getType();

    public abstract static class AbstractSerializer<T extends AbstractProcessRecipe<T>> implements RecipeSerializer<T> {
        protected final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf()
                .optionalFieldOf("ingredients", List.of())
                .forGetter(T::getItemIngredients),
            ChanceItemStack.CODEC.listOf()
                .optionalFieldOf("results", List.of())
                .forGetter(T::getResults)
        ).apply(instance, this::of));

        protected final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            T::getItemIngredients,
            ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            T::getResults,
            this::of
        );

        protected abstract T of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results);

        @Override
        public @NotNull MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }
    }

    public abstract static class AbstractBuilder<
        T extends AbstractProcessRecipe<T>,
        B extends AbstractBuilder<T, B>
        > extends AbstractRecipeBuilder<T> {

        protected final List<ItemIngredientPredicate> itemIngredients = new ArrayList<>();
        protected final List<ChanceItemStack> results = new ArrayList<>();

        protected abstract B getThis();

        public B requires(@NotNull ItemIngredientPredicate ingredient) {
            this.itemIngredients.add(ingredient);
            return this.getThis();
        }

        public B requires(@NotNull TagKey<Item> ingredient, int count) {
            this.itemIngredients.add(ItemIngredientPredicate.Builder.item().of(ingredient).withCount(count).build());
            return this.getThis();
        }

        public B requires(@NotNull TagKey<Item> ingredient) {
            return this.requires(ingredient, 1);
        }

        public <D> B requires(@NotNull ItemStack ingredient) {
            Item item = ingredient.getItem();
            ItemStack defaultInstance = item.getDefaultInstance();
            ItemIngredientPredicate.Builder predicate = ItemIngredientPredicate.Builder.item().of(item);
            for (TypedDataComponent<?> component : item.components()) {
                Object o = defaultInstance.get(component.type());
                if (o != null && o.equals(component.value())) continue;
                //noinspection unchecked
                predicate.hasComponents(
                    DataComponentPredicate.builder()
                        .expect((DataComponentType<D>) component.type(), (D) component.value())
                        .build()
                );
            }
            this.itemIngredients.add(predicate.withCount(ingredient.getCount()).build());
            return this.getThis();
        }

        public B requires(@NotNull ItemLike ingredient, int count) {
            ItemStack stack = ingredient.asItem().getDefaultInstance();
            stack.setCount(count);
            return this.requires(stack);
        }

        public B requires(@NotNull ItemLike ingredient) {
            return this.requires(ingredient, 1);
        }

        public B result(@NotNull ItemStack result, NumberProvider count) {
            this.results.add(ChanceItemStack.of(result, count));
            return this.getThis();
        }

        public B result(@NotNull ItemStack result, float chance) {
            return this.result(result, BinomialDistributionGenerator.binomial(result.getCount(), chance));
        }

        public B result(@NotNull ItemStack result) {
            return this.result(result, ConstantValue.exactly(result.getCount()));
        }

        public B result(@NotNull ItemLike result, NumberProvider count) {
            this.results.add(ChanceItemStack.of(result, count));
            return this.getThis();
        }

        public B result(@NotNull ItemLike result, int count, float chance) {
            return this.result(result, BinomialDistributionGenerator.binomial(count, chance));
        }

        public B result(@NotNull ItemLike result, int count) {
            return this.result(result, ConstantValue.exactly(count));
        }

        public B result(@NotNull ItemLike result, float chance) {
            return this.result(result, 1, chance);
        }

        public B result(@NotNull ItemLike result) {
            return this.result(result, ConstantValue.exactly(1.0f));
        }

        @Override
        public @NotNull Item getResult() {
            return results.isEmpty() ? Items.ANVIL : results.getFirst().getItem();
        }
    }

    public abstract static class SimpleAbstractBuilder<
        T extends AbstractProcessRecipe<T>,
        B extends SimpleAbstractBuilder<T, B>
        > extends AbstractBuilder<T, B> {
        protected abstract T of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results);

        @Override
        public @NotNull T buildRecipe() {
            return this.of(this.itemIngredients, this.results);
        }

        @Override
        public void validate(@NotNull ResourceLocation pId) {
            if (itemIngredients.isEmpty()) {
                throw new IllegalArgumentException("Recipe ingredients must not be empty, RecipeId: " + pId);
            }
            if (results.isEmpty()) {
                throw new IllegalArgumentException("Recipe result must not be empty, RecipeId: " + pId);
            }
        }
    }
}
