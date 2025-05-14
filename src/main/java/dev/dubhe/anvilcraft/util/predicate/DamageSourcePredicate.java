package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModItemTags;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record DamageSourcePredicate(List<DamageSourceSubPredicate> subPredicates, boolean isOr, boolean isInverted) {
    public static final Codec<DamageSourcePredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        DamageSourceSubPredicate.CODEC.listOf().fieldOf("subPredicates").forGetter(DamageSourcePredicate::subPredicates),
        Codec.BOOL.fieldOf("isOr").forGetter(DamageSourcePredicate::isOr),
        Codec.BOOL.fieldOf("isInverted").forGetter(DamageSourcePredicate::isInverted)
    ).apply(ins, DamageSourcePredicate::new));

    public boolean matches(ServerPlayer player, DamageSource source) {
        return this.matches(player.serverLevel(), player.position(), source);
    }

    public boolean matches(ServerLevel level, Vec3 position, DamageSource source) {
        for (DamageSourceSubPredicate subPredicate : this.subPredicates) {
            if (subPredicate.matches(level, position, source) == this.isOr) {
                return this.isOr;
            }
        }
        return this.isInverted;
    }

    public record DamageSourceSubPredicate(
        Optional<DamageTypePredicate> typePredicate,
        Optional<EntityPredicate> murderPredicate, Optional<EntityPredicate> victimPredicate,
        Optional<ItemPredicate> weaponPredicate, Optional<Boolean> isDirect, Optional<Boolean> isSameTeam,
        boolean isOr, boolean isInverted
    ) {
        public static final Codec<DamageSourceSubPredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            DamageTypePredicate.CODEC.optionalFieldOf("typePredicate").forGetter(DamageSourceSubPredicate::typePredicate),
            EntityPredicate.CODEC.optionalFieldOf("murderPredicate").forGetter(DamageSourceSubPredicate::murderPredicate),
            EntityPredicate.CODEC.optionalFieldOf("victimPredicate").forGetter(DamageSourceSubPredicate::victimPredicate),
            ItemPredicate.CODEC.optionalFieldOf("weaponPredicate").forGetter(DamageSourceSubPredicate::weaponPredicate),
            Codec.BOOL.optionalFieldOf("isDirect").forGetter(DamageSourceSubPredicate::isDirect),
            Codec.BOOL.optionalFieldOf("isSameTeam").forGetter(DamageSourceSubPredicate::isSameTeam),
            Codec.BOOL.fieldOf("isOr").forGetter(DamageSourceSubPredicate::isOr),
            Codec.BOOL.fieldOf("isInverted").forGetter(DamageSourceSubPredicate::isInverted)
        ).apply(ins, DamageSourceSubPredicate::new));

        public boolean matches(ServerPlayer player, DamageSource source) {
            return this.matches(player.serverLevel(), player.position(), source);
        }

        public boolean matches(ServerLevel level, Vec3 position, DamageSource source) {
            if (this.typePredicate.isPresent() && this.typePredicate.get().test(source.typeHolder()) == this.isOr) {
                return this.isOr == !this.isInverted;
            }

            if (this.isSameTeam.isPresent() && this.murderPredicate.isPresent() && this.victimPredicate.isPresent()
                && source.getEntity() instanceof Player murder && source.getDirectEntity() instanceof Player victim
            ) {
                boolean isSameTeam = Optional.ofNullable(victim.getTeam())
                    .map(team -> team.getPlayers().contains(murder.getScoreboardName()))
                    .orElse(murder.getTeam() == null);
                if (isSameTeam == this.isSameTeam.get() == this.isOr) {
                    return this.isOr == !this.isInverted;
                }
            } else if (this.victimPredicate.isPresent()
                && this.victimPredicate.get().matches(level, position, source.getDirectEntity()) == this.isOr
            ) {
                return this.isOr == !this.isInverted;
            } else if (this.murderPredicate.isPresent()
                       && this.murderPredicate.get().matches(level, position, source.getEntity()) == this.isOr
            ) {
                return this.isOr == !this.isInverted;
            } else if (this.weaponPredicate.isPresent()
                       && source.getWeaponItem() != null
                       && this.weaponPredicate.get().test(source.getWeaponItem()) == this.isOr
            ) {
                return this.isOr == !this.isInverted;
            } else if (this.isDirect.isPresent() && this.isDirect.get() == source.isDirect() == this.isOr) {
                return this.isOr == !this.isInverted;
            }
            return this.isOr == this.isInverted;
        }

        public static class Builder {
            private final DamageSourcePredicate.Builder parent;
            private Optional<DamageTypePredicate.Builder> typePredicate = Optional.empty();
            private Optional<EntityPredicate.Builder> murderPredicate = Optional.empty();
            private Optional<EntityPredicate.Builder> victimPredicate = Optional.empty();
            private Optional<ItemPredicate.Builder> weaponPredicate = Optional.empty();
            private Optional<Boolean> isDirect = Optional.empty();
            private Optional<Boolean> isSameTeam = Optional.empty();
            private boolean isOr = true;
            private boolean isInverted = false;

            private Builder(DamageSourcePredicate.Builder parent) {
                this.parent = parent;
            }

            private static Builder builder(DamageSourcePredicate.Builder parent) {
                return new Builder(parent);
            }

            @SafeVarargs
            public final Builder type(ResourceKey<DamageType>... key) {
                this.typePredicate = this.typePredicate
                    .map(typeTagPredicate -> typeTagPredicate.sub().type(key).build())
                    .or(() -> Optional.of(DamageTypePredicate.Builder.builder().type(key).build()));
                return this;
            }

            public Builder type(TagPredicate<DamageType> tag) {
                this.typePredicate = this.typePredicate
                    .map(typeTagPredicate -> typeTagPredicate.sub().tag(tag).build())
                    .or(() -> Optional.of(DamageTypePredicate.Builder.builder().tag(tag).build()));
                return this;
            }

            @SafeVarargs
            public final Builder type(TagKey<DamageType>... tags) {
                return this.type(TagPredicate.is(true, tags));
            }

            public Builder type(String namespace) {
                this.typePredicate = this.typePredicate
                    .map(typeTagPredicate -> typeTagPredicate.sub().namespace(namespace).build())
                    .or(() -> Optional.of(DamageTypePredicate.Builder.builder().namespace(namespace).build()));
                return this;
            }

            public Builder murder(EntityPredicate.Builder builder) {
                this.murderPredicate = this.murderPredicate
                    .map(builder1 -> builder1.append(builder))
                    .or(() -> Optional.of(builder));
                return this;
            }

            public Builder murder(EntityType<?>... entityTypes) {
                for (EntityType<?> entityType : entityTypes) {
                    this.murder(EntityPredicate.Builder.builder().entityType(EntityTypePredicate.of(entityType)).build());
                }
                return this;
            }

            @SafeVarargs
            public final Builder murder(TagKey<EntityType<?>>... entityTypeTags) {
                for (TagKey<EntityType<?>> entityTypeTag : entityTypeTags) {
                    this.murder(EntityPredicate.Builder.builder().entityType(EntityTypePredicate.of(entityTypeTag)).build());
                }
                return this;
            }

            public Builder victim(EntityPredicate.Builder builder) {
                this.victimPredicate = this.victimPredicate
                    .map(builder1 -> builder1.append(builder))
                    .or(() -> Optional.of(builder));
                return this;
            }

            public Builder victim(EntityType<?>... entityTypes) {
                for (EntityType<?> entityType : entityTypes) {
                    this.victim(EntityPredicate.Builder.builder().entityType(EntityTypePredicate.of(entityType)).build());
                }
                return this;
            }

            public Builder weapon(ItemPredicate.Builder builder) {
                this.weaponPredicate = this.weaponPredicate
                    .map(builder1 -> builder1.append(builder))
                    .or(() -> Optional.of(builder));
                return this;
            }

            public Builder weapon(TagKey<Item> tag) {
                return this.weapon(ItemPredicate.Builder.item().of(tag).build());
            }

            public Builder isDirect(boolean isDirect) {
                this.isDirect = Optional.of(isDirect);
                return this;
            }

            public Builder isSameTeam(boolean isSameTeam) {
                this.isSameTeam = Optional.of(isSameTeam);
                this.murder(EntityType.PLAYER);
                this.victim(EntityType.PLAYER);
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

            public DamageSourcePredicate.Builder build() {
                return this.parent.sub(new DamageSourceSubPredicate(
                    this.typePredicate.map(DamageTypePredicate.Builder::build),
                    this.murderPredicate.map(EntityPredicate.Builder::build),
                    this.victimPredicate.map(EntityPredicate.Builder::build),
                    this.weaponPredicate.map(ItemPredicate.Builder::build),
                    this.isDirect, this.isSameTeam, this.isOr, this.isInverted
                ));
            }

            public Builder buildAndSub() {
                return this.build().sub();
            }
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<DamageSourceSubPredicate> subPredicates = ImmutableList.builder();
        private boolean isOr = true;
        private boolean isInverted = false;

        public static DamageSourceSubPredicate.Builder builder() {
            return new Builder().sub();
        }

        public Builder append(Builder another) {
            this.subPredicates.addAll(another.subPredicates.build());
            return this;
        }

        public DamageSourceSubPredicate.Builder sub() {
            return new DamageSourceSubPredicate.Builder(this);
        }

        private Builder sub(DamageSourceSubPredicate subPredicate) {
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

        public DamageSourcePredicate build() {
            return new DamageSourcePredicate(this.subPredicates.build(), this.isOr, this.isInverted);
        }
    }
}
