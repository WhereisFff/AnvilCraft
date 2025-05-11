package dev.dubhe.anvilcraft.recipe.neo.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemSubPredicate;
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
import java.util.function.Predicate;

public record ItemIngredientPredicate(
    Optional<HolderSet<Item>> items,
    int count,
    DataComponentPredicate components,
    Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates
) implements Predicate<ItemStack> {
    public static final Codec<ItemIngredientPredicate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            RegistryCodecs
                .homogeneousList(Registries.ITEM)
                .optionalFieldOf("items")
                .forGetter(ItemIngredientPredicate::items),
            Codec.INT
                .fieldOf("count")
                .forGetter(ItemIngredientPredicate::count),
            DataComponentPredicate.CODEC
                .optionalFieldOf("components", DataComponentPredicate.EMPTY)
                .forGetter(ItemIngredientPredicate::components),
            ItemSubPredicate.CODEC
                .optionalFieldOf("predicates", Map.of())
                .forGetter(ItemIngredientPredicate::subPredicates)
        ).apply(instance, ItemIngredientPredicate::new));

    @Override
    public boolean test(ItemStack itemStack) {
        if (this.items.isPresent() && !itemStack.is(this.items.get())) {
            return false;
        } else if (this.count > itemStack.getCount()) {
            return false;
        } else if (!this.components.test(itemStack)) {
            return false;
        } else {
            for (ItemSubPredicate itemsubpredicate : this.subPredicates.values()) {
                if (!itemsubpredicate.matches(itemStack)) {
                    return false;
                }
            }
            return true;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<HolderSet<Item>> items = Optional.empty();
        private int count;
        private DataComponentPredicate components;
        private final ImmutableMap.Builder<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates;

        private Builder() {
            this.count = 1;
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

        public Builder withCount(int count) {
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

        public ItemIngredientPredicate build() {
            return new ItemIngredientPredicate(this.items, this.count, this.components, this.subPredicates.build());
        }
    }
}
