package dev.dubhe.anvilcraft.block.cfa;

import dev.anvilcraft.lib.v2.util.ShapeUtil;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.entity.CelestialForgingAnvilBlockEntity;
import dev.dubhe.anvilcraft.block.entity.CelestialForgingAnvilPortalBlockEntity;
import dev.dubhe.anvilcraft.block.multipart.FlexibleMultiPartBlock;
import dev.dubhe.anvilcraft.block.state.Cube323PartHalf;
import dev.dubhe.anvilcraft.block.state.DirectionGate331PartHalf;
import dev.dubhe.anvilcraft.init.block.ModBlockEntities;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/// 锻星砧传送门：3（宽）×1（深）×3（高）的柔性多方块。
/// 核心控制器为 BOTTOM_CENTER（底层中心），其世界坐标与旧版单方块传送门完全一致，
/// 因此虫洞/传送/激光等所有相对坐标逻辑无需改动。
/// 仅底层中心与正中心两格拥有方块实体，负责传送、激光与含水状态。
public class CelestialForgingAnvilPortalBlock
    extends FlexibleMultiPartBlock<DirectionGate331PartHalf, DirectionProperty, Direction>
    implements IHammerRemovable, EntityBlock, SimpleWaterloggedBlock {

    public static final EnumProperty<DirectionGate331PartHalf> HALF =
        EnumProperty.create("half", DirectionGate331PartHalf.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    /// 传送门各非中心格的碰撞箱（朝北时定义，其余朝向旋转得到）。
    /// 由 blockbench 导出的 VoxelShape 按格拟合而成，倾斜的三角形上沿用阶梯小方块近似。
    private static final VoxelShape WEST_BOTTOM_NORTH = ShapeUtil.merge(
        new AABB(6, 12.1, 0.5, 10, 16, 4.5),
        new AABB(5.5, 11.6, 0, 10.5, 16, 5),
        new AABB(8, 7.1, 6.1, 13, 16, 14.1),
        new AABB(0, 8.1, 4, 7, 12.1, 16),
        new AABB(0, 4.1, 6, 7, 8.1, 16),
        new AABB(0, 0.1, 2, 16, 4.1, 16),
        new AABB(5, 2, 0, 11, 4, 5),
        new AABB(3, 0, 0, 13, 2, 7),
        new AABB(7, 4.1, 2, 9, 13.1, 16),
        new AABB(12, 5.1, 9, 14, 16, 13),
        new AABB(9, 4.1, 13, 16, 13.1, 16)
    );
    private static final VoxelShape WEST_BOTTOM_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, WEST_BOTTOM_NORTH);
    private static final VoxelShape WEST_BOTTOM_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, WEST_BOTTOM_NORTH);
    private static final VoxelShape WEST_BOTTOM_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, WEST_BOTTOM_NORTH);
    private static final VoxelShape WEST_MID_NORTH = ShapeUtil.merge(
        new AABB(6, 0, 0.5, 10, 1.1, 4.5),
        new AABB(5.5, 0, 0, 10.5, 1.6, 5),
        new AABB(8, 0, 6.1, 13, 1.1, 14.1),
        new AABB(9, 1.1, 7, 12, 16, 13),
        new AABB(12, 0, 9, 14, 16, 13),
        new AABB(7, 2.1, 5, 10, 7.1, 15),
        new AABB(7, 8.1, 5, 10, 13.1, 15),
        new AABB(7, 14.1, 5, 10, 16, 15)
    );
    private static final VoxelShape WEST_MID_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, WEST_MID_NORTH);
    private static final VoxelShape WEST_MID_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, WEST_MID_NORTH);
    private static final VoxelShape WEST_MID_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, WEST_MID_NORTH);
    private static final VoxelShape WEST_TOP_NORTH = ShapeUtil.merge(
        new AABB(9, 0, 7, 12, 15.1, 13),
        new AABB(12, 0, 9, 14, 15.1, 13),
        new AABB(7, 0, 5, 10, 3.1, 15),
        new AABB(7, 4.1, 5, 10, 9.1, 15)
    );
    private static final VoxelShape WEST_TOP_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, WEST_TOP_NORTH);
    private static final VoxelShape WEST_TOP_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, WEST_TOP_NORTH);
    private static final VoxelShape WEST_TOP_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, WEST_TOP_NORTH);
    private static final VoxelShape CENTER_TOP_NORTH = ShapeUtil.merge(
        new AABB(5, 9.1, 7, 11, 15.1, 13),
        new AABB(3, 7.1, 5, 11, 15.1, 7),
        new AABB(3, 7.1, 13, 11, 15.1, 15)
    );
    private static final VoxelShape CENTER_TOP_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, CENTER_TOP_NORTH);
    private static final VoxelShape CENTER_TOP_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, CENTER_TOP_NORTH);
    private static final VoxelShape CENTER_TOP_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, CENTER_TOP_NORTH);
    private static final VoxelShape EAST_BOTTOM_NORTH = ShapeUtil.merge(
        new AABB(3, 7.1, 6.1, 8, 16, 14.1),
        new AABB(9, 8.1, 4, 16, 12.1, 16),
        new AABB(9, 4.1, 6, 16, 8.1, 16),
        new AABB(0, 0.1, 2, 16, 4.1, 16),
        new AABB(5, 2, 0, 11, 4, 5),
        new AABB(3, 0, 0, 13, 2, 7),
        new AABB(7, 4.1, 2, 9, 13.1, 16),
        new AABB(2, 5.1, 9, 4, 16, 13),
        new AABB(0, 4.1, 13, 7, 13.1, 16),
        new AABB(6, 12.1, 0.5, 10, 16, 4.5),
        new AABB(5.5, 11.6, 0, 10.5, 16, 5)
    );
    private static final VoxelShape EAST_BOTTOM_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, EAST_BOTTOM_NORTH);
    private static final VoxelShape EAST_BOTTOM_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, EAST_BOTTOM_NORTH);
    private static final VoxelShape EAST_BOTTOM_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, EAST_BOTTOM_NORTH);
    private static final VoxelShape EAST_MID_NORTH = ShapeUtil.merge(
        new AABB(3, 0, 6.1, 8, 1.1, 14.1),
        new AABB(4, 1.1, 7, 7, 16, 13),
        new AABB(2, 0, 9, 4, 16, 13),
        new AABB(6, 2.1, 5, 9, 7.1, 15),
        new AABB(6, 8.1, 5, 9, 13.1, 15),
        new AABB(6, 14.1, 5, 9, 16, 15),
        new AABB(6, 0, 0.5, 10, 1.1, 4.5),
        new AABB(5.5, 0, 0, 10.5, 1.6, 5)
    );
    private static final VoxelShape EAST_MID_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, EAST_MID_NORTH);
    private static final VoxelShape EAST_MID_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, EAST_MID_NORTH);
    private static final VoxelShape EAST_MID_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, EAST_MID_NORTH);
    private static final VoxelShape EAST_TOP_NORTH = ShapeUtil.merge(
        new AABB(4, 0, 7, 7, 15.1, 13),
        new AABB(2, 0, 9, 4, 15.1, 13),
        new AABB(6, 0, 5, 9, 3.1, 15),
        new AABB(6, 4.1, 5, 9, 9.1, 15)
    );
    private static final VoxelShape EAST_TOP_EAST = ShapeUtil.rotate(Direction.Axis.Y, 270, EAST_TOP_NORTH);
    private static final VoxelShape EAST_TOP_SOUTH = ShapeUtil.rotate(Direction.Axis.Y, 180, EAST_TOP_NORTH);
    private static final VoxelShape EAST_TOP_WEST = ShapeUtil.rotate(Direction.Axis.Y, 90, EAST_TOP_NORTH);

    /// 中心两格（开口）与锻星砧接触面上的 2 像素薄板（FACING 的反方向）。
    /// 紧贴锻星砧，不影响玩家穿行，但能拦截正面射入的普通激光使其被传送门接收。
    private static final VoxelShape SLAB_NORTH = Block.box(0, 0, 0, 16, 14, 3);
    private static final VoxelShape SLAB_SOUTH = Block.box(0, 0, 13, 16, 14, 16);
    private static final VoxelShape SLAB_WEST = Block.box(0, 0, 0, 3, 13, 16);
    private static final VoxelShape SLAB_EAST = Block.box(13, 0, 0, 16, 13, 16);

    public CelestialForgingAnvilPortalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
            .setValue(HALF, DirectionGate331PartHalf.BOTTOM_CENTER)
            .setValue(FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(WATERLOGGED, false));
    }

    // === 柔性多方块定义 ===

    @Override
    public Property<DirectionGate331PartHalf> getPart() {
        return HALF;
    }

    @Override
    public DirectionGate331PartHalf[] getParts() {
        return DirectionGate331PartHalf.values();
    }

    @Override
    public DirectionProperty getAdditionalProperty() {
        return FACING;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING, OPEN, WATERLOGGED);
    }

    @Override
    public BlockState placedState(DirectionGate331PartHalf part, BlockState state) {
        return state.setValue(this.getPart(), part);
    }

    /// 模型承载部件为正中心（MID_CENTER），用于铁砧锤预览与伽马激光破坏定位。
    @Override
    public BlockState mapRealModelHolderBlock(Level level, BlockPos blockPos, BlockState original) {
        return original.setValue(HALF, DirectionGate331PartHalf.MID_CENTER);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    // === 碰撞箱 ===

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        DirectionGate331PartHalf half = state.getValue(HALF);
        Direction facing = state.getValue(FACING);
        /// 中心列底部与中部为传送门开口：仅在紧贴锻星砧的背面保留 2 像素薄板，
        /// 既不阻挡玩家穿行，又能拦截正面射入的普通激光使其被传送门接收。
        if (half == DirectionGate331PartHalf.BOTTOM_CENTER || half == DirectionGate331PartHalf.MID_CENTER) {
            return switch (facing) {
                case NORTH -> SLAB_SOUTH;
                case SOUTH -> SLAB_NORTH;
                case WEST -> SLAB_EAST;
                case EAST -> SLAB_WEST;
                default -> Shapes.empty();
            };
        }
        return switch (half) {
            case TOP_CENTER -> switch (facing) {
                case SOUTH -> CENTER_TOP_SOUTH;
                case WEST -> CENTER_TOP_WEST;
                case EAST -> CENTER_TOP_EAST;
                default -> CENTER_TOP_NORTH;
            };
            case BOTTOM_LEFT -> switch (facing) {
                case SOUTH -> WEST_BOTTOM_SOUTH;
                case WEST -> WEST_BOTTOM_WEST;
                case EAST -> WEST_BOTTOM_EAST;
                default -> WEST_BOTTOM_NORTH;
            };
            case MID_LEFT -> switch (facing) {
                case SOUTH -> WEST_MID_SOUTH;
                case WEST -> WEST_MID_WEST;
                case EAST -> WEST_MID_EAST;
                default -> WEST_MID_NORTH;
            };
            case TOP_LEFT -> switch (facing) {
                case SOUTH -> WEST_TOP_SOUTH;
                case WEST -> WEST_TOP_WEST;
                case EAST -> WEST_TOP_EAST;
                default -> WEST_TOP_NORTH;
            };
            case BOTTOM_RIGHT -> switch (facing) {
                case SOUTH -> EAST_BOTTOM_SOUTH;
                case WEST -> EAST_BOTTOM_WEST;
                case EAST -> EAST_BOTTOM_EAST;
                default -> EAST_BOTTOM_NORTH;
            };
            case MID_RIGHT -> switch (facing) {
                case SOUTH -> EAST_MID_SOUTH;
                case WEST -> EAST_MID_WEST;
                case EAST -> EAST_MID_EAST;
                default -> EAST_MID_NORTH;
            };
            case TOP_RIGHT -> switch (facing) {
                case SOUTH -> EAST_TOP_SOUTH;
                case WEST -> EAST_TOP_WEST;
                case EAST -> EAST_TOP_EAST;
                default -> EAST_TOP_NORTH;
            };
            default -> Shapes.empty();
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    // === 含水 ===

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
            ? Fluids.WATER.getSource(false)
            : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // === 放置 ===

    /// 仅允许放置在锻星砧侧面中心旁。FACING 指向远离锻星砧的方向（朝向玩家），
    /// 因此长度 3 的边与传送门正面始终面对玩家，背面紧贴锻星砧。
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState cfaState = level.getBlockState(pos.relative(dir));
            if (cfaState.is(ModBlocks.CELESTIAL_FORGING_ANVIL)) {
                Cube323PartHalf cfaHalf = cfaState.getValue(CelestialForgingAnvilBlock.HALF);
                if (cfaHalf == Cube323PartHalf.BOTTOM_N || cfaHalf == Cube323PartHalf.BOTTOM_S
                    || cfaHalf == Cube323PartHalf.BOTTOM_E || cfaHalf == Cube323PartHalf.BOTTOM_W) {
                    FluidState fluidState = level.getFluidState(pos);
                    return this.defaultBlockState()
                        .setValue(HALF, DirectionGate331PartHalf.BOTTOM_CENTER)
                        .setValue(FACING, dir.getOpposite())
                        .setValue(OPEN, false)
                        .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
                }
            }
        }
        if (context.getPlayer() instanceof ServerPlayer sp) {
            sp.sendSystemMessage(
                Component.translatable("message.anvilcraft.portal.invalid_placement")
                    .withStyle(ChatFormatting.RED), true);
        }
        return null;
    }

    /// 让放置预览反映真实朝向（长度 3 的边正对玩家），而非默认 NORTH。
    @Override
    public BlockState getPlacementState(BlockPlaceContext context) {
        return this.getStateForPlacement(context);
    }


    /// 放置其余部件，并为每个部件按所在位置独立设置含水状态；随后在锻星砧上注册传送门。
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!state.hasProperty(this.getPart())) return;
        for (DirectionGate331PartHalf part : this.getParts()) {
            if (part == state.getValue(this.getPart())) continue;
            BlockPos partPos = pos.offset(this.offsetFrom(state, part));
            FluidState fluidState = level.getFluidState(partPos);
            BlockState newState = this.placedState(part, state)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER && fluidState.isSource());
            level.setBlockAndUpdate(partPos, newState);
        }
        if (!level.isClientSide()) {
            registerToCfa(level, pos, state);
        }
    }

    /// 在锻星砧上注册（或注销）此传送门。pos 为传送门核心控制器（BOTTOM_CENTER）的位置。
    private static void registerToCfa(Level level, BlockPos controllerPos, BlockState state) {
        Direction towardsCfa = state.getValue(FACING).getOpposite();
        BlockPos cfaPos = controllerPos.relative(towardsCfa);
        BlockState cfaState = level.getBlockState(cfaPos);
        if (!(cfaState.getBlock() instanceof CelestialForgingAnvilBlock)) return;
        Cube323PartHalf cfaHalf = cfaState.getValue(CelestialForgingAnvilBlock.HALF);
        if (cfaHalf != Cube323PartHalf.BOTTOM_N && cfaHalf != Cube323PartHalf.BOTTOM_S
            && cfaHalf != Cube323PartHalf.BOTTOM_E && cfaHalf != Cube323PartHalf.BOTTOM_W) {
            return;
        }
        BlockPos cfaControllerPos = cfaPos.offset(cfaHalf.getOffset().multiply(-1));
        if (level.getBlockEntity(cfaControllerPos) instanceof CelestialForgingAnvilBlockEntity cfaBe) {
            cfaBe.addPortal(cfaHalf, controllerPos);
        }
    }

    private static void unregisterFromCfa(Level level, BlockPos controllerPos, BlockState state) {
        Direction towardsCfa = state.getValue(FACING).getOpposite();
        BlockPos cfaPos = controllerPos.relative(towardsCfa);
        BlockState cfaState = level.getBlockState(cfaPos);
        if (!(cfaState.getBlock() instanceof CelestialForgingAnvilBlock)) return;
        Cube323PartHalf cfaHalf = cfaState.getValue(CelestialForgingAnvilBlock.HALF);
        BlockPos cfaControllerPos = cfaPos.offset(cfaHalf.getOffset().multiply(-1));
        if (level.getBlockEntity(cfaControllerPos) instanceof CelestialForgingAnvilBlockEntity cfaBe) {
            cfaBe.removePortal(cfaHalf);
        }
    }

    // === 方块实体（仅中心列底层与中部两格）===

    /// 给定部件是否为功能格（拥有方块实体）：底层中心与正中心。
    public static boolean isFunctionalPart(DirectionGate331PartHalf half) {
        return half == DirectionGate331PartHalf.BOTTOM_CENTER || half == DirectionGate331PartHalf.MID_CENTER;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.hasProperty(HALF) && isFunctionalPart(state.getValue(HALF))) {
            return ModBlockEntities.CELESTIAL_FORGING_ANVIL_PORTAL.create(pos, state);
        }
        return null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (level.isClientSide()) return null;
        if (type == ModBlockEntities.CELESTIAL_FORGING_ANVIL_PORTAL.get()) {
            return (lvl, pos, st, be) -> ((CelestialForgingAnvilPortalBlockEntity) be).tick();
        }
        return null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide()
                && state.hasProperty(HALF)
                && state.getValue(HALF) == DirectionGate331PartHalf.BOTTOM_CENTER) {
                unregisterFromCfa(level, pos, state);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // === 实体传送 ===

    /// 实体进入开口格时触发传送。vanilla 在每个移动步调用此方法（配合上面整格的
    /// getEntityInsideCollisionShape），因此快速的投掷物也能可靠命中而不会穿过。
    /// 底层中心与正中心均可触发；正中心会将传送委托给底层中心处理，
    /// 确保 touchingEntities 去重等状态一致。
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!state.getValue(OPEN)) return;
        DirectionGate331PartHalf half = state.getValue(HALF);
        if (half != DirectionGate331PartHalf.BOTTOM_CENTER
            && half != DirectionGate331PartHalf.MID_CENTER) return;
        BlockPos anchorPos = half == DirectionGate331PartHalf.BOTTOM_CENTER ? pos : pos.below();
        if (level.getBlockEntity(anchorPos) instanceof CelestialForgingAnvilPortalBlockEntity portalBe) {
            portalBe.tryTouchTeleport(entity);
        }
    }

    /// 由侧面（Cube323PartHalf）推导传送门正面朝向（远离锻星砧）。
    public static Direction getFacingFromSide(Cube323PartHalf side) {
        return switch (side) {
            case BOTTOM_N -> Direction.NORTH;
            case BOTTOM_S -> Direction.SOUTH;
            case BOTTOM_E -> Direction.EAST;
            case BOTTOM_W -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}

