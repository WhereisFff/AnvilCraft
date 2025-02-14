package dev.dubhe.anvilcraft.recipe.neo.predicate;

import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.RecipePredicateType;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HasItem implements RecipePredicate<HasItem> {
    protected final Vec3 pos;
    protected final Vec3 range;
    protected final HolderSet<Item> items;
    protected final MinMaxBounds.Ints count;
    protected final DataComponentPredicate components;
    protected final Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates;

    public HasItem(Vec3 pos, Vec3 range, HolderSet<Item> items, MinMaxBounds.Ints count, DataComponentPredicate components, Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates) {
        this.pos = pos;
        this.range = range;
        this.items = items;
        this.count = count;
        this.components = components;
        this.subPredicates = subPredicates;
    }

    @Override
    public @NotNull RecipePredicateType<HasItem> getType() {
        return null;
    }

    @Override
    public boolean test(@NotNull InWorldRecipeContext inWorldRecipeContext) {
        int count = 0;
        List<ItemStack> itemStacks = this.getItemStacks(inWorldRecipeContext);
        for (ItemStack stack : itemStacks) {
            if (this.test(stack)) {
                count += stack.getCount();
            }
        }
        return this.count.matches(count);
    }

    public boolean test(@NotNull ItemStack stack) {
        if (!stack.is(this.items)) {
            return false;
        } else if (!this.components.test(stack)) {
            return false;
        } else {
            for (ItemSubPredicate itemsubpredicate : this.subPredicates.values()) {
                if (!itemsubpredicate.matches(stack)) {
                    return false;
                }
            }
            return true;
        }
    }

    public @NotNull List<ItemStack> getItemStacks(@NotNull InWorldRecipeContext inWorldRecipeContext) {
        ServerLevel level = inWorldRecipeContext.getLevel();
        AABB aabb = AABB.ofSize(pos, range.x, range.y, range.z);
        List<? extends ItemEntity> entities = level.getEntities(EntityTypeTest.forClass(ItemEntity.class), itemEntity -> aabb.contains(itemEntity.position()));
        List<ItemStack> itemStacks = new ArrayList<>();
        for (ItemEntity entity : entities) itemStacks.add(entity.getItem());
        return itemStacks;
    }
}
