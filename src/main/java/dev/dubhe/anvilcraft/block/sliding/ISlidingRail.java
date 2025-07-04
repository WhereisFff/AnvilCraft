package dev.dubhe.anvilcraft.block.sliding;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.injection.block.IBlockExtension;
import dev.dubhe.anvilcraft.entity.SlidingBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface ISlidingRail extends IBlockExtension {
    Map<BlockPos, PistonPushInfo> MOVING_PISTON_MAP = new HashMap<>();

    /**
     * 当滑动方块经过时每tick调用该方法。
     *
     * @param level 滑轨所处的世界
     * @param state 滑轨方块状态
     * @param entity 滑动方块实体
     */
    void onSlidingAbove(Level level, BlockState state, SlidingBlockEntity entity);

    @Override
    default boolean canStickTo(BlockPos pos, BlockState state, BlockPos otherPos, BlockState other) {
        if (otherPos.equals(pos.above())) return false;
        if (!AnvilCraft.config.slidingRailStickToEachOther) return other.isStickyBlock();
        if (!other.is(ModBlockTags.STICKABLE_WITH_SLIDING_RAILS)) return other.isStickyBlock();
        Direction.Axis axis = state.getOptionalValue(BlockStateProperties.AXIS)
            .or(() -> state.getOptionalValue(BlockStateProperties.FACING).map(Direction::getAxis))
            .orElse(null);
        Direction.Axis otherAxis = other.getOptionalValue(BlockStateProperties.AXIS)
            .or(() -> state.getOptionalValue(BlockStateProperties.FACING).map(Direction::getAxis))
            .orElse(null);
        if (!Objects.equals(otherAxis, axis)) return false;
        if (axis == null) {
            return pos.relative(Direction.Axis.X, -1).equals(otherPos)
                   || pos.relative(Direction.Axis.X, 1).equals(otherPos)
                   || pos.relative(Direction.Axis.Y, -1).equals(otherPos)
                   || pos.relative(Direction.Axis.Y, 1).equals(otherPos)
                   || pos.relative(Direction.Axis.Z, -1).equals(otherPos)
                   || pos.relative(Direction.Axis.Z, 1).equals(otherPos);
        }
        return pos.relative(axis, -1).equals(otherPos) || pos.relative(axis, 1).equals(otherPos);
    }

    static void stopSlidingBlock(SlidingBlockEntity entity) {
        entity.stop();
        MOVING_PISTON_MAP.remove(entity.blockPosition());
    }

    class PistonPushInfo {
        public BlockPos fromPos;
        public Direction direction;
        public boolean extending;
        public boolean isSourcePiston;

        public PistonPushInfo(BlockPos blockPos, Direction direction) {
            this.fromPos = blockPos;
            this.direction = direction;
            this.extending = false;
            this.isSourcePiston = false;
        }
    }
}
