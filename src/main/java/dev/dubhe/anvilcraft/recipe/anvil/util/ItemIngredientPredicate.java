package dev.dubhe.anvilcraft.recipe.anvil.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.item.HasItemIngredient;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public record ItemIngredientPredicate(
    Optional<HolderSet<Item>> items,
    int count,
    DataComponentPredicate components,
    Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates
) implements IItemStackPredicate {
    public static final Codec<ItemIngredientPredicate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            RegistryCodecs
                .homogeneousList(Registries.ITEM)
                .optionalFieldOf("items")
                .forGetter(ItemIngredientPredicate::items),
            Codec.INT
                .optionalFieldOf("count", 1)
                .forGetter(ItemIngredientPredicate::count),
            DataComponentPredicate.CODEC
                .optionalFieldOf("components", DataComponentPredicate.EMPTY)
                .forGetter(ItemIngredientPredicate::components),
            ItemSubPredicate.CODEC
                .optionalFieldOf("predicates", Map.of())
                .forGetter(ItemIngredientPredicate::subPredicates)
        ).apply(instance, ItemIngredientPredicate::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemIngredientPredicate> STREAM_CODEC = StreamCodec.of(
        (buffer, value) -> {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.ITEM.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> encode = ItemIngredientPredicate.CODEC.encode(value, ops, ops.empty());
            Tag tag = encode.getOrThrow();
            buffer.writeNbt(tag);
        },
        buffer -> {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.ITEM.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            return ItemIngredientPredicate.CODEC.decode(ops, buffer.readNbt()).getOrThrow().getFirst();
        }
    );

    public static Builder of(ItemLike... items) {
        return new Builder().of(items);
    }

    public static Builder of(TagKey<Item> tag) {
        return new Builder().of(tag);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return this.testIgnoreCount(itemStack) && this.testCount(itemStack.getCount());
    }

    @Override
    public boolean testCount(int count) {
        return this.count <= count;
    }

    public @NotNull HasItemIngredient toHasItemIngredient(Vec3 offset, Vec3 range) {
        return new HasItemIngredient(offset, range, this);
    }

    private static final Int2ObjectMap<ItemStack[]> INGREDIENT_CACHE = new Int2ObjectArrayMap<>();

    public ItemStack[] getItems() {
        int hash = this.hashCode();
        if (!INGREDIENT_CACHE.containsKey(hash)) {
            //noinspection deprecation
            INGREDIENT_CACHE.put(
                hash, this.items()
                    .map(itemSet -> itemSet.stream()
                        .map(itemHolder -> new ItemStack(itemHolder, this.count(), this.components().asPatch()))
                        .toArray(ItemStack[]::new))
                    .orElse(
                        new ItemStack[]{new ItemStack(Items.BARRIER.builtInRegistryHolder(), this.count(), this.components().asPatch())})
            );
        }
        return INGREDIENT_CACHE.get(hash);
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

        public <D> Builder of(@NotNull ItemStack stack) {
            Item item = stack.getItem();
            ItemStack defaultInstance = item.getDefaultInstance();
            this.of(item);
            for (TypedDataComponent<?> component : item.components()) {
                Object o = defaultInstance.get(component.type());
                if (o != null && o.equals(component.value())) continue;
                //noinspection unchecked
                this.hasComponents(
                    DataComponentPredicate.builder()
                        .expect((DataComponentType<D>) component.type(), (D) component.value())
                        .build()
                );
            }
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
