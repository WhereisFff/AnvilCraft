package dev.dubhe.anvilcraft.entity;

import dev.dubhe.anvilcraft.init.entity.ModEntities;
import dev.dubhe.anvilcraft.util.GravityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;

public class LevitatingBlockEntity extends FallingBlockEntity {
    public LevitatingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
        // 1. 移除 setNoGravity(true)，让物理系统接管 (虽然 FallingBlock 不走标准 tick，但保持状态一致是好的)
        this.setNoGravity(false);
        this.refreshDimensions();
    }

    private LevitatingBlockEntity(Level level, double x, double y, double z, BlockState state) {
        this(ModEntities.LEVITATING_BLOCK.get(), level);
        this.blockState = state;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static LevitatingBlockEntity levitate(Level level, BlockPos pos, BlockState blockState) {
        LevitatingBlockEntity levitating = new LevitatingBlockEntity(
            level,
            (double) pos.getX() + 0.5,
            pos.getY(),
            (double) pos.getZ() + 0.5,
            blockState.hasProperty(BlockStateProperties.WATERLOGGED)
            ? blockState.setValue(BlockStateProperties.WATERLOGGED, false)
            : blockState
        );
        level.setBlock(pos, blockState.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(levitating);
        return levitating;
    }

    @Override
    public void tick() {

        if (this.blockState.isAir()) {
            this.discard();
        } else {
            Block block = this.blockState.getBlock();
            ++this.time;
            Vec3 externalGravity = GravityManager.getGravityVector(this);

            Vec3 naturalBuoyancy = new Vec3(0, 0.04, 0);

            Vec3 finalAcceleration = naturalBuoyancy.add(externalGravity);

            this.setDeltaMovement(this.getDeltaMovement().add(finalAcceleration));

            this.move(MoverType.SELF, this.getDeltaMovement());

            BlockPos blockPos = this.blockPosition();

            boolean landed = this.onGround() || (this.verticalCollision && this.getDeltaMovement().y > 0);

            if (blockPos.getY() <= this.level().getMinBuildHeight() || blockPos.getY() > this.level().getMaxBuildHeight() + 64) {
                this.discard();
            } else {
                if (!landed && this.checkCanMove(this.level(), blockPos)) {
                } else {
                    if (!this.level().isClientSide) {
                        BlockState blockState = this.level().getBlockState(blockPos);

                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));

                        if (!blockState.is(Blocks.MOVING_PISTON)) {
                            if (!this.cancelDrop) {

                                Direction placeDirection = this.getDeltaMovement().y > 0 ? Direction.DOWN : Direction.UP;

                                boolean canBeReplaced = blockState.canBeReplaced(new DirectionalPlaceContext(
                                    this.level(),
                                    blockPos,
                                    placeDirection,
                                    ItemStack.EMPTY,
                                    placeDirection.getOpposite()
                                ));
                                boolean canSurvive = this.blockState.canSurvive(this.level(), blockPos);

                                if (canBeReplaced && canSurvive) {
                                    if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level()
                                                                                                             .getFluidState(blockPos)
                                                                                                             .getType() == Fluids.WATER) {
                                        this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                                    }

                                    if (this.level().setBlock(blockPos, this.blockState, 3)) {
                                        ((ServerLevel) this.level()).getChunkSource().chunkMap.broadcast(
                                            this,
                                            new ClientboundBlockUpdatePacket(blockPos, this.level().getBlockState(blockPos))
                                        );
                                        this.discard();
                                        if (block instanceof Fallable) {
                                            ((Fallable) block).onLand(this.level(), blockPos, this.blockState, blockState, this);
                                        }
                                    } else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                        this.discard();
                                        this.callOnBrokenAfterFall(block, blockPos);
                                        this.spawnAtLocation(block);
                                    }
                                } else {
                                    this.discard();
                                    if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                        this.callOnBrokenAfterFall(block, blockPos);
                                        this.spawnAtLocation(block);
                                    }
                                }
                            } else {
                                this.discard();
                                this.callOnBrokenAfterFall(block, blockPos);
                            }
                        }
                    }
                }
            }

            // 5. 空气阻力 (原版是 0.98)
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
    }

    private boolean checkCanMove(Level level, BlockPos pos) {
        // 这里的逻辑主要是为了防止方块实体在生成时如果卡在方块里，给它一点时间移出去，或者判定是否有路可走
        // 针对漂浮粉，我们主要关心上方

        if (this.time > 1 && this.getDeltaMovement().equals(Vec3.ZERO)) return false;

        // 简化的碰撞检查：
        // 如果我们是向上飞的，检查上面
        // 如果是向下掉的，检查下面 (原版逻辑)
        // 这里保留原始逻辑作为一个宽泛的“是否被完全堵死”的检查

        BlockPos checkPos = this.getDeltaMovement().y > 0 ? pos.above() : pos.below();

        if (level.getBlockState(checkPos).isAir()) return true;
        if (level.getBlockState(checkPos).liquid()) return true;
        if (level.getBlockState(checkPos).getCollisionShape(this.level(), checkPos).equals(Shapes.empty())) return true;
        if (level.getBlockState(pos).getBlock() instanceof Fallable) return true; // 自己占的位置

        // 检查路径上是否有其他下落方块
        return !this.level().getEntitiesOfClass(
            FallingBlockEntity.class,
            new AABB(this.position(), this.position()).inflate(0.2, 0.2, 0.2), // 稍微扩大一点检测范围
            entity -> !entity.equals(this) && entity.getBlockState().getBlock() instanceof Fallable
        ).isEmpty();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(0.98f, 0.98f);
    }
}