package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.block.NeutronIrradiatorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GravityManager {
    // 重力设置
    public static final double DEFAULT_GRAVITY = 1.0;
    public static final double NEUTRON_IRRADIATOR_GRAVITY = 3.0;
    public static final double MUN_GRAVITY = 0.1663;

    public static double getGravity(Entity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        // 检查应该用什么重力
        if (isInNeutronIrradiatorRange(level, pos)) {
            return NEUTRON_IRRADIATOR_GRAVITY;
        }
        if (isOnMun(level)) {
            return MUN_GRAVITY;
        }
        return DEFAULT_GRAVITY;
    }

    public static boolean isInNeutronIrradiatorRange(Level level, BlockPos pos) {
        for (int i = 0; i <= 7; i++) {
            BlockPos belowPos = pos.below(i);
            BlockState blockstate = level.getBlockState(belowPos);

            if (blockstate.getBlock() instanceof NeutronIrradiatorBlock) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOnMun(Level level) {
        return false;
    }
}