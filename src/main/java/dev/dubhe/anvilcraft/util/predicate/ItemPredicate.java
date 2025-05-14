package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public record ItemPredicate(
    List<ItemSubPredicate> subPredicates, boolean isOr, boolean isInverted
) implements Predicate<ItemStack> {
    public static final Codec<ItemPredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        ItemSubPredicate.CODEC.listOf().fieldOf("subPredicates").forGetter(ItemPredicate::subPredicates),
        Codec.BOOL.fieldOf("isOr").forGetter(ItemPredicate::isOr),
        Codec.BOOL.fieldOf("isInverted").forGetter(ItemPredicate::isInverted)
    ).apply(ins, ItemPredicate::new));

    @Override
    public boolean test(ItemStack itemStack) {
        for (ItemSubPredicate subPredicate : this.subPredicates) {
            if (subPredicate.test(itemStack) == this.isOr) {
                return this.isOr == !this.isInverted;
            }
        }
        return this.isOr == this.isInverted;
    }

    public record ItemSubPredicate(
        Optional<HolderSet<Item>> items, MinMaxBounds.Ints count, DataComponentPredicate components,
        Map<net.minecraft.advancements.critereon.ItemSubPredicate.Type<?>,
            net.minecraft.advancements.critereon.ItemSubPredicate> subPredicates,
        boolean isOr, boolean isInverted
    ) implements Predicate<ItemStack> {
        public static final Codec<ItemSubPredicate> CODEC = RecordCodecBuilder.create(
            p_337371_ -> p_337371_.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("items").forGetter(ItemSubPredicate::items),
                    MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(ItemSubPredicate::count),
                    DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY)
                        .forGetter(ItemSubPredicate::components),
                    net.minecraft.advancements.critereon.ItemSubPredicate.CODEC.optionalFieldOf("predicates", Map.of())
                        .forGetter(ItemSubPredicate::subPredicates),
                    Codec.BOOL.fieldOf("isOr").forGetter(ItemSubPredicate::isOr),
                    Codec.BOOL.fieldOf("isInverted").forGetter(ItemSubPredicate::isInverted)
                )
                .apply(p_337371_, ItemSubPredicate::new)
        );

        public boolean test(ItemStack stack) {
            if (this.items.isPresent() && !stack.is(this.items.get())) {
                return false;
            } else if (!this.count.matches(stack.getCount())) {
                return false;
            } else if (!this.components.test(stack)) {
                return false;
            } else {
                for (net.minecraft.advancements.critereon.ItemSubPredicate itemsubpredicate : this.subPredicates.values()) {
                    if (!itemsubpredicate.matches(stack)) {
                        return false;
                    }
                }

                return true;
            }
        }
        public static class Builder {
            private final ItemPredicate.Builder parent;
            private Optional<HolderSet<Item>> items = Optional.empty();
            private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
            private net.minecraft.core.component.DataComponentPredicate components = net.minecraft.core.component.DataComponentPredicate.EMPTY;
            private final ImmutableMap.Builder<
                net.minecraft.advancements.critereon.ItemSubPredicate.Type<?>,
                net.minecraft.advancements.critereon.ItemSubPredicate> subPredicates = ImmutableMap.builder();
            private boolean isOr = false;
            private boolean isInverted = false;

            private Builder(ItemPredicate.Builder parent) {
                this.parent = parent;
            }

            public static Builder builder(ItemPredicate.Builder parent) {
                return new Builder(parent);
            }

            public Builder of(ItemLike... items) {
                this.items = Optional.of(HolderSet.direct(p_298756_ -> p_298756_.asItem().builtInRegistryHolder(), items));
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

            public <T extends net.minecraft.advancements.critereon.ItemSubPredicate> Builder withSubPredicate(
                net.minecraft.advancements.critereon.ItemSubPredicate.Type<T> type, T value) {
                this.subPredicates.put(type, value);
                return this;
            }

            public Builder hasComponents(DataComponentPredicate components) {
                this.components = components;
                return this;
            }

            public Builder or() {
                this.isOr = true;
                return this;
            }

            public Builder and() {
                this.isOr = false;
                return this;
            }

            public Builder invert() {
                this.isInverted = true;
                return this;
            }

            public Builder notInvert() {
                this.isInverted = false;
                return this;
            }

            public ItemPredicate.Builder build() {
                return this.parent.sub(new ItemSubPredicate(
                    this.items, this.count, this.components, this.subPredicates.build(), this.isOr, this.isInverted
                ));
            }
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<ItemSubPredicate> subPredicates = ImmutableList.builder();
        private boolean isOr = false;
        private boolean isInverted = false;

        private Builder() {
        }

        public static ItemSubPredicate.Builder item() {
            return new Builder().sub();
        }

        public Builder append(Builder another) {
            this.subPredicates.addAll(another.subPredicates.build());
            return this;
        }

        public ItemSubPredicate.Builder sub() {
            return new ItemSubPredicate.Builder(this);
        }

        private Builder sub(ItemSubPredicate subPredicate) {
            this.subPredicates.add(subPredicate);
            return this;
        }

        public Builder or() {
            this.isOr = true;
            return this;
        }

        public Builder and() {
            this.isOr = false;
            return this;
        }

        public Builder invert() {
            this.isInverted = true;
            return this;
        }

        public Builder notInvert() {
            this.isInverted = false;
            return this;
        }

        public ItemPredicate build() {
            return new ItemPredicate(this.subPredicates.build(), this.isOr, this.isInverted);
        }
    }
}
