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
        this.setNoGravity(true);
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
        this.setNoGravity(true);

        if (this.blockState.isAir()) {
            this.discard();
            return;
        }

        Block block = this.blockState.getBlock();
        ++this.time;
        BlockPos blockPos = this.blockPosition();
        int y = blockPos.getY();

        if (y <= this.level().getMinBuildHeight() || y > this.level().getMaxBuildHeight() + 64) {
            this.discard();
            return;
        }

        Vec3 movement = GravityManager.applyGravity(this, this.getDeltaMovement());

        if (this.checkCanMove(this.level(), blockPos)) {
            this.setDeltaMovement(movement.add(0.0, 0.04, 0.0));
        } else if (!this.level().isClientSide) {
            BlockState blockState = this.level().getBlockState(blockPos);
            this.setDeltaMovement(movement.multiply(0.7, -0.5, 0.7));

            if (!blockState.is(Blocks.MOVING_PISTON)) {
                if (this.cancelDrop) {
                    this.discard();
                    this.callOnBrokenAfterFall(block, blockPos);
                } else {
                    handleBlockPlacementOrDrop(block, blockPos, blockState);
                }
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    private void handleBlockPlacementOrDrop(Block block, BlockPos pos, BlockState currentState) {
        boolean canBeReplaced = currentState.canBeReplaced(
            new DirectionalPlaceContext(this.level(), pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
        );
        boolean canSurvive = this.blockState.canSurvive(this.level(), pos);

        if (canBeReplaced && canSurvive) {
            if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                && this.level().getFluidState(pos).getType() == Fluids.WATER) {
                this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (this.level().setBlock(pos, this.blockState, 3)) {
                ((ServerLevel) this.level()).getChunkSource().chunkMap
                    .broadcast(this, new ClientboundBlockUpdatePacket(pos, this.level().getBlockState(pos)));
                this.discard();
                if (block instanceof Fallable fallable) {
                    fallable.onLand(this.level(), pos, this.blockState, currentState, this);
                }
                return;
            }
        }

        this.discard();
        if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.callOnBrokenAfterFall(block, pos);
            this.spawnAtLocation(block);
        }
    }

    private boolean checkCanMove(Level level, BlockPos pos) {
        if (this.time > 1 && this.getDeltaMovement().equals(Vec3.ZERO)) return false;
        if (level.getBlockState(pos.above()).isAir()) return true;
        if (level.getBlockState(pos.above()).liquid()) return true;
        if (level.getBlockState(pos.above()).getCollisionShape(this.level(), pos.above()).equals(Shapes.empty())) return true;
        if (level.getBlockState(pos).getBlock() instanceof Fallable) return true;
        if (level.getBlockState(pos.above()).getBlock() instanceof Fallable) return true;
        return !this.level().getEntitiesOfClass(
            FallingBlockEntity.class,
            new AABB(this.position(), this.position()).expandTowards(-0.2, 0, -0.2).expandTowards(0.2, 1.7, 0.2),
            entity -> !entity.equals(this) && entity.getBlockState().getBlock() instanceof Fallable
        ).isEmpty();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(0.98f, 0.98f);
    }
}
