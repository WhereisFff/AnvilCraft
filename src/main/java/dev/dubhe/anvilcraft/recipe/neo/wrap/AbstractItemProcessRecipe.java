package dev.dubhe.anvilcraft.recipe.neo.wrap;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTriggers;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Getter
public abstract class AbstractItemProcessRecipe<T extends InWorldRecipe> extends InWorldRecipe {
    protected final Vec3 inputOffset;
    protected final List<ItemIngredientPredicate> itemIngredients;
    protected final Vec3 outputOffset;
    protected final List<ChanceItemStack> results;
    protected final Vec3 blockOffset;
    protected final List<BlockStatePredicate> block;

    public AbstractItemProcessRecipe(
        Vec3 inputOffset,
        List<ItemIngredientPredicate> itemIngredients,
        Vec3 outputOffset,
        List<ChanceItemStack> results,
        Vec3 blockOffset,
        BlockStatePredicate... block
    ) {
        super(
            AbstractItemProcessRecipe.getIcon(results),
            ModRecipeTriggers.ON_ANVIL_FALL_ON.get(),
            List.of(),
            AbstractItemProcessRecipe.getPredicates(inputOffset, itemIngredients, blockOffset, block),
            AbstractItemProcessRecipe.getOutcomes(outputOffset, results),
            0,
            false
        );
        this.inputOffset = inputOffset;
        this.itemIngredients = itemIngredients;
        this.outputOffset = outputOffset;
        this.results = results;
        this.blockOffset = blockOffset;
        this.block = List.of(block);
    }

    private static ItemStack getIcon(@NotNull List<ChanceItemStack> results) {
        return results.isEmpty() ? Items.ANVIL.getDefaultInstance() : results.getFirst().getStack();
    }

    private static @NotNull List<IRecipePredicate<?>> getPredicates(
        Vec3 inputOffset,
        @NotNull List<ItemIngredientPredicate> ingredients,
        Vec3 blockOffset,
        BlockStatePredicate... blocks
    ) {
        List<IRecipePredicate<?>> predicates = new ArrayList<>();
        for (ItemIngredientPredicate ingredient : ingredients) {
            predicates.add(ingredient.toHasItemIngredient(inputOffset, new Vec3(1, 0.5, 1)));
        }
        for (int i = 0; i < blocks.length; i++) {
            BlockStatePredicate block = blocks[i];
            predicates.add(new HasBlock(blockOffset.subtract(0, i, 0), block));
        }
        return predicates;
    }

    private static @NotNull List<IRecipeOutcome<?>> getOutcomes(
        Vec3 outputOffset,
        @NotNull List<ChanceItemStack> results
    ) {
        List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
        for (ChanceItemStack chanceItemStack : results) {
            outcomes.add(chanceItemStack.toSpawnItem(outputOffset));
        }
        return outcomes;
    }

    @Override
    public abstract @NotNull RecipeSerializer<T> getSerializer();

    @Override
    public abstract @NotNull RecipeType<T> getType();

    public static abstract class AbstractSerializer<T extends AbstractItemProcessRecipe<T>> implements RecipeSerializer<T> {
        protected MapCodec<T> codec = null;
        protected StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = null;

        protected abstract T of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results);

        @Override
        public @NotNull MapCodec<T> codec() {
            if (this.codec == null) {
                codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemIngredientPredicate.CODEC.listOf()
                        .fieldOf("ingredients")
                        .forGetter(T::getItemIngredients),
                    ChanceItemStack.CODEC.listOf()
                        .fieldOf("results")
                        .forGetter(T::getResults)
                ).apply(instance, this::of));
            }
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            if (this.streamCodec == null) {
                this.streamCodec = StreamCodec.of(this::encode, this::decode);
            }
            return this.streamCodec;
        }

        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull T recipe) {
            buf.writeVarInt(recipe.getItemIngredients().size());
            for (ItemIngredientPredicate itemIngredient : recipe.getItemIngredients()) {
                RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())).createSerializationContext(NbtOps.INSTANCE);
                DataResult<Tag> encode = ItemIngredientPredicate.CODEC.encode(itemIngredient, ops, ops.empty());
                Tag tag = encode.getOrThrow();
                buf.writeNbt(tag);
            }
            buf.writeVarInt(recipe.results.size());
            for (ChanceItemStack result : recipe.results) {
                ChanceItemStack.STREAM_CODEC.encode(buf, result);
            }
        }

        public @NotNull T decode(@NotNull RegistryFriendlyByteBuf buf) {
            int i = buf.readVarInt();
            List<ItemIngredientPredicate> ingredients = new ArrayList<>();
            for (; i > 0; i--) {
                RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())).createSerializationContext(NbtOps.INSTANCE);
                ingredients.add(ItemIngredientPredicate.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst());
            }
            List<ChanceItemStack> results = new ArrayList<>();
            i = buf.readVarInt();
            for (; i > 0; i--) {
                results.add(ChanceItemStack.STREAM_CODEC.decode(buf));
            }
            return this.of(ingredients, results);
        }
    }

    public static abstract class AbstractBuilder<T extends AbstractItemProcessRecipe<T>, B extends AbstractBuilder<T, B>> extends AbstractRecipeBuilder<T> {

        private final List<ItemIngredientPredicate> itemIngredients = new ArrayList<>();
        private final List<ChanceItemStack> results = new ArrayList<>();

        protected abstract T of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results);

        protected abstract B getThis();

        public B requires(@NotNull ItemIngredientPredicate ingredient) {
            this.itemIngredients.add(ingredient);
            return this.getThis();
        }

        public B requires(@NotNull TagKey<Item> ingredient) {
            this.itemIngredients.add(ItemIngredientPredicate.Builder.item().of(ingredient).build());
            return this.getThis();
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

        public B result(@NotNull ItemStack result, double chance) {
            this.results.add(ChanceItemStack.of(result, chance));
            return this.getThis();
        }

        public B result(@NotNull ItemStack result) {
            return this.result(result, 1.0);
        }

        public B result(@NotNull ItemLike result, double chance) {
            return this.result(result.asItem().getDefaultInstance(), chance);
        }

        public B result(@NotNull ItemLike result) {
            return this.result(result, 1.0);
        }

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

        @Override
        public @NotNull Item getResult() {
            return results.isEmpty() ? Items.ANVIL : results.getFirst().getItem();
        }
    }
}
