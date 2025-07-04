package dev.dubhe.anvilcraft.block.sliding;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.entity.SlidingBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SlidingRailBlock extends Block implements IHammerChangeable, IHammerRemovable, ISlidingRail {
    public static final VoxelShape OUTSIDE = Block.box(0, 0, 0, 16, 16, 16);
    public static final VoxelShape AABB_X = Stream.of(
        Block.box(0, 6, 11, 16, 12, 14),
        Block.box(0, 0, 0, 16, 6, 16),
        Block.box(0, 12, 0, 16, 16, 5),
        Block.box(0, 12, 11, 16, 16, 16),
        Block.box(0, 6, 2, 16, 12, 5)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final VoxelShape AABB_Z =
        Stream.of(
            Block.box(2, 6, 0, 5, 12, 16),
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(11, 12, 0, 16, 16, 16),
            Block.box(0, 12, 0, 5, 16, 16),
            Block.box(11, 6, 0, 14, 12, 16)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final VoxelShape AABB_Y =
        Stream.of(
            Block.box(0, 0, 0, 16, 6, 16),
            Block.box(11, 6, 11, 16, 16, 16),
            Block.box(0, 6, 11, 5, 16, 16),
            Block.box(0, 6, 0, 5, 16, 5),
            Block.box(11, 6, 0, 16, 16, 5)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

    public SlidingRailBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(AXIS, Axis.X));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(AXIS, context.getHorizontalDirection().getOpposite().getAxis());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return OUTSIDE;
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
        return switch (blockState.getValue(AXIS)) {
            case X -> AABB_X;
            case Z -> AABB_Z;
            case Y -> AABB_Y;
        };
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        if (!level.getBlockState(neighbor).is(Blocks.MOVING_PISTON)) return;
        Direction dir = level.getBlockState(neighbor).getValue(FACING);
        if (dir.getAxis() == Axis.Y || !neighbor.equals(pos.above())) {
            MOVING_PISTON_MAP.remove(pos);
            return;
        }
        PistonPushInfo ppi = new PistonPushInfo(neighbor, dir);
        if (MOVING_PISTON_MAP.containsKey(pos)) {
            MOVING_PISTON_MAP.get(pos).fromPos = neighbor;
        } else MOVING_PISTON_MAP.put(pos, ppi);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;
        BlockState blockState = level.getBlockState(fromPos);
        if (!MOVING_PISTON_MAP.containsKey(pos)) return;
        if (blockState.is(Blocks.MOVING_PISTON)) return;
        level.scheduleTick(pos, this, 2);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!MOVING_PISTON_MAP.containsKey(pos)) return;
        if (!MOVING_PISTON_MAP.get(pos).extending && MOVING_PISTON_MAP.get(pos).isSourcePiston) {
            MOVING_PISTON_MAP.remove(pos);
            return;
        } else if (!MOVING_PISTON_MAP.get(pos).extending) {
            MOVING_PISTON_MAP.get(pos).direction = MOVING_PISTON_MAP.get(pos).direction.getOpposite();
        }
        level.blockEvent(pos, this, 0, MOVING_PISTON_MAP.get(pos).direction.get3DDataValue());
        MOVING_PISTON_MAP.remove(pos);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        Direction direction = Direction.from3DDataValue(param);
        return moveBlocks(level, pos.above(), direction);
    }

    private static boolean moveBlocks(Level level, BlockPos pos, Direction facing) {
        PistonStructureResolver resolver = new PistonStructureResolver(level, pos.relative(facing.getOpposite()), facing, true);
        if (!resolver.resolve()) return false;
        List<Triple<BlockPos, BlockState, Optional<CompoundTag>>> toPushes = new ArrayList<>();
        List<BlockPos> toPushPoses = resolver.getToPush();

        for (BlockPos toPushPos : toPushPoses) {
            BlockState toPushState = level.getBlockState(toPushPos);
            Optional<CompoundTag> toPushEntityData = Optional.ofNullable(level.getBlockEntity(toPushPos))
                .map(entity -> entity.saveCustomOnly(level.registryAccess()));
            toPushes.add(Triple.of(toPushPos, toPushState, toPushEntityData));
        }

        List<BlockPos> toDestroys = resolver.getToDestroy();

        for (int j = toDestroys.size() - 1; j >= 0; j--) {
            BlockPos destroyingPos = toDestroys.get(j);
            BlockState destroyingState = level.getBlockState(destroyingPos);
            BlockEntity destroyingEntity = destroyingState.hasBlockEntity() ? level.getBlockEntity(destroyingPos) : null;
            dropResources(destroyingState, level, destroyingPos, destroyingEntity);
            destroyingState.onDestroyedByPushReaction(level, destroyingPos, facing, level.getFluidState(destroyingPos));
        }

        for (BlockPos toPushPos : toPushPoses) {
            level.setBlock(toPushPos, Blocks.AIR.defaultBlockState(), 0b1010010);
        }

        SlidingBlockEntity.slid(level, pos, facing, toPushes);
        return true;
    }

    @Override
    public boolean change(Player player, BlockPos blockPos, @NotNull Level level, ItemStack anvilHammer) {
        BlockState bs = level.getBlockState(blockPos);
        level.setBlockAndUpdate(blockPos, bs.cycle(AXIS));
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public @Nullable Property<?> getChangeableProperty(BlockState blockState) {
        return AXIS;
    }

    @Override
    public void onSlidingAbove(Level level, BlockState state, SlidingBlockEntity entity) {
    }
}
