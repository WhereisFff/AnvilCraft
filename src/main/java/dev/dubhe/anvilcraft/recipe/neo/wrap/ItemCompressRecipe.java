package dev.dubhe.anvilcraft.recipe.neo.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.neo.util.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.neo.util.HasCauldronSimple;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ItemCompressRecipe extends AbstractItemProcessRecipe<ItemCompressRecipe> {

    public ItemCompressRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            new Vec3(0.0, -1.0, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -0.6, 0.0),
            HasCauldronSimple.empty().build()
        );
    }

    @Override
    public @NotNull RecipeSerializer<ItemCompressRecipe> getSerializer() {
        return ModRecipeTypes.ITEM_COMPRESS_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<ItemCompressRecipe> getType() {
        return ModRecipeTypes.ITEM_COMPRESS_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<ItemCompressRecipe> {
        @Override
        protected ItemCompressRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new ItemCompressRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends AbstractBuilder<ItemCompressRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "item_compress";
        }

        @Override
        protected ItemCompressRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new ItemCompressRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
