package dev.dubhe.anvilcraft.recipe.anvil.wrap;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.ChanceItemStack;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class ItemCrushRecipe extends AbstractProcessRecipe<ItemCrushRecipe> {

    public ItemCrushRecipe(
        List<ItemIngredientPredicate> itemIngredients,
        List<ChanceItemStack> results
    ) {
        super(
            new Vec3(0.0, -0.0625, 0.0),
            itemIngredients,
            new Vec3(0.0, -1.5, 0.0),
            results,
            new Vec3(0.0, -0.6, 0.0),
            BlockStatePredicate.builder()
                .of(ModBlocks.CRUSHING_TABLE.get())
                .build()
        );
    }

    @Override
    public @NotNull RecipeSerializer<ItemCrushRecipe> getSerializer() {
        return ModRecipeTypes.ITEM_CRUSH_SERIALIZERS.get();
    }

    @Override
    public @NotNull RecipeType<ItemCrushRecipe> getType() {
        return ModRecipeTypes.ITEM_CRUSH_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<ItemCrushRecipe> {
        @Override
        protected ItemCrushRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new ItemCrushRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends SimpleAbstractBuilder<ItemCrushRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "item_crush";
        }

        @Override
        protected ItemCrushRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new ItemCrushRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
