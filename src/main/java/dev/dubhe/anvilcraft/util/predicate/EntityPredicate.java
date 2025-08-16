package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.MovementPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.SlotsPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record EntityPredicate(List<EntitySubPredicate> subPredicates, boolean isOr, boolean isInverted) {
    public static final Codec<EntitySubPredicate> SUB_CODEC = RecordCodecBuilder.create(
        ins -> ins.group(
                EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntitySubPredicate::entityType),
                DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntitySubPredicate::distanceToPlayer),
                MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntitySubPredicate::movement),
                LocationWrapper.CODEC.forGetter(EntitySubPredicate::location),
                MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntitySubPredicate::effects),
                NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntitySubPredicate::nbt),
                EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntitySubPredicate::flags),
                EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntitySubPredicate::equipment),
                net.minecraft.advancements.critereon.EntitySubPredicate.CODEC.optionalFieldOf("type_specific")
                    .forGetter(EntitySubPredicate::subPredicate),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntitySubPredicate::periodicTick),
                net.minecraft.advancements.critereon.EntityPredicate.CODEC.optionalFieldOf("vehicle")
                    .forGetter(EntitySubPredicate::vehicle),
                net.minecraft.advancements.critereon.EntityPredicate.CODEC.optionalFieldOf("passenger")
                    .forGetter(EntitySubPredicate::passenger),
                net.minecraft.advancements.critereon.EntityPredicate.CODEC.optionalFieldOf("targeted_entity")
                    .forGetter(EntitySubPredicate::targetedEntity),
                Codec.STRING.optionalFieldOf("team").forGetter(EntitySubPredicate::team),
                SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntitySubPredicate::slots),
                Codec.BYTE.fieldOf("flag").forGetter(EntitySubPredicate::getFlag)
            )
            .apply(ins, EntitySubPredicate::new)
    );
    public static final Codec<EntityPredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        SUB_CODEC.listOf().fieldOf("subPredicates").forGetter(EntityPredicate::subPredicates),
        Codec.BOOL.fieldOf("isOr").forGetter(EntityPredicate::isOr),
        Codec.BOOL.fieldOf("isInverted").forGetter(EntityPredicate::isInverted)
    ).apply(ins, EntityPredicate::new));

    public boolean matches(ServerPlayer player, @Nullable Entity entity) {
        return this.matches(player.serverLevel(), player.position(), entity);
    }

    public boolean matches(ServerLevel level, @Nullable Vec3 position, @Nullable Entity entity) {
        for (EntitySubPredicate subPredicate : this.subPredicates) {
            if (subPredicate.matches(level, position, entity) == this.isOr) {
                return this.isOr == !this.isInverted;
            }
        }
        return this.isOr == this.isInverted;
    }

    public record EntitySubPredicate(
        Optional<EntityTypePredicate> entityType,
        Optional<DistancePredicate> distanceToPlayer,
        Optional<MovementPredicate> movement,
        LocationWrapper location,
        Optional<MobEffectsPredicate> effects,
        Optional<NbtPredicate> nbt,
        Optional<EntityFlagsPredicate> flags,
        Optional<EntityEquipmentPredicate> equipment,
        Optional<net.minecraft.advancements.critereon.EntitySubPredicate> subPredicate,
        Optional<Integer> periodicTick,
        Optional<net.minecraft.advancements.critereon.EntityPredicate> vehicle,
        Optional<net.minecraft.advancements.critereon.EntityPredicate> passenger,
        Optional<net.minecraft.advancements.critereon.EntityPredicate> targetedEntity,
        Optional<String> team,
        Optional<SlotsPredicate> slots,
        boolean isOr, boolean isInverted
    ) {
        public EntitySubPredicate(
            Optional<EntityTypePredicate> entityType,
            Optional<DistancePredicate> distanceToPlayer,
            Optional<MovementPredicate> movement,
            LocationWrapper location,
            Optional<MobEffectsPredicate> effects,
            Optional<NbtPredicate> nbt,
            Optional<EntityFlagsPredicate> flags,
            Optional<EntityEquipmentPredicate> equipment,
            Optional<net.minecraft.advancements.critereon.EntitySubPredicate> subPredicate,
            Optional<Integer> periodicTick,
            Optional<net.minecraft.advancements.critereon.EntityPredicate> vehicle,
            Optional<net.minecraft.advancements.critereon.EntityPredicate> passenger,
            Optional<net.minecraft.advancements.critereon.EntityPredicate> targetedEntity,
            Optional<String> team,
            Optional<SlotsPredicate> slots,
            byte flag
        ) {
            this(
                entityType, distanceToPlayer, movement, location, effects, nbt,
                flags, equipment, subPredicate, periodicTick, vehicle, passenger,
                targetedEntity, team, slots, (flag & 0x01) == 0x01, (flag & 0x10) == 0x10
            );
        }

        private byte getFlag() {
            return (byte) ((this.isOr() ? 0x00 : 0x01) + (this.isInverted() ? 0x00 : 0x10));
        }

        public boolean matches(ServerLevel level, @Nullable Vec3 position, @Nullable Entity entity) {
            if (entity == null) {
                return this.isInverted;
            } else if (this.entityType.isPresent() && this.entityType.get().matches(entity.getType()) == this.isOr) {
                return this.isOr == !this.isInverted;
            } else {
                if (position == null) {
                    if (this.distanceToPlayer.isPresent()) {
                        return this.isInverted;
                    }
                } else if (this.distanceToPlayer.isPresent()
                    && this.distanceToPlayer.get()
                    .matches(position.x, position.y, position.z, entity.getX(), entity.getY(), entity.getZ()) == this.isOr
                ) {
                    return this.isOr == !this.isInverted;
                }

                if (this.movement.isPresent()) {
                    Vec3 vec3 = entity.getKnownMovement();
                    Vec3 vec31 = vec3.scale(20.0);
                    if (this.movement.get().matches(vec31.x, vec31.y, vec31.z, entity.fallDistance) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    }
                }

                if (this.location.located.isPresent()
                    && this.location.located.get().matches(level, entity.getX(), entity.getY(), entity.getZ()) == this.isOr
                ) {
                    return this.isOr == !this.isInverted;
                } else {
                    if (this.location.steppingOn.isPresent()) {
                        Vec3 vec32 = Vec3.atCenterOf(entity.getOnPos());
                        if (this.location.steppingOn.get().matches(level, vec32.x(), vec32.y(), vec32.z()) == this.isOr) {
                            return this.isOr == !this.isInverted;
                        }
                    }

                    if (this.location.affectsMovement.isPresent()) {
                        Vec3 vec33 = Vec3.atCenterOf(entity.getBlockPosBelowThatAffectsMyMovement());
                        if (this.location.affectsMovement.get().matches(level, vec33.x(), vec33.y(), vec33.z()) == this.isOr) {
                            return this.isOr == !this.isInverted;
                        }
                    }

                    if (this.effects.isPresent() && this.effects.get().matches(entity) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.flags.isPresent() && !this.flags.get().matches(entity) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.equipment.isPresent() && !this.equipment.get().matches(entity) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.subPredicate.isPresent() && !this.subPredicate.get().matches(entity, level, position) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.vehicle.isPresent() && !this.vehicle.get().matches(level, position, entity.getVehicle()) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.passenger.isPresent()
                        && entity.getPassengers().stream().anyMatch(
                        entity1 -> this.passenger.get().matches(level, position, entity1)) == this.isOr
                    ) {
                        return this.isOr == !this.isInverted;
                    } else if (this.targetedEntity.isPresent()
                        && !this.targetedEntity.get().matches(
                        level, position, entity instanceof Mob ? ((Mob) entity).getTarget() : null) == this.isOr
                    ) {
                        return this.isOr == !this.isInverted;
                    } else if (this.periodicTick.isPresent() && entity.tickCount % this.periodicTick.get() == 0 == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.team.isPresent()) {
                        Team team = entity.getTeam();
                        if ((team != null && this.team.get().equals(team.getName())) == this.isOr) {
                            return this.isOr == !this.isInverted;
                        }
                    } else if (this.slots.isPresent() && this.slots.get().matches(entity) == this.isOr) {
                        return this.isOr == !this.isInverted;
                    } else if (this.nbt.isPresent() && this.nbt.get().matches(entity)) {
                        return this.isOr == !this.isInverted;
                    }
                }
            }
            return this.isOr == this.isInverted;
        }

        public static class Builder {
            private final EntityPredicate.Builder parent;
            private Optional<EntityTypePredicate> entityType = Optional.empty();
            private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
            private Optional<MovementPredicate> movement = Optional.empty();
            private Optional<LocationPredicate> located = Optional.empty();
            private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
            private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
            private Optional<MobEffectsPredicate> effects = Optional.empty();
            private Optional<NbtPredicate> nbt = Optional.empty();
            private Optional<EntityFlagsPredicate> flags = Optional.empty();
            private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
            private Optional<net.minecraft.advancements.critereon.EntitySubPredicate> subPredicate = Optional.empty();
            private Optional<Integer> periodicTick = Optional.empty();
            private Optional<net.minecraft.advancements.critereon.EntityPredicate> vehicle = Optional.empty();
            private Optional<net.minecraft.advancements.critereon.EntityPredicate> passenger = Optional.empty();
            private Optional<net.minecraft.advancements.critereon.EntityPredicate> targetedEntity = Optional.empty();
            private Optional<String> team = Optional.empty();
            private Optional<SlotsPredicate> slots = Optional.empty();
            private boolean isOr = false;
            private boolean isInverted = false;

            private Builder(EntityPredicate.Builder parent) {
                this.parent = parent;
            }

            private static Builder builder(EntityPredicate.Builder parent) {
                return new Builder(parent);
            }

            public Builder of(EntityType<?> entityType) {
                this.entityType = Optional.of(EntityTypePredicate.of(entityType));
                return this;
            }

            public Builder of(TagKey<EntityType<?>> entityTypeTag) {
                this.entityType = Optional.of(EntityTypePredicate.of(entityTypeTag));
                return this;
            }

            public Builder entityType(EntityTypePredicate entityType) {
                this.entityType = Optional.of(entityType);
                return this;
            }

            public Builder distance(DistancePredicate distanceToPlayer) {
                this.distanceToPlayer = Optional.of(distanceToPlayer);
                return this;
            }

            public Builder moving(MovementPredicate movement) {
                this.movement = Optional.of(movement);
                return this;
            }

            public Builder located(LocationPredicate.Builder location) {
                this.located = Optional.of(location.build());
                return this;
            }

            public Builder steppingOn(LocationPredicate.Builder steppingOnLocation) {
                this.steppingOnLocation = Optional.of(steppingOnLocation.build());
                return this;
            }

            public Builder movementAffectedBy(LocationPredicate.Builder movementAffectedBy) {
                this.movementAffectedBy = Optional.of(movementAffectedBy.build());
                return this;
            }

            public Builder effects(MobEffectsPredicate.Builder effects) {
                this.effects = effects.build();
                return this;
            }

            public Builder nbt(NbtPredicate nbt) {
                this.nbt = Optional.of(nbt);
                return this;
            }

            public Builder flags(EntityFlagsPredicate.Builder flags) {
                this.flags = Optional.of(flags.build());
                return this;
            }

            public Builder equipment(EntityEquipmentPredicate.Builder equipment) {
                this.equipment = Optional.of(equipment.build());
                return this;
            }

            public Builder equipment(EntityEquipmentPredicate equipment) {
                this.equipment = Optional.of(equipment);
                return this;
            }

            public Builder subPredicate(net.minecraft.advancements.critereon.EntitySubPredicate subPredicate) {
                this.subPredicate = Optional.of(subPredicate);
                return this;
            }

            public Builder periodicTick(int periodicTick) {
                this.periodicTick = Optional.of(periodicTick);
                return this;
            }

            public Builder vehicle(net.minecraft.advancements.critereon.EntityPredicate.Builder vehicle) {
                this.vehicle = Optional.of(vehicle.build());
                return this;
            }

            public Builder passenger(net.minecraft.advancements.critereon.EntityPredicate.Builder passenger) {
                this.passenger = Optional.of(passenger.build());
                return this;
            }

            public Builder targetedEntity(net.minecraft.advancements.critereon.EntityPredicate.Builder targetedEntity) {
                this.targetedEntity = Optional.of(targetedEntity.build());
                return this;
            }

            public Builder team(String team) {
                this.team = Optional.of(team);
                return this;
            }

            public Builder slots(SlotsPredicate slots) {
                this.slots = Optional.of(slots);
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

            public EntityPredicate.Builder build() {
                return this.parent.sub(new EntitySubPredicate(
                    this.entityType,
                    this.distanceToPlayer,
                    this.movement,
                    new LocationWrapper(this.located, this.steppingOnLocation, this.movementAffectedBy),
                    this.effects,
                    this.nbt,
                    this.flags,
                    this.equipment,
                    this.subPredicate,
                    this.periodicTick,
                    this.vehicle,
                    this.passenger,
                    this.targetedEntity,
                    this.team,
                    this.slots,
                    this.isOr,
                    this.isInverted
                ));
            }
        }
    }

    public static LootContext createContext(ServerPlayer player, Entity entity) {
        LootParams lootparams = new LootParams.Builder(player.serverLevel())
            .withParameter(LootContextParams.THIS_ENTITY, entity)
            .withParameter(LootContextParams.ORIGIN, player.position())
            .create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootparams).create(Optional.empty());
    }

    public static class Builder {
        private final ImmutableList.Builder<EntitySubPredicate> subPredicates = ImmutableList.builder();
        private boolean isOr = false;
        private boolean isInverted = false;

        public static EntitySubPredicate.Builder builder() {
            return new Builder().sub();
        }

        public EntitySubPredicate.Builder sub() {
            return EntitySubPredicate.Builder.builder(this);
        }

        private Builder sub(EntitySubPredicate subPredicate) {
            this.subPredicates.add(subPredicate);
            return this;
        }

        public Builder append(Builder another) {
            this.subPredicates.addAll(another.subPredicates.build());
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

        public EntityPredicate build() {
            return new EntityPredicate(this.subPredicates.build(), this.isOr, this.isInverted);
        }
    }

    public record LocationWrapper(
        Optional<LocationPredicate> located, Optional<LocationPredicate> steppingOn, Optional<LocationPredicate> affectsMovement
    ) {
        public static final MapCodec<LocationWrapper> CODEC = RecordCodecBuilder.mapCodec(
            ins -> ins.group(
                    LocationPredicate.CODEC.optionalFieldOf("location").forGetter(LocationWrapper::located),
                    LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(LocationWrapper::steppingOn),
                    LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(LocationWrapper::affectsMovement)
                )
                .apply(ins, LocationWrapper::new)
        );
    }
}
