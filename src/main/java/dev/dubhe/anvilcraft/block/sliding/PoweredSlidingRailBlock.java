package dev.dubhe.anvilcraft.block.sliding;

import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.entity.SlidingBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PoweredSlidingRailBlock extends BaseSlidingRailBlock implements IHammerChangeable {
    public static final List<Direction> SIGNAL_SOURCE_SIDES = List.of(
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    public static final VoxelShape AABB_X = Stream.of(
        Block.box(0, 0, 0, 16, 6, 16),
        Block.box(0, 6, 11, 16, 16, 16),
        Block.box(0, 6, 0, 16, 16, 5)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final VoxelShape AABB_Z =
        Stream.of(
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(11, 6, 0, 16, 16, 16),
            Block.box(0, 6, 0, 5, 16, 16)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PoweredSlidingRailBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            facing = facing.getOpposite();
        }
        return this.defaultBlockState()
            .setValue(FACING, facing)
            .setValue(POWERED, this.isPowered(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(
        BlockState blockState,
        BlockGetter blockGetter,
        BlockPos blockPos,
        CollisionContext collisionContext
    ) {
        return switch (blockState.getValue(FACING).getAxis()) {
            case X -> AABB_X;
            case Z -> AABB_Z;
            default -> super.getShape(blockState, blockGetter, blockPos, collisionContext);
        };
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (!state.getValue(POWERED)) return;
        super.onNeighborChange(state, level, pos, neighbor);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = state.getValue(POWERED);
        boolean shouldPower = this.isPowered(level, pos);
        if (powered != shouldPower) {
            powered = shouldPower;
            level.setBlockAndUpdate(pos, state.setValue(POWERED, shouldPower));
        }
        if (powered) {
            fromPos = pos.above();
            if (level.isEmptyBlock(fromPos)) {
                BlockPos stop = pos.relative(state.getValue(FACING).getOpposite());
                if (!level.getBlockState(stop).is(ModBlocks.SLIDING_RAIL_STOP) || level.isEmptyBlock(stop.above())) return;
                BlockState above = level.getBlockState(stop.above());
                level.setBlock(stop.above(), Blocks.AIR.defaultBlockState(), 0b1000011);
                level.setBlock(fromPos, above, 0b1000011);
            }
            PistonPushInfo ppi = new PistonPushInfo(fromPos, state.getValue(FACING));
            ppi.extending = true;
            if (MOVING_PISTON_MAP.containsKey(pos)) {
                MOVING_PISTON_MAP.get(pos).fromPos = fromPos;
            } else MOVING_PISTON_MAP.put(pos, ppi);
        }
        if (level.isClientSide) return;
        BlockState blockState = level.getBlockState(MOVING_PISTON_MAP.get(pos) instanceof PistonPushInfo info ? info.fromPos : fromPos);
        if (!MOVING_PISTON_MAP.containsKey(pos)) return;
        if (blockState.is(Blocks.MOVING_PISTON) || blockState.isAir()) return;
        level.scheduleTick(pos, this, 2);
    }

    private boolean isPowered(SignalGetter level, BlockPos pos) {
        for (Direction side : SIGNAL_SOURCE_SIDES) {
            if (level.getSignal(pos.relative(side), side) > 0) return true;
        }
        return false;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity.getType() != EntityType.ITEM) return;
        if (!state.getValue(POWERED)) {
            ISlidingRail.absorbEntity(pos, entity);
        } else {
            entity.setDeltaMovement(Vec3.ZERO.relative(state.getValue(FACING), 0.5));
        }
    }

    @Override
    public boolean change(Player player, BlockPos blockPos, @NotNull Level level, ItemStack anvilHammer) {
        BlockState bs = level.getBlockState(blockPos);
        level.setBlockAndUpdate(blockPos, bs.cycle(FACING));
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public @Nullable Property<?> getChangeableProperty(BlockState blockState) {
        return FACING;
    }

    @Override
    public void onSlidingAbove(Level level, BlockPos pos, BlockState state, SlidingBlockEntity entity) {
        if (!state.getValue(POWERED)) {
            ISlidingRail.stopSlidingBlock(entity);
            return;
        }
        entity.setMoveDirection(state.getValue(FACING));
    }

    @Override
    public boolean canMoveBlockToTop(LevelReader level, BlockPos pos, BlockState state, BlockState top) {
        return state.getValue(POWERED);
    }
}
