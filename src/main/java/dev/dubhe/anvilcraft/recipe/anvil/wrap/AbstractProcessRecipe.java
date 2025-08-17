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
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceBlockState;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import lombok.Getter;
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
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class AbstractProcessRecipe<T extends InWorldRecipe> extends InWorldRecipe {
    protected final Property property;

    public AbstractProcessRecipe(@NotNull Property property) {
        super(
            property.getIcon(),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            property.getConflictingPredicates(),
            property.getNonConflictingPredicates(),
            property.getOutcomes(),
            property.getPriority(),
            false
        );
        this.property = property;
    }

    @Override
    public abstract @NotNull RecipeSerializer<T> getSerializer();

    @Override
    public abstract @NotNull RecipeType<T> getType();

    public List<ItemIngredientPredicate> getInputItems() {
        return this.property.getInputItems();
    }

    public List<ChanceItemStack> getResultItems() {
        return this.property.getResultItems();
    }

    public List<BlockStatePredicate> getInputBlocks() {
        return this.property.getInputBlocks();
    }

    public List<ChanceBlockState> getResultBlocks() {
        return this.property.getResultBlocks();
    }

    public HasCauldronSimple getHasCauldron() {
        return this.property.getHasCauldron();
    }

    public abstract static class AbstractSerializer<T extends AbstractProcessRecipe<T>> implements RecipeSerializer<T> {
        protected final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemIngredientPredicate.CODEC.listOf()
                .optionalFieldOf("ingredients", List.of())
                .forGetter(T::getInputItems),
            ChanceItemStack.CODEC.listOf()
                .optionalFieldOf("results", List.of())
                .forGetter(T::getResultItems)
        ).apply(instance, this::of));

        protected final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.composite(
            ItemIngredientPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
            T::getInputItems,
            ChanceItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            T::getResultItems,
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

        public B requires(@NotNull ItemStack ingredient) {
            this.itemIngredients.add(ItemIngredientPredicate.Builder.item().of(ingredient).build());
            return this.getThis();
        }

        public B requires(@NotNull ItemLike ingredient, int count) {
            return this.requires(ItemIngredientPredicate.Builder.item().of(ingredient).withCount(count).build());
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

    @Getter
    public static class Property {
        private Vec3 itemInputOffset = Vec3.ZERO;
        private Vec3 itemInputRange = new Vec3(1, 1, 1);
        private List<ItemIngredientPredicate> inputItems = null;
        private Vec3 itemOutputOffset = Vec3.ZERO;
        private List<ChanceItemStack> resultItems = null;
        private Vec3 blockInputOffset = Vec3.ZERO;
        private List<BlockStatePredicate> inputBlocks = null;
        private Vec3 blockOutputOffset = Vec3.ZERO;
        private List<ChanceBlockState> resultBlocks = null;
        private Vec3 cauldronOffset = Vec3.ZERO;
        private HasCauldronSimple hasCauldron = null;
        private Integer priority = null;

        public Property setItemInputOffset(Vec3 itemInputOffset) {
            this.itemInputOffset = itemInputOffset;
            return this;
        }

        public Property setItemInputRange(Vec3 itemInputRange) {
            this.itemInputRange = itemInputRange;
            return this;
        }

        public Property setInputItems(List<ItemIngredientPredicate> inputItems) {
            this.inputItems = inputItems;
            return this;
        }

        public Property setInputItems(ItemIngredientPredicate... inputItems) {
            return this.setInputItems(Arrays.asList(inputItems));
        }

        public Property setItemOutputOffset(Vec3 itemOutputOffset) {
            this.itemOutputOffset = itemOutputOffset;
            return this;
        }

        public Property setResultItems(List<ChanceItemStack> resultItems) {
            this.resultItems = resultItems;
            return this;
        }

        public Property setResultItems(ChanceItemStack... resultItems) {
            return this.setResultItems(Arrays.asList(resultItems));
        }

        public Property setBlockInputOffset(Vec3 blockInputOffset) {
            this.blockInputOffset = blockInputOffset;
            return this;
        }

        public Property setInputBlocks(List<BlockStatePredicate> inputBlocks) {
            this.inputBlocks = inputBlocks;
            return this;
        }

        public Property setInputBlocks(BlockStatePredicate... inputBlocks) {
            return this.setInputBlocks(Arrays.asList(inputBlocks));
        }

        public Property setBlockOutputOffset(Vec3 blockOutputOffset) {
            this.blockOutputOffset = blockOutputOffset;
            return this;
        }

        public Property setResultBlocks(List<ChanceBlockState> resultBlocks) {
            this.resultBlocks = resultBlocks;
            return this;
        }

        public Property setResultBlocks(ChanceBlockState... resultBlocks) {
            return this.setResultBlocks(Arrays.asList(resultBlocks));
        }

        public Property setCauldronOffset(Vec3 cauldronOffset) {
            this.cauldronOffset = cauldronOffset;
            return this;
        }

        public Property setHasCauldron(HasCauldronSimple hasCauldron) {
            this.hasCauldron = hasCauldron;
            return this;
        }

        public Property setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        private @NotNull ItemStack getIcon() {
            ItemStack icon = null;
            if (this.resultItems != null && !this.resultItems.isEmpty()) {
                icon = this.resultItems.getFirst().getStack();
            }
            if (icon == null && this.resultBlocks != null && !this.resultBlocks.isEmpty()) {
                Item item = this.resultBlocks.getFirst().getState().getBlock().asItem();
                if (item != Items.AIR) icon = item.getDefaultInstance();
            }
            if (icon == null) icon = Items.ANVIL.getDefaultInstance();
            return icon;
        }

        private int getPriority() {
            if (this.priority != null) return this.priority;
            return (this.inputItems == null ? 0 : this.inputItems.size())
                + (this.resultItems == null ? 0 : this.resultItems.size())
                + (this.inputBlocks == null ? 0 : this.inputBlocks.size() * 100)
                + (this.resultBlocks == null ? 0 : this.resultBlocks.size())
                + (this.hasCauldron != null ? 1 : 0);
        }

        private @NotNull List<IRecipePredicate<?>> getNonConflictingPredicates() {
            List<IRecipePredicate<?>> predicates = new ArrayList<>();
            if (this.hasCauldron != null) {
                predicates.add(this.hasCauldron.toHasCauldron(this.cauldronOffset));
            }
            if (this.inputBlocks != null) {
                for (int i = 0; i < this.inputBlocks.size(); i++) {
                    BlockStatePredicate block = this.inputBlocks.get(i);
                    predicates.add(new HasBlock(this.blockInputOffset.subtract(0, i, 0), block));
                }
            }
            return predicates;
        }

        private @NotNull List<IRecipePredicate<?>> getConflictingPredicates() {
            List<IRecipePredicate<?>> predicates = new ArrayList<>();
            if (this.inputItems != null) {
                for (ItemIngredientPredicate ingredient : this.inputItems) {
                    predicates.add(ingredient.toHasItemIngredient(this.itemInputOffset, this.itemInputRange));
                }
            }
            return predicates;
        }

        private @NotNull List<IRecipeOutcome<?>> getOutcomes() {
            List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
            if (this.resultItems != null) {
                for (ChanceItemStack chanceItemStack : this.resultItems) {
                    outcomes.add(chanceItemStack.toSpawnItem(this.itemOutputOffset));
                }
            }
            if (this.resultBlocks != null) {
                for (int i = 0; i < this.resultBlocks.size(); i++) {
                    ChanceBlockState chanceBlockState = this.resultBlocks.get(i);
                    outcomes.add(chanceBlockState.toSetBlock(this.blockOutputOffset.subtract(0, i, 0)));
                }
            }
            return outcomes;
        }
    }
}
