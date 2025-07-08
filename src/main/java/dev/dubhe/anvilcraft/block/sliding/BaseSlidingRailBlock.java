package dev.dubhe.anvilcraft.block.sliding;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseSlidingRailBlock extends Block implements ISlidingRail {
    public BaseSlidingRailBlock(Properties properties) {
        super(properties.friction(1.0204082f));
    }

    protected BaseSlidingRailBlock(Properties properties, boolean shouldSetFriction) {
        super(shouldSetFriction ? properties.friction(1.0204082f) : properties);
    }

    @Override
    public Block self() {
        return this;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        ISlidingRail.whenNeighborChanged(level, self(), pos, fromPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ISlidingRail.whenTick(level, self(), pos);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        return ISlidingRail.whenTriggerEvent(level, pos, param);
    }
}
