package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record DamageTypePredicate(
    List<DamageTypeSubPredicate> subPredicates, boolean isOr, boolean isInverted
) implements Predicate<Holder<DamageType>> {
    public static final Codec<DamageTypePredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        DamageTypeSubPredicate.CODEC.listOf().fieldOf("subPredicates").forGetter(DamageTypePredicate::subPredicates),
        Codec.BOOL.fieldOf("isOr").forGetter(DamageTypePredicate::isOr),
        Codec.BOOL.fieldOf("isInverted").forGetter(DamageTypePredicate::isInverted)
    ).apply(ins, DamageTypePredicate::new));

    @Override
    public boolean test(Holder<DamageType> typeHolder) {
        for (DamageTypeSubPredicate subPredicate : this.subPredicates) {
            if (subPredicate.test(typeHolder) == this.isOr) {
                return this.isOr == !this.isInverted;
            }
        }
        return this.isOr == this.isInverted;
    }

    public record DamageTypeSubPredicate(
        List<ResourceKey<DamageType>> keys, Optional<TagPredicate<DamageType>> tagPredicate, Optional<String> namespace,
        boolean isOr, boolean isInverted
    ) implements Predicate<Holder<DamageType>> {
        public static final Codec<DamageTypeSubPredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            ResourceKey.codec(Registries.DAMAGE_TYPE).listOf().fieldOf("keys").forGetter(DamageTypeSubPredicate::keys),
            TagPredicate.codec(Registries.DAMAGE_TYPE).optionalFieldOf("tagPredicate").forGetter(DamageTypeSubPredicate::tagPredicate),
            Codec.STRING.optionalFieldOf("namespace").forGetter(DamageTypeSubPredicate::namespace),
            Codec.BOOL.fieldOf("isOr").forGetter(DamageTypeSubPredicate::isOr),
            Codec.BOOL.fieldOf("isInverted").forGetter(DamageTypeSubPredicate::isInverted)
        ).apply(ins, DamageTypeSubPredicate::new));

        @Override
        public boolean test(Holder<DamageType> typeHolder) {
            for (ResourceKey<DamageType> key : this.keys) {
                if (typeHolder.is(key) == this.isOr) {
                    return this.isOr == !this.isInverted;
                }
            }

            if (this.tagPredicate.isPresent() && this.tagPredicate.get().matches(typeHolder) == this.isOr) {
                return this.isOr == !this.isInverted;
            } else if (
                this.namespace.isPresent()
                && typeHolder.unwrapKey().isPresent()
                && this.namespace.get().contains(typeHolder.unwrapKey().get().location().getNamespace()) == this.isOr
            ) {
                return this.isOr == !this.isInverted;
            }
            return this.isOr == this.isInverted;
        }

        public static class Builder {
            private final DamageTypePredicate.Builder parent;
            private final ImmutableList.Builder<ResourceKey<DamageType>> typeKeys = ImmutableList.builder();
            private Optional<TagPredicate<DamageType>> typeTagPredicate = Optional.empty();
            private Optional<String> namespace = Optional.empty();
            private boolean isOr = false;
            private boolean isInverted = false;

            private Builder(DamageTypePredicate.Builder parent) {
                this.parent = parent;
            }

            private static Builder builder(DamageTypePredicate.Builder parent) {
                return new Builder(parent);
            }

            @SafeVarargs
            public final Builder type(ResourceKey<DamageType>... key) {
                this.typeKeys.addAll(Arrays.asList(key));
                return this;
            }

            public Builder tag(TagPredicate<DamageType> tag) {
                this.typeTagPredicate = this.typeTagPredicate
                    .map(typeTagPredicate -> typeTagPredicate.append(tag))
                    .or(() -> Optional.of(tag));
                return this;
            }

            @SafeVarargs
            public final Builder tags(TagKey<DamageType>... tags) {
                return this.tag(TagPredicate.is(true, tags));
            }

            public Builder namespace(String namespace) {
                this.namespace = Optional.of(namespace);
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

            public DamageTypePredicate.Builder build() {
                return this.parent.sub(new DamageTypeSubPredicate(
                    this.typeKeys.build(),
                    this.typeTagPredicate,
                    this.namespace,
                    this.isOr, this.isInverted
                ));
            }

            public Builder buildAndSub() {
                return this.build().sub();
            }
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<DamageTypeSubPredicate> subPredicates = ImmutableList.builder();
        private boolean isOr = true;
        private boolean isInverted = false;

        public static DamageTypeSubPredicate.Builder builder() {
            return new Builder().sub();
        }

        public Builder append(Builder another) {
            this.subPredicates.addAll(another.subPredicates.build());
            return this;
        }

        public DamageTypeSubPredicate.Builder sub() {
            return new DamageTypeSubPredicate.Builder(this);
        }

        private Builder sub(DamageTypeSubPredicate subPredicate) {
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

        public DamageTypePredicate build() {
            return new DamageTypePredicate(this.subPredicates.build(), this.isOr, this.isInverted);
        }
    }
}
