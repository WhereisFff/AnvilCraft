package dev.dubhe.anvilcraft.entity;

import dev.dubhe.anvilcraft.api.sliding.SlidingBlockSection;
import dev.dubhe.anvilcraft.block.sliding.ISlidingRail;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModEntities;
import dev.dubhe.anvilcraft.network.SlidingEntitySyncPacket;
import dev.dubhe.anvilcraft.util.Util;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SlidingBlockEntity extends Entity {
    @Getter
    @Setter
    private SlidingBlockSection section;
    @Getter
    @Setter
    private Direction moveDirection;
    public static final double DEFAULT_MOVEMENT = 0.75;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(
        SlidingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public SlidingBlockEntity(EntityType<? extends SlidingBlockEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.refreshDimensions();
        this.section = SlidingBlockSection.EMPTY;
    }

    public SlidingBlockEntity(
        Level level, Vec3 pos, Direction moveDirection,
        Iterable<Triple<BlockPos, BlockState, Optional<CompoundTag>>> infos
    ) {
        this(ModEntities.SLIDING_BLOCK.get(), level);
        this.blocksBuilding = true;
        this.setPos(pos);
        this.section = SlidingBlockSection.create(this.blockPosition(), infos);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = pos.x;
        this.yo = pos.y;
        this.zo = pos.z;
        this.setStartPos(this.blockPosition());
        this.moveDirection = moveDirection;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static SlidingBlockEntity slid(
        Level level, BlockPos pos, Direction moveDirection,
        Iterable<Triple<BlockPos, BlockState, Optional<CompoundTag>>> infos
    ) {
        SlidingBlockEntity entity = new SlidingBlockEntity(level, pos.getBottomCenter(), moveDirection, infos);
        for (var entry : infos) {
            level.setBlock(pos, level.getFluidState(entry.getLeft()).createLegacyBlock(), 3);
        }
        level.addFreshEntity(entity);
        Util.castSafely(level, ServerLevel.class)
            .ifPresent(it -> PacketDistributor.sendToPlayersTrackingChunk(
                it, new ChunkPos(pos),
                new SlidingEntitySyncPacket(entity.getId(), entity.section.blocks(), entity.moveDirection)
            ));
        return entity;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        if (this.section.isEmpty()) {
            this.discard();
        } else {
            BlockPos pos = this.blockPosition();
            BlockPos belowPos = pos.below();
            BlockState belowState = this.level().getBlockState(belowPos);
            if (belowState.getBlock() instanceof ISlidingRail slidingRail) {
                slidingRail.onSlidingAbove(this.level(), belowState, this);
            }

            if (this.level().isOutsideBuildHeight(pos)) {
                this.section.setBlock(this.level(), this.blockPosition(), this);
            } else if (this.checkCanMove()) {
                this.setDeltaMovement(Vec3.ZERO.relative(this.moveDirection, DEFAULT_MOVEMENT));
            } else if (!this.level().isClientSide) {
                BlockState state = this.level().getBlockState(pos);
                this.setDeltaMovement(Vec3.ZERO.multiply(Vec3.ZERO.relative(this.moveDirection, -0.5)));
                if (!state.is(Blocks.MOVING_PISTON)) {
                    this.section.setBlock(this.level(), this.blockPosition(), this);
                }
                this.discard();
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    @Override
    public void move(MoverType type, Vec3 motion) {
        super.move(type, motion);
        if (motion.x == 0 && motion.y == 0 && motion.z == 0) return;
        List<Entity> list = this.level().getEntities(
            this,
            this.section.getBoundsOnSide(Direction.UP),
            EntitySelector.pushableBy(this)
        );
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (entity instanceof SlidingBlockEntity) continue;
                Vec3 collide = this.section.findCollide(this.position(), entity.getBoundingBox());
                entity.setDeltaMovement(
                    entity.getDeltaMovement().x + collide.x() + 5.5,
                    entity.getDeltaMovement().y < 0 ? 0 : entity.getDeltaMovement().y,
                    entity.getDeltaMovement().z + collide.z() + 5.5
                );
            }
        }
    }

    protected boolean checkCanMove() {
        if (!this.level().getBlockState(this.blockPosition().below()).is(ModBlockTags.SLIDING_RAILS)) return false;
        for (Vec3i pos : this.section.getWallsOnSide(this.moveDirection)) {
            BlockPos checking = this.blockPosition().offset(pos);
            if (!this.level().getBlockState(checking).isAir()) return false;
            if (!this.level().getBlockState(checking.relative(this.moveDirection)).isAir()) return false;
        }
        return true;
    }

    public void stop() {
        if (this.level().isClientSide) return;
        this.section.setBlock(this.level(), this.blockPosition(), this);
        this.discard();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos startPos) {
        this.entityData.set(DATA_START_POS, startPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        CompoundTag section = new CompoundTag();
        SlidingBlockSection.CODEC.encode(this.section, NbtOps.INSTANCE, section);
        compound.put("SlidingBlocks", section);
        Direction.CODEC.encode(this.moveDirection, NbtOps.INSTANCE, EndTag.INSTANCE)
            .ifSuccess(tag -> compound.put("MovingDirection", tag));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        CompoundTag section = compound.getCompound("SlidingBlocks");
        SlidingBlockSection.CODEC.decode(NbtOps.INSTANCE, section)
            .ifSuccess(data -> this.section = data.getFirst());
        Direction.CODEC.decode(NbtOps.INSTANCE, Objects.requireNonNull(compound.get("MovingDirection")))
            .ifSuccess(data -> this.moveDirection = data.getFirst());
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        super.fillCrashReportCategory(category);
        category.setDetail("Sliding Blocks", this.section.toString());
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(0.98f, 0.98f);
    }
}
