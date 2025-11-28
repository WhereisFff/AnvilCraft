package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.block.NeutronIrradiatorBlock;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static dev.dubhe.anvilcraft.util.GravityManager.GravitySourceManager.getGravityVector;

public class GravityManager {
    // 重力设置，默认重力1.0
    public static final double NEUTRON_IRRADIATOR_GRAVITY = 10.0;

    static {
        GravitySourceManager.registerSource(NeutronIrradiatorBlock.class, 7, NEUTRON_IRRADIATOR_GRAVITY);
        // 在这里注册更多重力源
    }

    private static boolean isNegativeMatterItem(ItemEntity itemEntity) {
        return itemEntity.getItem().is(ModItems.NEGATIVE_MATTER_NUGGET.get()) || itemEntity.getItem()
            .is(ModItems.NEGATIVE_MATTER.get()) || itemEntity.getItem().is(ModBlocks.NEGATIVE_MATTER_BLOCK.get().asItem());
    }
    private static boolean isLevitationPowder(ItemEntity itemEntity) {
        return itemEntity.getItem().is(ModItems.LEVITATION_POWDER.get()) || itemEntity.getItem()
            .is(ModBlocks.LEVITATION_POWDER_BLOCK.get().asItem());
    }
    // 在这里添加反重力物质

    // 根据移动方向和重力源方向调整速度
    public static Vec3 applyGravity(Entity entity, Vec3 movement) {
        Level level = entity.level();
        Vec3 entityPos = entity.position();
        BlockPos blockPos = entity.blockPosition();
        // 反重力物质处理
        if (entity instanceof ItemEntity itemEntity) {
            if (isNegativeMatterItem(itemEntity)) { // 负物质：反转重力
                Vec3 gravityVector = getGravityVector(entity.level(), entity.blockPosition(), entity.position());
                if (isLevitationPowder(itemEntity)) { // 漂浮粉：轻微失重感
                    return movement.subtract(gravityVector.scale(0.005));
                }
                return movement.subtract(gravityVector);
            }
        }
        // 获取所有重力影响并应用到移动向量
        Vec3 gravityForce = getGravityVector(level, blockPos, entityPos);
        return movement.add(gravityForce);
    }

    // 重力源类型定义
    public static class GravitySourceType {
        private final Class<? extends Block> blockClass;
        private final int radius;
        private final double strength;
        public GravitySourceType(Class<? extends Block> blockClass, int radius, double strength) {
            this.blockClass = blockClass;
            this.radius = radius;
            this.strength = strength;
        }
        public Class<? extends Block> getBlockClass() {
            return blockClass;
        }
        public int getRadius() {
            return radius;
        }
        public double getStrength() {
            return strength;
        }
    }

    // 用于存储重力源信息的内部类
    public static class GravitySource {
        public final BlockPos pos;
        public final double strength;
        public GravitySource(BlockPos pos, double strength) {
            this.pos = pos;
            this.strength = strength;
        }
    }

    // 重力源管理器
    public static class GravitySourceManager {
        private static final List<GravitySourceType> REGISTERED_SOURCES = new ArrayList<>();
        public static void registerSource(Class<? extends Block> blockClass, int radius, double strength) {
            REGISTERED_SOURCES.add(new GravitySourceType(blockClass, radius, strength));
        }
        // 计算实体所受的总重力（返回向量）
        public static Vec3 getGravityVector(Level level, BlockPos pos, Vec3 entityPos) {
            List<GravitySource> sources = new ArrayList<>();
            for (GravitySourceType type : REGISTERED_SOURCES) { // 遍历所有注册的重力源
                findGravitySourcesInRange(level, pos, type, sources);
            }
            if (sources.isEmpty()) { // 如果没有任何重力源，返回默认重力
                return new Vec3(0, 0, 0);
            }
            return gravityVector(sources, entityPos); // 否则计算合力
        }
        // 搜索重力源
        private static void findGravitySourcesInRange(Level level, BlockPos center, GravitySourceType type, List<GravitySource> sources) {
            int radius = type.getRadius(); // 读取重力源半径
            int rSqr = radius * radius;
            for (int x = -radius; x <= radius; x++) { // 遍历所有方块
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x * x + y * y + z * z > rSqr) continue; // 球形
                        BlockPos checkPos = center.offset(x, y, z);
                        BlockState state = level.getBlockState(checkPos);
                        if (type.getBlockClass().isInstance(state.getBlock())) {
                            sources.add(new GravitySource(checkPos, type.getStrength()));
                        }
                    }
                }
            }
        }
        // 计算来自所有重力源的合力
        public static Vec3 gravityVector(List<GravitySource> sources, Vec3 entityPosition) {
            double fx = 0, fy = 0, fz = 0;
            for (GravitySource source : sources) { // 遍历重力源累加引力
                Vec3 direction = Vec3.atCenterOf(source.pos).subtract(entityPosition); // 每个重力源计算距离向量
                double rSqr = direction.lengthSqr();
                if (rSqr > 0.5) {
                    double r = Math.sqrt(rSqr); // 万有引力公式计算
                    double f = 0.0224964424376324 * source.strength / (rSqr); // 测量得 G 在0.0224964424376323和0.0224964424376324之间
                    fx += direction.x / r * f; // 累加各分量力返回总向量
                    fy += direction.y / r * f;
                    fz += direction.z / r * f;
                }
            }
            return new Vec3(fx, fy, fz);
        }
    }
}