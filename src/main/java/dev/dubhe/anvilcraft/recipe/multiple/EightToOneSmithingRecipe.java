package dev.dubhe.anvilcraft.recipe.multiple;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.dubhe.anvilcraft.api.item.IMultipleResult;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EightToOneSmithingRecipe<T extends Item & IMultipleResult>
    extends BaseMultipleToOneSmithingRecipe<T> {

    protected EightToOneSmithingRecipe(Ingredient template, Ingredient material, List<Ingredient> inputs, T result, int recipeId) {
        super(template, material, inputs, result, recipeId);
    }

    public EightToOneSmithingRecipe(Data<T> data) {
        super(data);
    }

    public static <T extends Item & IMultipleResult> Builder<T, EightToOneSmithingRecipe<T>> builder(
        NonNullSupplier<T> resultGetter, int recipeId
    ) {
        return BaseMultipleToOneSmithingRecipe.builder(
            Ingredient.of(ModItems.EIGHT_TO_ONE_SMITHING_TEMPLATE.get()), resultGetter, 8, recipeId, EightToOneSmithingRecipe::new
        );
    }

    public static <T extends Item & IMultipleResult> Builder<T, EightToOneSmithingRecipe<T>> builder(T result, int recipeId) {
        return BaseMultipleToOneSmithingRecipe.builder(
            Ingredient.of(ModItems.EIGHT_TO_ONE_SMITHING_TEMPLATE.get()), result, 8, recipeId, EightToOneSmithingRecipe::new
        );
    }

    @ApiStatus.Internal
    public static <T extends Item & IMultipleResult> Serializer<T, EightToOneSmithingRecipe<T>> createSerializer() {
        return new Serializer<>(EightToOneSmithingRecipe::new);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.EIGHT_TO_ONE_SMITHING_SERIALIZER.get();
    }
}
