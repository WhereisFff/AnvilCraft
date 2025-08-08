package dev.dubhe.anvilcraft.event.anvil;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.event.anvil.AnvilEvent;
import dev.dubhe.anvilcraft.api.event.anvil.GiantAnvilFallOnLandEvent;
import dev.dubhe.anvilcraft.block.GunpowderBlock;
import dev.dubhe.anvilcraft.entity.AnimateAscendingBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class AnvilHitGunpowderBlockEventListener {
    @SubscribeEvent
    public static void onLand(AnvilEvent.OnLand event) {
        Level level = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState blockState = level.getBlockState(pos);
        final BlockPos hitPos = pos.below();
        final BlockState hitState = level.getBlockState(hitPos);
        if (hitState.getBlock() instanceof GunpowderBlock gunpowderBlock) {
            gunpowderBlock.onHit(level, hitPos);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            int distance = Math.round(event.getFallDistance());
            BlockPos above = pos;
            for (int i = 1; i < distance + 1; i++) {
                above = above.above();
                if (!level.getBlockState(above).isAir()) {
                    break;
                }
            }
            above = above.below();
            AnimateAscendingBlockEntity.animate(level, pos, blockState, above);
            level.setBlockAndUpdate(above, blockState);
        }
    }

    @SubscribeEvent
    public static void giantAnvilOnLand(GiantAnvilFallOnLandEvent event) {
        Level level = event.getLevel();
        final BlockPos pos = event.getPos();
        final BlockState blockState = level.getBlockState(pos);
        final BlockPos hitPos = pos.below();
        final BlockState hitState = level.getBlockState(hitPos);
        if (hitState.getBlock() instanceof GunpowderBlock gunpowderBlock) {
            gunpowderBlock.onHit(level, hitPos);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            int distance = Math.round(event.getFallDistance());
            BlockPos above = pos;
            for (int i = 1; i < distance + 1; i++) {
                above = above.above();
                if (!level.getBlockState(above).isAir()) {
                    break;
                }
            }
            above = above.below();
            AnimateAscendingBlockEntity.animate(level, pos, blockState, above);
            level.setBlockAndUpdate(above, blockState);
        }
    }
}
