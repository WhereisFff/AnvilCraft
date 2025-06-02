package dev.dubhe.anvilcraft.block.entity.heatable;

import dev.dubhe.anvilcraft.api.heat.TempVariationManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HeatableBlockEntity extends BlockEntity {
    protected static final int MAX_DURATION = 1200 * 20;
    @Getter
    @Setter
    protected int duration = 0;

    protected HeatableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * 增加1秒
     */
    public void addDuration(int second) {
        this.addDurationInTick(second * 20);
    }

    public void addDurationInTick(int tick) {
        this.duration = Math.clamp(this.duration + tick, 0, MAX_DURATION);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("duration", this.duration);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.duration = tag.getInt("duration");
    }

    public static void tick(Level level, BlockPos pos) {
        TempVariationManager.addHeatableBlock(pos, level);
    }
}
