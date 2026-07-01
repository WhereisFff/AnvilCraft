package dev.dubhe.anvilcraft.block;

import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.state.Orientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockComparatorBlock extends Block implements IHammerChangeable, IHammerRemovable {

    public static final MapCodec<BlockComparatorBlock> CODEC = simpleCodec(BlockComparatorBlock::new);

    public static final EnumProperty<Orientation> ORIENTATION =
        EnumProperty.create("orientation", Orientation.class);
    public static final BooleanProperty PRECISE = BooleanProperty.create("precise");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    // Horizontal shapes (upright on floor)
    public static final VoxelShape NORTH_UP_SHAPE =
        Shapes.or(Block.box(0, 4, 0, 16, 7, 6), Block.box(4, 0, 3, 12, 8, 16));
    public static final VoxelShape SOUTH_UP_SHAPE =
        Shapes.or(Block.box(0, 4, 10, 16, 7, 16), Block.box(4, 0, 0, 12, 8, 13));
    public static final VoxelShape EAST_UP_SHAPE =
        Shapes.or(Block.box(10, 4, 0, 16, 7, 16), Block.box(0, 0, 4, 13, 8, 12));
    public static final VoxelShape WEST_UP_SHAPE =
        Shapes.or(Block.box(0, 4, 0, 6, 7, 16), Block.box(3, 0, 4, 16, 8, 12));

    // Up-facing shapes (on wall, output goes up)
    public static final VoxelShape UP_NORTH_SHAPE =
        Shapes.or(Block.box(0, 10, 10, 16, 16, 16), Block.box(4, 0, 8, 12, 13, 16));
    public static final VoxelShape UP_SOUTH_SHAPE =
        Shapes.or(Block.box(0, 10, 0, 16, 16, 6), Block.box(4, 0, 0, 12, 13, 8));
    public static final VoxelShape UP_WEST_SHAPE =
        Shapes.or(Block.box(10, 10, 0, 16, 16, 16), Block.box(8, 0, 4, 16, 13, 12));
    public static final VoxelShape UP_EAST_SHAPE =
        Shapes.or(Block.box(0, 10, 0, 6, 16, 16), Block.box(0, 0, 4, 8, 13, 12));

    // Down-facing shapes (on ceiling, output goes down)
    public static final VoxelShape DOWN_NORTH_SHAPE =
        Shapes.or(Block.box(0, 0, 10, 16, 6, 16), Block.box(4, 3, 8, 12, 16, 16));
    public static final VoxelShape DOWN_SOUTH_SHAPE =
        Shapes.or(Block.box(0, 0, 0, 16, 6, 6), Block.box(4, 3, 0, 12, 16, 8));
    public static final VoxelShape DOWN_WEST_SHAPE =
        Shapes.or(Block.box(10, 0, 0, 16, 6, 16), Block.box(8, 3, 4, 16, 16, 12));
    public static final VoxelShape DOWN_EAST_SHAPE =
        Shapes.or(Block.box(0, 0, 0, 6, 6, 16), Block.box(0, 3, 4, 8, 16, 12));

    public BlockComparatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(ORIENTATION, Orientation.NORTH_UP)
                .setValue(PRECISE, false)
                .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION).add(PRECISE).add(POWERED);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(
        BlockState state,
        BlockGetter level,
        BlockPos pos,
        CollisionContext context
    ) {
        return switch (state.getValue(ORIENTATION)) {
            case NORTH_UP -> NORTH_UP_SHAPE;
            case SOUTH_UP -> SOUTH_UP_SHAPE;
            case EAST_UP -> EAST_UP_SHAPE;
            case WEST_UP -> WEST_UP_SHAPE;
            case UP_NORTH -> UP_NORTH_SHAPE;
            case UP_SOUTH -> UP_SOUTH_SHAPE;
            case UP_WEST -> UP_WEST_SHAPE;
            case UP_EAST -> UP_EAST_SHAPE;
            case DOWN_NORTH -> DOWN_NORTH_SHAPE;
            case DOWN_SOUTH -> DOWN_SOUTH_SHAPE;
            case DOWN_WEST -> DOWN_WEST_SHAPE;
            case DOWN_EAST -> DOWN_EAST_SHAPE;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Orientation orientation;
        Direction horizontalDirection = context.getHorizontalDirection();
        if (context.getNearestLookingDirection() == Direction.UP) {
            orientation = switch (horizontalDirection) {
                case SOUTH -> Orientation.UP_SOUTH;
                case WEST -> Orientation.UP_WEST;
                case EAST -> Orientation.UP_EAST;
                default -> Orientation.UP_NORTH;
            };
        } else if (context.getNearestLookingDirection() == Direction.DOWN) {
            orientation = switch (horizontalDirection) {
                case SOUTH -> Orientation.DOWN_SOUTH;
                case WEST -> Orientation.DOWN_WEST;
                case EAST -> Orientation.DOWN_EAST;
                default -> Orientation.DOWN_NORTH;
            };
        } else {
            orientation = switch (horizontalDirection) {
                case SOUTH -> Orientation.SOUTH_UP;
                case WEST -> Orientation.WEST_UP;
                case EAST -> Orientation.EAST_UP;
                default -> Orientation.NORTH_UP;
            };
        }
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            orientation = orientation.opposite();
        }
        return defaultBlockState().setValue(ORIENTATION, orientation);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide
            || (oldState.is(this) && state.getValue(ORIENTATION) == oldState.getValue(ORIENTATION))
        ) {
            return;
        }
        boolean newPowered = checkBlocks(level, pos, state);
        level.setBlock(pos, state.setValue(POWERED, newPowered), 3);
        this.updateNeighborsInFront(level, pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (level.isClientSide
            || (state.is(newState.getBlock())
                && state.getValue(ORIENTATION) == newState.getValue(ORIENTATION))
        ) {
            return;
        }
        if (state.getValue(POWERED)) {
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult
    ) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        } else {
            BlockState newState = state.cycle(PRECISE);
            level.setBlock(pos, newState.setValue(POWERED, checkBlocks(level, pos, newState)), 2);
            this.updateNeighborsInFront(level, pos, state);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    private boolean checkBlocks(LevelAccessor level, BlockPos pos, BlockState blockState) {
        Orientation orientation = blockState.getValue(ORIENTATION);
        Direction[] dirs = getCompareDirections(orientation);
        BlockState state1 = level.getBlockState(pos.relative(dirs[0]));
        BlockState state2 = level.getBlockState(pos.relative(dirs[1]));
        return blockState.getValue(PRECISE)
            ? state1.equals(state2)
            : state1.getBlock() == state2.getBlock();
    }

    /**
     * Returns the two directions perpendicular to the output direction
     * whose blocks should be compared.
     */
    private static Direction[] getCompareDirections(Orientation orientation) {
        return switch (orientation) {
            case NORTH_UP, UP_NORTH, DOWN_NORTH -> new Direction[]{Direction.EAST, Direction.WEST};
            case SOUTH_UP, UP_SOUTH, DOWN_SOUTH -> new Direction[]{Direction.WEST, Direction.EAST};
            case WEST_UP, UP_WEST, DOWN_WEST -> new Direction[]{Direction.NORTH, Direction.SOUTH};
            case EAST_UP, UP_EAST, DOWN_EAST -> new Direction[]{Direction.SOUTH, Direction.NORTH};
        };
    }

    @Override
    public BlockState updateShape(
        BlockState blockState,
        Direction direction,
        BlockState blockState2,
        LevelAccessor level,
        BlockPos pos,
        BlockPos pos2
    ) {
        Direction facing = blockState.getValue(ORIENTATION).getDirection();
        if (facing.getAxis().isHorizontal()) {
            // Horizontal facing: skip Y-axis changes and changes along the facing axis
            if (direction.getAxis() == Direction.Axis.Y
                || direction.getAxis() == facing.getAxis()
            ) {
                return blockState;
            }
        } else {
            // Vertical facing (UP/DOWN): skip changes along the facing axis only
            if (direction.getAxis() == facing.getAxis()) return blockState;
        }
        if (!level.isClientSide() && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
        return blockState;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean same = checkBlocks(level, pos, state);
        if (same != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, same), 2);
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    protected void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(ORIENTATION).getDirection();
        BlockPos blockpos = pos.relative(direction.getOpposite());
        level.neighborChanged(blockpos, this, pos);
        level.updateNeighborsAtExceptFromFacing(blockpos, this, direction);
    }

    @Override
    public boolean canConnectRedstone(
        BlockState state,
        BlockGetter level,
        BlockPos pos,
        @Nullable Direction direction
    ) {
        return direction == state.getValue(ORIENTATION).getDirection();
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(
        BlockState blockState,
        BlockGetter blockAccess,
        BlockPos pos,
        Direction side
    ) {
        return blockState.getSignal(blockAccess, pos, side);
    }

    @Override
    protected int getSignal(
        BlockState blockState,
        BlockGetter blockAccess,
        BlockPos pos,
        Direction side
    ) {
        return blockState.getValue(POWERED)
            && blockState.getValue(ORIENTATION).getDirection() == side
            ? 15 : 0;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    // -- Hammer changeable support (cycle through 12 orientations) --

    @Override
    public boolean change(Player player, BlockPos blockPos, Level level, ItemStack anvilHammer) {
        BlockState state = level.getBlockState(blockPos);
        state = state.setValue(ORIENTATION, state.getValue(ORIENTATION).next());
        level.setBlockAndUpdate(blockPos, state);
        return true;
    }

    @Override
    @Nullable
    public Property<?> getChangeableProperty(BlockState blockState) {
        return ORIENTATION;
    }

    // -- Structure rotation support --

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ORIENTATION, state.getValue(ORIENTATION).rotate(rotation));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(ORIENTATION, state.getValue(ORIENTATION).mirror(mirror));
    }
}
