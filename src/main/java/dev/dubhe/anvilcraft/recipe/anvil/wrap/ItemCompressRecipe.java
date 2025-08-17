package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ItemCompressRecipe extends AbstractProcessRecipe<ItemCompressRecipe> {

    public ItemCompressRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            new Property()
                .setItemInputOffset(new Vec3(0.0, -1.0, 0.0))
                .setInputItems(itemIngredients)
                .setItemOutputOffset(new Vec3(0.0, -1.0, 0.0))
                .setResultItems(results)
                .setCauldronOffset(new Vec3(0.0, -1.0, 0.0))
                .setHasCauldron(HasCauldronSimple.empty().build())
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

    public static class Builder extends SimpleAbstractBuilder<ItemCompressRecipe, Builder> {
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
