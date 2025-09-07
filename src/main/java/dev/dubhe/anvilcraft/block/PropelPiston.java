package dev.dubhe.anvilcraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.injection.block.entity.IPistonMovingBlockEntityExtension;
import dev.dubhe.anvilcraft.block.entity.PropelPistonBlockEntity;
import dev.dubhe.anvilcraft.block.piston.IMoveableEntityBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockEntities;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.init.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PropelPiston extends DirectionalBlock implements IMoveableEntityBlock {
    public static final BooleanProperty EXHAUSTED = BooleanProperty.create("exhausted");
    private CompoundTag nbt;

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(PropelPiston::new);
    }

    public PropelPiston(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EXHAUSTED, true).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(EXHAUSTED)) {
            if (level.getBlockEntity(pos) instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
                if (propelPistonBlockEntity.getStoredEnergy() > 5) {
                    level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, false));
                }
            }
        } else {
            level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, true));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            int storedEnergy = propelPistonBlockEntity.getStoredEnergy();
            if (stack.is(ModItems.CAPACITOR)) {
                if (storedEnergy < 76000) {
                    propelPistonBlockEntity.updateStoredEnergy(storedEnergy + 4000);
                    stack.consume(1, player);
                    player.addItem(ModItems.CAPACITOR_EMPTY.asStack());
                    return ItemInteractionResult.SUCCESS;
                }
            } else if (stack.is(ModItems.SUPER_CAPACITOR)) {
                if (storedEnergy < 20000) {
                    propelPistonBlockEntity.updateStoredEnergy(80000);
                    stack.consume(1, player);
                    player.addItem(ModItems.SUPER_CAPACITOR_EMPTY.asStack());
                    return ItemInteractionResult.SUCCESS;
                }
            }
            if (state.getValue(EXHAUSTED)) {
                if (propelPistonBlockEntity.getStoredEnergy() > 5) {
                    level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, false));
                }
            } else {
                level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, true));
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        if (state.hasProperty(EXHAUSTED) && !state.getValue(EXHAUSTED)) {
            return PushReaction.BLOCK;
        }
        return PushReaction.NORMAL;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (state.getValue(EXHAUSTED)) {
            if (level.hasNeighborSignal(pos)) {
                if (level.getBlockEntity(pos) instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
                    if (propelPistonBlockEntity.getStoredEnergy() > 5) {
                        level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, false));
                    }
                }
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getNearestLookingDirection().getOpposite();
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            if (context.getLevel().hasNeighborSignal(context.getClickedPos())) {
                return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite()).setValue(EXHAUSTED, false);
            }
            return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
        }
        if (context.getLevel().hasNeighborSignal(context.getClickedPos())) {
            return this.defaultBlockState().setValue(FACING, clickedFace).setValue(EXHAUSTED, false);
        }
        return this.defaultBlockState().setValue(FACING, clickedFace);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXHAUSTED, FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.PROPEL_PISTON.create(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type,
            ModBlockEntities.PROPEL_PISTON.get(),
            (level1, blockPos, blockState, blockEntity) ->
                blockEntity.tick(level1, blockPos, blockState, blockEntity));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker
    ) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Integer energy = stack.getOrDefault(ModComponents.STORED_ENERGY, 0);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            propelPistonBlockEntity.updateStoredEnergy(energy);
        }
    }

    @Override
    public CompoundTag clearData(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("storedEnergyData", propelPistonBlockEntity.getStoredEnergy());
            return tag;
        }
        return new CompoundTag();
    }

    @Override
    public void setData(Level level, BlockPos pos, CompoundTag nbt) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            int data = nbt.getInt("storedEnergyData");
            propelPistonBlockEntity.updateStoredEnergy(data);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        Direction direction = state.getValue(PropelPiston.FACING);
        if (!level.isClientSide) {
            boolean flag = state.getValue(EXHAUSTED);
            if (flag) {
                return false;
            }
        }

        if (id == 0) {
            if (net.neoforged.neoforge.event.EventHooks.onPistonMovePre(level, pos, direction, true)) return false;
            if (!this.moveBlocks(level, pos, direction)) {
                return false;
            }
        }
        net.neoforged.neoforge.event.EventHooks.onPistonMovePost(level, pos, direction, (id == 0));
        return true;
    }

    public boolean moveBlocks(Level level, BlockPos pos, Direction facing) {
        BlockPos blockpos = pos.relative(facing);
        PistonStructureResolver pistonstructureresolver = new PistonStructureResolver(level, pos, facing, true);
        if (!pistonstructureresolver.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> map = Maps.newHashMap();
            List<BlockPos> list = pistonstructureresolver.getToPush();
            List<BlockState> list1 = Lists.newArrayList();
            list.add(pos);

            for (BlockPos blockpos1 : list) {
                BlockState blockstate = level.getBlockState(blockpos1);
                list1.add(blockstate);
                map.put(blockpos1, blockstate);
            }

            List<BlockPos> list2 = pistonstructureresolver.getToDestroy();
            BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
            int i = 0;

            for (int j = list2.size() - 1; j >= 0; j--) {
                BlockPos blockpos2 = list2.get(j);
                BlockState blockstate1 = level.getBlockState(blockpos2);
                BlockEntity blockentity = blockstate1.hasBlockEntity() ? level.getBlockEntity(blockpos2) : null;
                dropResources(blockstate1, level, blockpos2, blockentity);
                blockstate1.onDestroyedByPushReaction(level, blockpos2, facing, level.getFluidState(blockpos2));
                if (!blockstate1.is(BlockTags.FIRE)) {
                    level.addDestroyBlockEffect(blockpos2, blockstate1);
                }

                ablockstate[i++] = blockstate1;
            }

            for (int k = list.size() - 1; k >= 0; k--) {
                BlockPos blockpos3 = list.get(k);
                BlockState blockstate5 = level.getBlockState(blockpos3);
                blockpos3 = blockpos3.relative(facing);
                map.remove(blockpos3);
                BlockState blockstate8 = Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, facing);
                nbt = new CompoundTag();
                if (list1.get(k).getBlock() instanceof IMoveableEntityBlock block) {
                    nbt = block.clearData(level, blockpos3.relative(facing.getOpposite()));
                }
                if (level.getBlockEntity(blockpos3.relative(facing.getOpposite())) instanceof PropelPistonBlockEntity) {
                    int data = nbt.getInt("storedEnergyData");
                    nbt.putInt("storedEnergyData", data - 5 * (list.size() - 1));
                }
                level.setBlock(blockpos3, blockstate8, 68);
                BlockEntity blockEntity = MovingPistonBlock.newMovingBlockEntity(blockpos3, blockstate8, list1.get(k), facing, true, false);
                if (blockEntity instanceof IPistonMovingBlockEntityExtension entity) {
                    entity.anvilcraft$setData(nbt);
                }
                level.setBlockEntity(blockEntity);
                ablockstate[i++] = blockstate5;
            }

            BlockState blockstate3 = Blocks.AIR.defaultBlockState();

            for (BlockPos blockpos4 : map.keySet()) {
                level.setBlock(blockpos4, blockstate3, 82);
            }

            for (Map.Entry<BlockPos, BlockState> entry : map.entrySet()) {
                BlockPos blockpos5 = entry.getKey();
                BlockState blockstate2 = entry.getValue();
                blockstate2.updateIndirectNeighbourShapes(level, blockpos5, 2);
                blockstate3.updateNeighbourShapes(level, blockpos5, 2);
                blockstate3.updateIndirectNeighbourShapes(level, blockpos5, 2);
            }

            i = 0;

            for (int l = list2.size() - 1; l >= 0; l--) {
                BlockState blockstate7 = ablockstate[i++];
                BlockPos blockpos6 = list2.get(l);
                blockstate7.updateIndirectNeighbourShapes(level, blockpos6, 2);
                level.updateNeighborsAt(blockpos6, blockstate7.getBlock());
            }

            for (int i1 = list.size() - 1; i1 >= 0; i1--) {
                level.updateNeighborsAt(list.get(i1), ablockstate[i++].getBlock());
            }

            level.updateNeighborsAt(blockpos, Blocks.PISTON_HEAD);

            return true;
        }
    }
}
