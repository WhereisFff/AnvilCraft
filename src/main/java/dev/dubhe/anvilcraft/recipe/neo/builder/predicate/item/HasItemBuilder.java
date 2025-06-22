package dev.dubhe.anvilcraft.recipe.neo.builder.predicate.item;

import dev.dubhe.anvilcraft.recipe.neo.builder.InWorldRecipeBuilder;
import dev.dubhe.anvilcraft.recipe.neo.predicate.item.HasItem;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

public class HasItemBuilder {
    private final InWorldRecipeBuilder builder;
    private Vec3 offset = Vec3.ZERO;
    private Vec3 range = new Vec3(1.0, 1.0, 1.0);
    private final ItemPredicate.Builder item = ItemPredicate.Builder.item();

    public HasItemBuilder(InWorldRecipeBuilder builder) {
        this.builder = builder;
    }

    public HasItemBuilder offset(Vec3 offset) {
        this.offset = offset;
        return this;
    }

    public HasItemBuilder offset(double x, double y, double z) {
        this.offset = new Vec3(x, y, z);
        return this;
    }

    public HasItemBuilder range(Vec3 range) {
        this.range = range;
        return this;
    }

    public HasItemBuilder range(double x, double y, double z) {
        this.range = new Vec3(x, y, z);
        return this;
    }

    public HasItemBuilder range(double range) {
        this.range = new Vec3(range, range, range);
        return this;
    }

    public HasItemBuilder of(ItemLike... items) {
        this.item.of(items);
        return this;
    }

    public HasItemBuilder of(TagKey<Item> tag) {
        this.item.of(tag);
        return this;
    }

    public HasItemBuilder count(MinMaxBounds.Ints count) {
        this.item.withCount(count);
        return this;
    }

    public HasItemBuilder moreThan(int min) {
        this.item.withCount(MinMaxBounds.Ints.atLeast(min));
        return this;
    }

    public HasItemBuilder between(int min, int max) {
        this.item.withCount(MinMaxBounds.Ints.between(min, max));
        return this;
    }

    public HasItemBuilder lessThan(int min) {
        this.item.withCount(MinMaxBounds.Ints.atMost(min));
        return this;
    }

    public <T extends ItemSubPredicate> HasItemBuilder with(ItemSubPredicate.Type<T> type, T predicate) {
        this.item.withSubPredicate(type, predicate);
        return this;
    }

    public HasItemBuilder has(DataComponentPredicate components) {
        this.item.hasComponents(components);
        return this;
    }

    public InWorldRecipeBuilder build() {
        this.builder.with(new HasItem(offset, range, item.build()));
        return this.builder;
    }
}
