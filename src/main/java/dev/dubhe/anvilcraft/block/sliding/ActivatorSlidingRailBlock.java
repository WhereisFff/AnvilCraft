package dev.dubhe.anvilcraft.block.sliding;

import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.block.entity.ActivatorSlidingRailBlockEntity;
import dev.dubhe.anvilcraft.entity.SlidingBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
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
public class ActivatorSlidingRailBlock extends BaseSlidingRailBlock implements IHammerChangeable, EntityBlock {
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

    public ActivatorSlidingRailBlock(Properties properties) {
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = state.getValue(POWERED);
        boolean shouldPower = this.isPowered(level, pos);
        if (powered != shouldPower) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, shouldPower));
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Optional<ActivatorSlidingRailBlockEntity> beOp = level.getBlockEntity(pos, ModBlockEntities.ACTIVATOR_SLIDING_RAIL.get());
        if (beOp.map(ActivatorSlidingRailBlockEntity::isShouldPower).orElse(false)) {
            beOp.ifPresent(ActivatorSlidingRailBlockEntity::shouldNotPower);
            level.scheduleTick(pos, this, 20);
            level.updateNeighbourForOutputSignal(pos, this);
            return;
        } else {
            BlockPos fromPos = pos.above();
            if (level.isEmptyBlock(fromPos)) return;
            PistonPushInfo ppi = new PistonPushInfo(fromPos, state.getValue(FACING));
            ppi.extending = true;
            if (!MOVING_PISTON_MAP.containsKey(pos)) {
                MOVING_PISTON_MAP.put(pos, ppi);
            }
        }
        super.tick(state, level, pos, random);
    }

    private boolean isPowered(Level level, BlockPos pos) {
        for (Direction side : SIGNAL_SOURCE_SIDES) {
            if (level.getSignal(pos.relative(side), side) > 0) return true;
        }
        return false;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!state.getValue(POWERED)) return 0;
        if (!level.getBlockEntity(pos, ModBlockEntities.ACTIVATOR_SLIDING_RAIL.get())
            .map(ActivatorSlidingRailBlockEntity::isShouldPower)
            .orElse(false)
        ) return 0;
        return direction == Direction.DOWN ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
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
        if (entity.getStartPos().equals(pos.above())) return;
        if (!state.getValue(POWERED)) return;
        level.setBlockAndUpdate(pos, state.setValue(FACING, entity.getMoveDirection()));
        level.getBlockEntity(pos, ModBlockEntities.ACTIVATOR_SLIDING_RAIL.get()).ifPresent(ActivatorSlidingRailBlockEntity::shouldPower);
        ISlidingRail.stopSlidingBlock(entity);
        level.scheduleTick(pos, this, 4);
        BlockPos blockpos = pos.above();
        BlockState blockstate = level.getBlockState(blockpos);
        blockstate.onNeighborChange(level, blockpos, pos);
        level.neighborChanged(blockstate, blockpos, this, pos, false);
        if (!blockstate.isRedstoneConductor(level, blockpos)) return;
        blockpos = blockpos.above();
        blockstate = level.getBlockState(blockpos);
        if (!blockstate.getWeakChanges(level, blockpos)) return;
        level.neighborChanged(blockstate, blockpos, this, pos, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.ACTIVATOR_SLIDING_RAIL.create(pos, state);
    }
}
