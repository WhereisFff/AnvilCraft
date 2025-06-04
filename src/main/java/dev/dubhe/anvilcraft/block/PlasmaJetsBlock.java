package dev.dubhe.anvilcraft.block;

import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PlasmaJetsBlock extends Block implements EntityBlock {
    public PlasmaJetsBlock(Properties properties) {
        super(properties);
    }

    public static boolean trySpawn(BlockPos pos, Level level) {
        BlockState cauldron = level.getBlockState(pos.below());
        BlockState heater = level.getBlockState(pos.below().below());
        if (!cauldron.is(ModBlocks.FIRE_CAULDRON)
            || !heater.is(ModBlocks.HEATER)
            || heater.getValue(HeaterBlock.OVERLOAD)
        ) return false;
        for (int i = 0; i < 8; i++) {
            if (!level.getBlockState(pos.above(i)).isAir()) return false;
        }
        level.setBlock(pos, ModBlocks.PLASMA_JETS.getDefaultState(), 3);
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(0, 0, 0, 16, 16, 16);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.igniteForSeconds(15.0f);
            if (entity.hurt(entity.damageSources().inFire(), 32.0f)) {
                entity.playSound(SoundEvents.GENERIC_BURN, 0.4f, 2.0f + RandomSource.create().nextFloat() * 0.4f);
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
