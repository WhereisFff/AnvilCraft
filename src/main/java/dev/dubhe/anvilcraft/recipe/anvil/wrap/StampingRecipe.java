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
public class StampingRecipe extends AbstractProcessRecipe<StampingRecipe> {
    public StampingRecipe(
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
                .of(ModBlocks.STAMPING_PLATFORM.get())
                .build()
        );
    }

    @Override
    public @NotNull RecipeSerializer<StampingRecipe> getSerializer() {
        return ModRecipeTypes.STAMPING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<StampingRecipe> getType() {
        return ModRecipeTypes.STAMPING_TYPE.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Serializer extends AbstractSerializer<StampingRecipe> {
        @Override
        protected StampingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new StampingRecipe(itemIngredients, results);
        }
    }

    public static class Builder extends SimpleAbstractBuilder<StampingRecipe, Builder> {
        @Override
        public @NotNull String getType() {
            return "stamping";
        }

        @Override
        protected StampingRecipe of(List<ItemIngredientPredicate> itemIngredients, List<ChanceItemStack> results) {
            return new StampingRecipe(itemIngredients, results);
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
