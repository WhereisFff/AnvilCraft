package dev.dubhe.anvilcraft.block.sliding;

import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.entity.SlidingBlockEntity;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlidingRailStopBlock extends BaseSlidingRailBlock implements IHammerRemovable {
    public static final VoxelShape SHAPE = Stream.of(
        Block.box(11, 6, 11, 16, 16, 16),
        Block.box(0, 0, 0, 16, 6, 16),
        Block.box(11, 6, 0, 16, 16, 5),
        Block.box(0, 6, 0, 5, 16, 5),
        Block.box(0, 6, 11, 5, 16, 16)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public SlidingRailStopBlock(Properties properties) {
        super(properties, false);
    }

    @Override
    protected VoxelShape getShape(
        BlockState state,
        BlockGetter level,
        BlockPos pos,
        CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void stepOn(
        Level level,
        BlockPos pos,
        BlockState state,
        Entity entity
    ) {
        Vec3 blockPos = pos.getCenter();
        Vec3 entityPos = entity.position();
        Vector3f acceleration = blockPos.toVector3f()
            .sub(entityPos.toVector3f())
            .mul(0.45f)
            .div(0.98f)
            .mul(new Vector3f(1, 0, 1));
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5f, 0.5f, 0.5f).add(new Vec3(acceleration)));
    }

    @Override
    public void onSlidingAbove(Level level, BlockState state, SlidingBlockEntity entity) {
        ISlidingRail.stopSlidingBlock(entity);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
        Direction direction = null;
        for (Direction side : Direction.values()) {
            if (side.getAxis() == Direction.Axis.Y) return;
            Optional<Direction> dirOp = Util.castSafely(level.getBlockState(pos.relative(side)).getBlock(), ISlidingRail.class)
                .flatMap(rail -> rail.getSlidingDirection(level, state));
            if (dirOp.isEmpty()) continue;
            direction = dirOp.get();
            break;
        }
        if (direction == null) return;
        Direction finalDirection = direction;
        Optional.ofNullable(ISlidingRail.MOVING_PISTON_MAP.get(pos))
            .ifPresent(info -> info.direction = finalDirection);
    }
}
