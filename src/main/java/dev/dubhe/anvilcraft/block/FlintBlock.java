package dev.dubhe.anvilcraft.block;

import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.ArrayList;
import java.util.List;

public class FlintBlock extends Block {
    public FlintBlock(Properties properties) {
        super(properties);
    }

    public static void ignite(LevelAccessor level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockPos below = pos.below();
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos north = pos.north();
        if (level.getBlockState(above).is(Blocks.IRON_BLOCK)
            || level.getBlockState(below).is(Blocks.IRON_BLOCK)
            || level.getBlockState(west).is(Blocks.IRON_BLOCK)
            || level.getBlockState(east).is(Blocks.IRON_BLOCK)
            || level.getBlockState(south).is(Blocks.IRON_BLOCK)
            || level.getBlockState(north).is(Blocks.IRON_BLOCK)) {
            List<BlockPos> blocks = new ArrayList<>();
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    for (int z = -1; z < 2; z++) {
                        BlockPos offset = pos.offset(x, y, z);
                        BlockState blockState = level.getBlockState(offset);
                        if (blockState.is(ModBlocks.OIL_CAULDRON)) {
                            OilCauldronBlock.ignite(level, offset, blockState);
                            return;
                        } else if (blockState.getBlock() instanceof CampfireBlock) {
                            if (!blockState.getValue(CampfireBlock.LIT)) {
                                level.setBlock(offset, blockState.setValue(CampfireBlock.LIT, true), 3);
                                return;
                            }
                        }
                        blocks.add(offset);
                    }
                }
            }

            List<BlockPos> newBlocks = blocks.stream()
                .filter(blockPos -> (Shapes.faceShapeOccludes(box(0, 0, 0, 16, 1, 16),
                    level.getBlockState(blockPos).getCollisionShape(level, blockPos))
                    && level.getBlockState(blockPos.above()).isAir())
                    || level.getBlockState(blockPos).is(ModBlocks.OIL_CAULDRON)
                    || (level.getBlockState(blockPos).getBlock() instanceof CampfireBlock))
                .toList();
            BlockPos blockPos = newBlocks.get(level.getRandom().nextIntBetweenInclusive(0, newBlocks.size() - 1));
            level.setBlock(blockPos.above(), BaseFireBlock.getState(level, blockPos), 3);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (movedByPiston) {
            ignite(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (movedByPiston) {
            ignite(level, pos);
        }
    }
}
