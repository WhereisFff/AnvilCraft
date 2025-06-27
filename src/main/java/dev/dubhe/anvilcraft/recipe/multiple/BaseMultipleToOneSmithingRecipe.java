package dev.dubhe.anvilcraft.recipe.multiple;

import com.google.common.collect.Collections2;
import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.dubhe.anvilcraft.api.item.IMultipleResult;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.builder.AbstractRecipeBuilder;
import dev.dubhe.anvilcraft.util.CodecUtil;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BaseMultipleToOneSmithingRecipe<T extends Item & IMultipleResult>
    implements Recipe<MultipleToOneSmithingRecipeInput> {

    @SuppressWarnings("unchecked")
    protected static <T extends Item & IMultipleResult> Codec<T> resultCodec() {
        return CodecUtil.ITEM_CODEC.flatXmap(
            item -> item instanceof IMultipleResult
                    ? DataResult.success((T) item)
                    : DataResult.error(() -> "Item " + item + " is not instance of IMultipleResult"),
            DataResult::success
        );
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Item & IMultipleResult> StreamCodec<RegistryFriendlyByteBuf, T> resultStreamCodec() {
        return CodecUtil.ITEM_STREAM_CODEC.map(item -> (T) item, t -> t);
    }

    @Getter
    protected final Ingredient template;
    @Getter
    protected final Ingredient material;
    @Getter
    protected final List<Ingredient> inputs;
    @Getter
    protected final T result;
    protected final int recipeId;

    protected BaseMultipleToOneSmithingRecipe(Ingredient template, Ingredient material, List<Ingredient> inputs, T result, int recipeId) {
        this.template = template;
        this.material = material;
        this.inputs = inputs;
        this.result = result;
        this.recipeId = recipeId;
    }

    protected BaseMultipleToOneSmithingRecipe(Data<T> data) {
        this.template = data.template;
        this.material = data.material;
        this.inputs = data.inputs;
        this.result = data.result;
        this.recipeId = data.recipeId;
    }

    protected static <T extends Item & IMultipleResult, R extends BaseMultipleToOneSmithingRecipe<T>>
    Builder<T, R> builder(
        Ingredient template, NonNullSupplier<T> resultGetter, int inputSize, int recipeId,
        Function5<Ingredient, Ingredient, List<Ingredient>, T, Integer, R> factory
    ) {
        return new Builder<>(template, resultGetter, inputSize, recipeId, factory);
    }

    protected static <T extends Item & IMultipleResult, R extends BaseMultipleToOneSmithingRecipe<T>>
    Builder<T, R> builder(
        Ingredient template, T result, int inputSize, int recipeId, Function5<Ingredient, Ingredient, List<Ingredient>, T, Integer, R> factory
    ) {
        return new Builder<>(template, result, inputSize, recipeId, factory);
    }

    public int inputSize() {
        return this.inputs.size();
    }

    @Override
    public boolean matches(MultipleToOneSmithingRecipeInput input, Level level) {
        if (input.inputs().size() != this.inputs.size()) return false;
        return this.isTemplateIngredient(input.template())
               && this.isMaterialIngredient(input.material())
               && input.inputs().size() == 1
               ? this.inputs.getFirst().test(input.getItem(0))
               : this.matchesInput(input);
    }

    protected boolean matchesInput(MultipleToOneSmithingRecipeInput input) {
        int result = 0;
        List<Ingredient> ingredientsCloned = new ArrayList<>(this.inputs);
        List<ItemStack> inputsCloned = new ArrayList<>(input.inputs());

        Iterator<Ingredient> ingredientIt = ingredientsCloned.iterator();
        while (ingredientIt.hasNext()) {
            Ingredient ingredient = ingredientIt.next();
            Iterator<ItemStack> inputIt = inputsCloned.iterator();
            while (inputIt.hasNext()) {
                ItemStack stack = inputIt.next();
                if (ingredient.test(stack)) {
                    result++;
                    ingredientIt.remove();
                    inputIt.remove();
                    break;
                }
            }
        }
        return result == this.inputSize();
    }

    @Override
    public ItemStack assemble(MultipleToOneSmithingRecipeInput input, HolderLookup.Provider registries) {
        return this.result.assemble(this.recipeId, input, registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.inputSize() + 2 && height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.copyOf(this.inputs);
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.MULTIPLE_TO_ONE_SMITHING_TYPE.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        return ModBlocks.EMBER_SMITHING_TABLE.asStack();
    }

    public boolean isTemplateIngredient(ItemStack template) {
        return this.template.test(template);
    }

    public boolean isMaterialIngredient(ItemStack material) {
        return this.material.test(material);
    }

    public boolean isInputIngredient(int index, ItemStack input) {
        if (index >= this.inputSize()) return false;
        for (Ingredient ingredient : this.inputs) {
            if (ingredient.test(input)) return true;
        }
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    private Data<T> toData() {
        return new Data<>(this.template, this.material, this.inputs, this.result, this.recipeId);
    }

    public record Data<T extends Item & IMultipleResult>(
        Ingredient template, Ingredient material, List<Ingredient> inputs, T result, int recipeId
    ) {
        public static <T extends Item & IMultipleResult> Codec<Data<T>> createCodec() {
            return Data.<T>createMapCodec().codec();
        }

        public static <T extends Item & IMultipleResult> MapCodec<Data<T>> createMapCodec() {
            return RecordCodecBuilder.mapCodec(ins -> ins.group(
                Ingredient.CODEC.fieldOf("template").forGetter(Data::template),
                Ingredient.CODEC.fieldOf("material").forGetter(Data::material),
                Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(Data::inputs),
                BaseMultipleToOneSmithingRecipe.<T>resultCodec().fieldOf("result").forGetter(Data::result),
                Codec.INT.fieldOf("recipeId").forGetter(Data::recipeId)
            ).apply(ins, Data::new));
        }

        public static <T extends Item & IMultipleResult> StreamCodec<RegistryFriendlyByteBuf, Data<T>> createStreamCodec() {
            return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, Data::template,
                Ingredient.CONTENTS_STREAM_CODEC, Data::material,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), Data::inputs,
                resultStreamCodec(), Data::result,
                ByteBufCodecs.VAR_INT, Data::recipeId,
                Data::new
            );
        }
    }

    public static class Serializer<T extends Item & IMultipleResult, R extends BaseMultipleToOneSmithingRecipe<T>>
        implements RecipeSerializer<R> {
        private final Function<Data<T>, R> factory;

        public Serializer(Function<Data<T>, R> factory) {
            this.factory = factory;
        }

        @Override
        public MapCodec<R> codec() {
            return Data.<T>createMapCodec().xmap(factory, BaseMultipleToOneSmithingRecipe::toData);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return StreamCodec.composite(Data.createStreamCodec(), BaseMultipleToOneSmithingRecipe::toData, factory);
        }
    }

    public static class Builder<T extends Item & IMultipleResult, R extends BaseMultipleToOneSmithingRecipe<T>>
        extends AbstractRecipeBuilder<R> {

        protected final Ingredient template;
        protected Ingredient material;
        protected final List<Ingredient> inputs;
        protected final int inputSize;
        protected final T result;
        protected final int recipeId;
        protected final Function5<Ingredient, Ingredient, List<Ingredient>, T, Integer, R> factory;

        protected Builder(
            Ingredient template, NonNullSupplier<T> resultGetter, int inputSize, int recipeId,
            Function5<Ingredient, Ingredient, List<Ingredient>, T, Integer, R> factory
        ) {
            this(template, resultGetter.get(), inputSize, recipeId, factory);
        }

        protected Builder(
            Ingredient template, T result, int inputSize, int recipeId,
            Function5<Ingredient, Ingredient, List<Ingredient>, T, Integer, R> factory
        ) {
            this.template = template;
            this.inputs = new ArrayList<>(inputSize);
            this.inputSize = inputSize;
            this.result = result;
            this.recipeId = recipeId;
            this.factory = factory;
        }

        @SafeVarargs
        public final Builder<T, R> material(NonNullSupplier<? extends Item>... materialGetters) {
            this.material = Ingredient.of(Collections2.transform(List.of(materialGetters), NonNullSupplier::get).toArray(new Item[0]));
            return this;
        }

        public Builder<T, R> material(int count, NonNullSupplier<? extends Item> materialGetter) {
            for (int i = 0; i < count; i++) {
                this.input(Ingredient.of(materialGetter.get()));
            }
            return this;
        }

        public Builder<T, R> material(ItemStack... materials) {
            this.material = Ingredient.of(materials);
            return this;
        }

        public Builder<T, R> material(int count, ItemStack material) {
            for (int i = 0; i < count; i++) {
                this.input(Ingredient.of(material));
            }
            return this;
        }

        public Builder<T, R> material(Ingredient material) {
            this.material = material;
            return this;
        }

        public final Builder<T, R> input(ItemLike... inputs) {
            for (ItemLike input : inputs) {
                this.input(Ingredient.of(input));
            }
            return this;
        }

        public Builder<T, R> input(int count, ItemLike input) {
            for (int i = 0; i < count; i++) {
                this.input(Ingredient.of(input));
            }
            return this;
        }

        public Builder<T, R> input(ItemStack... inputs) {
            for (ItemStack input : inputs) {
                this.input(Ingredient.of(input));
            }
            return this;
        }

        public Builder<T, R> input(int count, ItemStack input) {
            for (int i = 0; i < count; i++) {
                this.input(Ingredient.of(input));
            }
            return this;
        }

        public Builder<T, R> input(TagKey<Item> inputTag) {
            return this.input(Ingredient.of(inputTag));
        }

        public Builder<T, R> input(Ingredient input) {
            this.inputs.add(input);
            return this;
        }

        @Override
        public R buildRecipe() {
            return this.factory.apply(this.template, this.material, this.inputs, this.result, this.recipeId);
        }

        @Override
        public void validate(ResourceLocation pId) {
            if (this.template.isEmpty()) {
                throw new IllegalArgumentException("The template of multiple to one recipe must not be empty, RecipeId: " + pId);
            }
            if (this.material.isEmpty()) {
                throw new IllegalArgumentException("The material of multiple to one recipe must not be empty, RecipeId: " + pId);
            }
            for (int i = 0; i < this.inputs.size(); i++) {
                Ingredient input = this.inputs.get(i);
                if (input.isEmpty()) {
                    throw new IllegalArgumentException(
                        "The " + i + "th input of multiple to one recipe must not be empty, RecipeId: " + pId
                    );
                }
            }
        }

        @Override
        public String getType() {
            String name = switch (this.inputs.size()) {
                case 2 -> "two";
                case 4 -> "four";
                case 8 -> "eight";
                default -> throw new IllegalArgumentException("Illegal input size! get " + this.inputs.size());
            };
            return name + "_to_one_smithing";
        }

        @Override
        public Item getResult() {
            return this.result;
        }
    }
}
