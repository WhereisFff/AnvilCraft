package dev.dubhe.anvilcraft.recipe.neo.builder.predicate.item;

import dev.dubhe.anvilcraft.recipe.neo.builder.InWorldRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.predicate.item.HasItemIngredient;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemIngredientPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

public class HasItemIngredientBuilder {
    private final InWorldRecipeBuilder builder;
    private Vec3 offset = Vec3.ZERO;
    private Vec3 range = new Vec3(1.0, 1.0, 1.0);
    private final ItemIngredientPredicate.Builder item = ItemIngredientPredicate.Builder.item();

    public HasItemIngredientBuilder(InWorldRecipeBuilder builder) {
        this.builder = builder;
    }

    public HasItemIngredientBuilder offset(Vec3 offset) {
        this.offset = offset;
        return this;
    }

    public HasItemIngredientBuilder offset(double x, double y, double z) {
        this.offset = new Vec3(x, y, z);
        return this;
    }

    public HasItemIngredientBuilder range(Vec3 range) {
        this.range = range;
        return this;
    }

    public HasItemIngredientBuilder range(double x, double y, double z) {
        this.range = new Vec3(x, y, z);
        return this;
    }

    public HasItemIngredientBuilder range(double range) {
        this.range = new Vec3(range, range, range);
        return this;
    }

    public HasItemIngredientBuilder of(ItemLike... items) {
        this.item.of(items);
        return this;
    }

    public HasItemIngredientBuilder of(TagKey<Item> tag) {
        this.item.of(tag);
        return this;
    }

    public HasItemIngredientBuilder count(int count) {
        this.item.withCount(count);
        return this;
    }

    public <T extends ItemSubPredicate> HasItemIngredientBuilder with(ItemSubPredicate.Type<T> type, T predicate) {
        this.item.withSubPredicate(type, predicate);
        return this;
    }

    public HasItemIngredientBuilder has(DataComponentPredicate components) {
        this.item.hasComponents(components);
        return this;
    }

    public InWorldRecipeBuilder build() {
        this.builder.with(new HasItemIngredient(offset, range, item.build()));
        return this.builder;
    }
}
