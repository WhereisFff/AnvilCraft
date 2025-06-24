package dev.dubhe.anvilcraft.block.heatable;

import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NormalBlock extends HeatableBlock {
    public NormalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean hasBlockEntity() {
        return false;
    }

    @Override
    public @Nullable HeatableBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
