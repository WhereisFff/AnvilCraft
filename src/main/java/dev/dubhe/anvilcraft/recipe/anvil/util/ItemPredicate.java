package dev.dubhe.anvilcraft.recipe.anvil.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public record ItemPredicate(
    Optional<HolderSet<Item>> items,
    MinMaxBounds.Ints count,
    DataComponentPredicate components,
    Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates
) implements IItemStackPredicate {
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            RegistryCodecs
                .homogeneousList(Registries.ITEM)
                .optionalFieldOf("items")
                .forGetter(ItemPredicate::items),
            MinMaxBounds.Ints.CODEC
                .optionalFieldOf("count", MinMaxBounds.Ints.ANY)
                .forGetter(ItemPredicate::count),
            DataComponentPredicate.CODEC
                .optionalFieldOf("components", DataComponentPredicate.EMPTY)
                .forGetter(ItemPredicate::components),
            ItemSubPredicate.CODEC
                .optionalFieldOf("predicates", Map.of())
                .forGetter(ItemPredicate::subPredicates)
        ).apply(instance, ItemPredicate::new));

    @Override
    public boolean test(ItemStack itemStack) {
        return this.testIgnoreCount(itemStack) && this.testCount(itemStack.getCount());
    }

    @Override
    public boolean testCount(int count) {
        return this.count.matches(count);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<HolderSet<Item>> items = Optional.empty();
        private MinMaxBounds.Ints count;
        private DataComponentPredicate components;
        private final ImmutableMap.Builder<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates;

        private Builder() {
            this.count = MinMaxBounds.Ints.ANY;
            this.components = DataComponentPredicate.EMPTY;
            this.subPredicates = ImmutableMap.builder();
        }

        public static @NotNull Builder item() {
            return new Builder();
        }

        public Builder of(ItemLike... items) {
            //noinspection deprecation
            this.items = Optional.of(HolderSet.direct((item) -> item.asItem().builtInRegistryHolder(), items));
            return this;
        }

        public Builder of(TagKey<Item> tag) {
            this.items = Optional.of(BuiltInRegistries.ITEM.getOrCreateTag(tag));
            return this;
        }

        public Builder withCount(MinMaxBounds.Ints count) {
            this.count = count;
            return this;
        }

        public <T extends ItemSubPredicate> Builder withSubPredicate(ItemSubPredicate.Type<T> type, T predicate) {
            this.subPredicates.put(type, predicate);
            return this;
        }

        public Builder hasComponents(DataComponentPredicate components) {
            this.components = components;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.items, this.count, this.components, this.subPredicates.build());
        }
    }
}
