package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.init.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WhiteHoleBlockEntity extends BlockEntity {
    public WhiteHoleBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WHITE_HOLE.get(), pos, blockState);
    }

    public static WhiteHoleBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new WhiteHoleBlockEntity(pos, blockState);
    }
}