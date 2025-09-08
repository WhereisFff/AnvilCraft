package dev.dubhe.anvilcraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PropelPiston extends DirectionalBlock implements IMoveableEntityBlock, IHammerRemovable, IHammerChangeable {
    public static final BooleanProperty EXHAUSTED = BooleanProperty.create("exhausted");

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
        if (level.getBlockEntity(pos) instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            if (state.hasProperty(EXHAUSTED) && state.getValue(EXHAUSTED)) {
                if (propelPistonBlockEntity.getStoredEnergy() > 5) {
                    level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, false));
                }
            } else {
                level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, true));
                propelPistonBlockEntity.updateStoredEnergy(propelPistonBlockEntity.getStoredEnergy());
            }
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
            if (state.hasProperty(EXHAUSTED) && state.getValue(EXHAUSTED)) {
                if (propelPistonBlockEntity.getStoredEnergy() > 5) {
                    level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, false));
                }
            } else {
                level.setBlockAndUpdate(pos, state.setValue(EXHAUSTED, true));
            }
            propelPistonBlockEntity.updateStoredEnergy(propelPistonBlockEntity.getStoredEnergy());
        }
        return ItemInteractionResult.SUCCESS;
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
        if (level.getBlockEntity(pos) instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            if (state.hasProperty(EXHAUSTED) && state.getValue(EXHAUSTED)) {
                if (level.hasNeighborSignal(pos)) {
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
                blockEntity.tick(level1, blockPos, blockState));
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

    @Override
    public boolean change(Player player, BlockPos blockPos, Level level, ItemStack anvilHammer) {
        BlockState state = level.getBlockState(blockPos);
        level.setBlockAndUpdate(blockPos, state.cycle(FACING));
        return true;
    }

    @Override
    public @Nullable Property<?> getChangeableProperty(BlockState blockState) {
        return FACING;
    }

    private boolean moveBlocks(Level level, BlockPos pos, Direction facing) {
        PistonStructureResolver pistonstructureresolver = new PistonStructureResolver(level, pos, facing, true);
        if (!pistonstructureresolver.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> map = Maps.newHashMap();
            List<BlockPos> list = pistonstructureresolver.getToPush();
            List<BlockState> list1 = Lists.newArrayList();
            list.add(pos);

            for (BlockPos blockPos1 : list) {
                BlockState blockState = level.getBlockState(blockPos1);
                list1.add(blockState);
                map.put(blockPos1, blockState);
            }

            List<BlockPos> list2 = pistonstructureresolver.getToDestroy();
            BlockState[] blockStates = new BlockState[list.size() + list2.size()];
            int i = 0;

            for (int j = list2.size() - 1; j >= 0; j--) {
                BlockPos blockPos2 = list2.get(j);
                BlockState blockState1 = level.getBlockState(blockPos2);
                BlockEntity blockentity = blockState1.hasBlockEntity() ? level.getBlockEntity(blockPos2) : null;
                dropResources(blockState1, level, blockPos2, blockentity);
                blockState1.onDestroyedByPushReaction(level, blockPos2, facing, level.getFluidState(blockPos2));
                if (!blockState1.is(BlockTags.FIRE)) {
                    level.addDestroyBlockEffect(blockPos2, blockState1);
                }

                blockStates[i++] = blockState1;
            }

            for (int k = list.size() - 1; k >= 0; k--) {
                BlockPos blockPos3 = list.get(k);
                BlockState blockState5 = level.getBlockState(blockPos3);
                blockPos3 = blockPos3.relative(facing);
                map.remove(blockPos3);
                BlockState blockState8 = Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, facing);
                CompoundTag nbt = new CompoundTag();
                if (list1.get(k).getBlock() instanceof IMoveableEntityBlock block) {
                    nbt = block.clearData(level, blockPos3.relative(facing.getOpposite()));
                }
                if (level.getBlockEntity(blockPos3.relative(facing.getOpposite())) instanceof PropelPistonBlockEntity) {
                    int data = nbt.getInt("storedEnergyData");
                    nbt.putInt("storedEnergyData", data - 5 * (list.size() - 1));
                }
                level.setBlock(blockPos3, blockState8, 68);
                BlockEntity blockEntity = MovingPistonBlock.newMovingBlockEntity(blockPos3, blockState8, list1.get(k), facing, true, false);
                if (blockEntity instanceof IPistonMovingBlockEntityExtension entity) {
                    entity.anvilcraft$setData(nbt);
                }
                level.setBlockEntity(blockEntity);
                blockStates[i++] = blockState5;
            }

            BlockState blockState3 = Blocks.AIR.defaultBlockState();

            for (BlockPos blockPos4 : map.keySet()) {
                level.setBlock(blockPos4, blockState3, 82);
            }

            for (Map.Entry<BlockPos, BlockState> entry : map.entrySet()) {
                BlockPos blockPos5 = entry.getKey();
                BlockState blockState2 = entry.getValue();
                blockState2.updateIndirectNeighbourShapes(level, blockPos5, 2);
                blockState3.updateNeighbourShapes(level, blockPos5, 2);
                blockState3.updateIndirectNeighbourShapes(level, blockPos5, 2);
            }

            i = 0;

            for (int l = list2.size() - 1; l >= 0; l--) {
                BlockState blockState7 = blockStates[i++];
                BlockPos blockPos6 = list2.get(l);
                blockState7.updateIndirectNeighbourShapes(level, blockPos6, 2);
                level.updateNeighborsAt(blockPos6, blockState7.getBlock());
            }

            for (int i1 = list.size() - 1; i1 >= 0; i1--) {
                level.updateNeighborsAt(list.get(i1), blockStates[i++].getBlock());
            }

            return true;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker
    ) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }
}
