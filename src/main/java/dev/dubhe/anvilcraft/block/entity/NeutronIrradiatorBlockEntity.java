package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.block.NeutronIrradiatorBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NeutronIrradiatorBlockEntity extends BlockEntity {

    public NeutronIrradiatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.NEUTRON_IRRADIATOR.get(), pos, blockState);
    }

    public static NeutronIrradiatorBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new NeutronIrradiatorBlockEntity(pos, blockState);
    }

    public static boolean isInNeutronIrradiatorRange(Level level, BlockPos pos) {
        for (int dy = 1; dy <= 7; dy++) {
            if (level.getBlockState(pos.below(dy)).getBlock() instanceof NeutronIrradiatorBlock) {
                return true;
            }
        }
        return false;
    }
}