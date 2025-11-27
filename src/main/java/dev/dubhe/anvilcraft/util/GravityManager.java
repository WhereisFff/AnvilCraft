package dev.dubhe.anvilcraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

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
        } if (isOnMun(level)) {
            return MUN_GRAVITY;
        } return DEFAULT_GRAVITY;
    }

    private static boolean isInNeutronIrradiatorRange(Level level, BlockPos pos) {
        // 引用中子辐照器的检查方法
        try {
            Class<?> neutronIrradiatorClass = Class.forName("dev.dubhe.anvilcraft.block.entity.NeutronIrradiatorBlockEntity");
            java.lang.reflect.Method isInIrradiatorRangeMethod = neutronIrradiatorClass.getMethod(
                "isInIrradiatorRange", Level.class, BlockPos.class);
            return (boolean) isInIrradiatorRangeMethod.invoke(null, level, pos);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isOnMun(Level level) {
        return false;
    }
}