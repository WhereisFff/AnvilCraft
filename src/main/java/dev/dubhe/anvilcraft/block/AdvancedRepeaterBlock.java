package dev.dubhe.anvilcraft.block;

import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.entity.AdvancedRepeaterBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedRepeaterBlock extends DiodeBlock implements EntityBlock {
    public static final MapCodec<AdvancedRepeaterBlock> CODEC = simpleCodec(AdvancedRepeaterBlock::new);

    public AdvancedRepeaterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.FALSE)
        );
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getDelay(BlockState state) {
        return 1;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedRepeaterBlockEntity(pos, state);
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity instanceof AdvancedRepeaterBlockEntity repeater ? repeater.getOutputSignal() : 0;
    }

    @Override
    protected boolean canSurviveOn(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof AdvancedRepeaterBlockEntity repeater) {
            if (
                repeater.getStartMode() == 0 && this.getInputSignal(level, pos, state) > 0
                || repeater.getStartMode() == 1 && this.getInputSignal(level, pos, state) == 0
            ) {
                repeater.start();
            } else {
                level.scheduleTick(pos, this, 1);
            }
        }
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity instanceof AdvancedRepeaterBlockEntity repeater && repeater.isWaiting();
    }
}
