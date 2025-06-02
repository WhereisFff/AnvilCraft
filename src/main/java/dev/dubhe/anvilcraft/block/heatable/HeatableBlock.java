package dev.dubhe.anvilcraft.block.heatable;

import dev.dubhe.anvilcraft.api.heat.TempVariationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HeatableBlock extends Block implements EntityBlock {
    protected HeatableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        TempVariationManager.addHeatableBlock(pos, level);
    }
}
