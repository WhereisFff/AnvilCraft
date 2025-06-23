package dev.dubhe.anvilcraft.recipe.neo.wrap;

import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ItemCrushRecipe extends AbstractItemProcessRecipe {
    public ItemCrushRecipe(Vec3 inputOffset, List<ItemIngredientPredicate> ingredients, Vec3 outputOffset, List<ItemStack> results) {
        super(inputOffset, ingredients, outputOffset, results);
    }
}
