package dev.dubhe.anvilcraft.recipe.neo.predicate;

import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HasItemIngredient extends HasItem {
    public HasItemIngredient(Vec3 pos, Vec3 range, HolderSet<Item> items, MinMaxBounds.Ints count, DataComponentPredicate components, Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates) {
        super(pos, range, items, count, components, subPredicates);
    }

    @Override
    public boolean test(@NotNull InWorldRecipeContext inWorldRecipeContext) {
        return super.test(inWorldRecipeContext);
    }

    @Override
    public void accept(InWorldRecipeContext inWorldRecipeContext) {
        super.accept(inWorldRecipeContext);
    }

    @Override
    public void push(InWorldRecipeContext inWorldRecipeContext) {
    }

    @Override
    public void pop(InWorldRecipeContext inWorldRecipeContext) {
    }
}
