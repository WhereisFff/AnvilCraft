package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.block.NeutronIrradiatorBlock;
import dev.dubhe.anvilcraft.entity.LevitatingBlockEntity;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dubhe.anvilcraft.util.GravityManager.GravitySourceManager.getGravityVector;

public class GravityManager {
    // 维度重力映射表
    private static final Map<ResourceKey<Level>, Double> DIMENSION_GRAVITY_MAP = new HashMap<>();

    static {
        GravitySourceManager.registerSource(NeutronIrradiatorBlock.class, 7, 10.0);
        // 在这里注册更多重力源，strength -> 该重力源所在方块任意表面中心的重力是主世界重力的 strength 倍

        registerDimensionGravity(Level.END, 0.5);
        // 在这里注册更多维度重力，gravity -> 该维度重力是主世界重力的 gravity 倍
    }

    // 设置不同实体的重力系数
    private static double getGravityFactor(Entity entity) {
        return switch (entity) {
            case FallingBlockEntity fallingBlockEntity -> -0.04; // 下落方块
            case LivingEntity livingEntity -> -0.08; // 生物
            case Projectile projectile -> -0.05; // 投掷物
            case ItemEntity itemEntity -> -0.02; // 掉落物
            default -> -0.08;
        };
    }

    // 应用重力并处理特殊实体
    public static Vec3 applyGravity(Entity entity, Vec3 movement) {
        Level level = entity.level();
        BlockPos blockPos = entity.blockPosition();
        Vec3 entityPos = entity.position();
        Vec3 gravityVector = getGravityVector(level, blockPos, entityPos);

        // 维度重力处理，恒定向下重力
        if (getDimensionGravity(level) != 1.0) {
            gravityVector = gravityVector.add(0, getGravityFactor(entity) * (getDimensionGravity(level) - 1.0), 0);
        }

        // 物品实体处理，负物质反转重力，漂浮粉略有失重感
        if (entity instanceof ItemEntity itemEntity) {
            var item = itemEntity.getItem();
            boolean isNegativeMatter = item.is(ModItems.NEGATIVE_MATTER_NUGGET.get()) || item.is(ModItems.NEGATIVE_MATTER.get()) || item.is(
                ModBlocks.NEGATIVE_MATTER_BLOCK.get().asItem());

            if (isNegativeMatter) {
                boolean isLevitationPowder = item.is(ModItems.LEVITATION_POWDER.get()) || item.is(ModBlocks.LEVITATION_POWDER_BLOCK.get()
                    .asItem());

                return movement.subtract(isLevitationPowder ? gravityVector.scale(0.005) : gravityVector);
            }
        }

        // 漂浮粉块下落方块实体：反转重力
        if (entity instanceof LevitatingBlockEntity levitatingBlock && levitatingBlock.getBlockState()
            .is(ModBlocks.LEVITATION_POWDER_BLOCK.get())) {
            return movement.subtract(gravityVector);
        }

        // 默认情况：正常加重力
        return movement.add(gravityVector);
    }

    // 维度重力管理器
    public static void registerDimensionGravity(ResourceKey<Level> dimension, double gravity) {
        DIMENSION_GRAVITY_MAP.put(dimension, gravity);
    }

    public static double getDimensionGravity(Level level) {
        return DIMENSION_GRAVITY_MAP.getOrDefault(level.dimension(), 1.0);
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
                if (rSqr > 0.70710678) {
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