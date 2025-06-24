package dev.dubhe.anvilcraft.block.heatable;

import dev.dubhe.anvilcraft.api.heat.HeaterManager;
import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import dev.dubhe.anvilcraft.block.piston.IMoveableEntityBlock;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HeatableBlock extends Block implements IMoveableEntityBlock {
    protected HeatableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        HeaterManager.addHeatableBlock(pos, level);
    }

    protected abstract boolean hasBlockEntity();

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (this.hasBlockEntity()) {
            return (level1, pos, state1, blockEntity) -> HeatableBlockEntity.tick(level1, pos);
        }
        return null;
    }

    @Override
    public CompoundTag clearData(Level level, BlockPos pos) {
        CompoundTag nbt = new CompoundTag();
        Util.castSafely(level.getBlockEntity(pos), HeatableBlockEntity.class)
            .ifPresent(entity -> nbt.putInt("duration", entity.getDuration()));
        return nbt;
    }

    @Override
    public void setData(@NotNull Level level, @NotNull BlockPos pos, @NotNull CompoundTag nbt) {
        Util.castSafely(level.getBlockEntity(pos), HeatableBlockEntity.class)
            .ifPresent(entity -> entity.setDuration(nbt.getInt("duration")));
    }
}
