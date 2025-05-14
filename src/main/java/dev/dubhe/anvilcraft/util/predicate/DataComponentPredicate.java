package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class DataComponentPredicate implements Predicate<DataComponentMap> {
    public static final Codec<DataComponentPredicate> CODEC = DataComponentType.VALUE_MAP_CODEC
        .xmap(
            p_330430_ -> new DataComponentPredicate(
                p_330430_.entrySet().stream().map(entry -> TypedDataComponent.createUnchecked(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList())),
            p_337454_ -> p_337454_.expectedComponents
                .stream()
                .filter(p_337453_ -> !p_337453_.type().isTransient())
                .collect(Collectors.toMap(TypedDataComponent::type, TypedDataComponent::value))
        );
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPredicate> STREAM_CODEC = TypedDataComponent.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .map(DataComponentPredicate::new, p_331347_ -> p_331347_.expectedComponents);
    public static final DataComponentPredicate EMPTY = new DataComponentPredicate(List.of());
    private final List<TypedDataComponent<?>> expectedComponents;

    DataComponentPredicate(List<TypedDataComponent<?>> expectedComponents) {
        this.expectedComponents = expectedComponents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DataComponentPredicate allOf(DataComponentMap expectedComponents) {
        return new DataComponentPredicate(ImmutableList.copyOf(expectedComponents));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DataComponentPredicate datacomponentpredicate
            && this.expectedComponents.equals(datacomponentpredicate.expectedComponents)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.expectedComponents.hashCode();
    }

    @Override
    public String toString() {
        return this.expectedComponents.toString();
    }

    public boolean test(DataComponentMap components) {
        for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
            Object object = components.get(typeddatacomponent.type());
            if (!Objects.equals(typeddatacomponent.value(), object)) {
                return false;
            }
        }

        return true;
    }

    public boolean test(DataComponentHolder components) {
        return this.test(components.getComponents());
    }

    public boolean alwaysMatches() {
        return this.expectedComponents.isEmpty();
    }

    public DataComponentPatch asPatch() {
        DataComponentPatch.Builder datacomponentpatch$builder = DataComponentPatch.builder();

        for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
            datacomponentpatch$builder.set(typeddatacomponent);
        }

        return datacomponentpatch$builder.build();
    }

    public static class Builder {
        private final List<TypedDataComponent<?>> expectedComponents = new ArrayList<>();

        Builder() {
        }

        public Builder append(Builder another) {
            this.expectedComponents.addAll(another.expectedComponents);
            return this;
        }

        public <T> Builder expect(DataComponentType<? super T> component, T value) {
            for (TypedDataComponent<?> typeddatacomponent : this.expectedComponents) {
                if (typeddatacomponent.type() == component) {
                    throw new IllegalArgumentException("Predicate already has component of type: '" + component + "'");
                }
            }

            this.expectedComponents.add(new TypedDataComponent<>(component, value));
            return this;
        }

        public DataComponentPredicate build() {
            return new DataComponentPredicate(List.copyOf(this.expectedComponents));
        }
    }
}
