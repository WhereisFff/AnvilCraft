package dev.dubhe.anvilcraft.block.piston;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;

@SuppressWarnings("unused")
public interface IMoveableEntityBlock extends EntityBlock {
    default CompoundTag clearData(Level level, BlockPos pos) {
        return new CompoundTag();
    }

    default void setData(Level level, BlockPos pos, CompoundTag nbt) {
    }
}
