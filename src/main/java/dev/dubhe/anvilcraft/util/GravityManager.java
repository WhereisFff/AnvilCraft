package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.block.BlackHoleBlock;
import dev.dubhe.anvilcraft.entity.LevitatingBlockEntity;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "anvilcraft", bus = EventBusSubscriber.Bus.GAME)
public class GravityManager {

    // 维度 -> 区块索引 -> 重力源列表
    private static final Map<ResourceKey<Level>, Map<Long, List<GravitySource>>> GRAVITY_CACHE = new ConcurrentHashMap<>();

    // 维度重力倍率表
    private static final Map<ResourceKey<Level>, Double> DIMENSION_GRAVITY_MAP = new HashMap<>();

    static {
        GravitySourceManager.registerSourceType(BlackHoleBlock.class, 7, 10.0);
        // 在这里注册更多重力源，strength -> 距离该重力源 1 格处的重力是主世界重力的 strength 倍

        registerDimensionGravity(Level.END, 0.1653061224489796);
        // 在这里注册更多维度重力，gravity -> 该维度重力是主世界重力的 gravity 倍
    }

    // 区块加载事件，搜索BlockEntity添加重力源
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide && event.getChunk() instanceof LevelChunk chunk) {
            for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                BlockState state = chunk.getBlockState(pos);
                GravitySourceType type = GravitySourceManager.getType(state.getBlock());
                if (type != null) {
                    GravitySourceManager.addSource(level, pos, type);
                }
            }
        }
    }

    // 区块卸载事件，移除记录的重力源
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide) {
            long chunkKey = event.getChunk().getPos().toLong();
            Map<Long, List<GravitySource>> dimCache = GRAVITY_CACHE.get(level.dimension());
            if (dimCache != null) {
                dimCache.remove(chunkKey);
            }
        }
    }

    // 世界卸载事件，清除所有缓存
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide) {
            GRAVITY_CACHE.remove(level.dimension());
        }
    }

    // 方块放置事件，添加重力源
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide) {
            GravitySourceType type = GravitySourceManager.getType(event.getState().getBlock());
            if (type != null) {
                GravitySourceManager.addSource(level, event.getPos(), type);
            }
        }
    }

    // 方块破坏事件，移除重力源
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide) {
            GravitySourceType type = GravitySourceManager.getType(event.getState().getBlock());
            if (type != null) {
                GravitySourceManager.removeSource(level, event.getPos());
            }
        }
    }

    // 特殊物质定义不同引力类型
    public enum GravityType {
        NORMAL, // 正常实体正常重力
        ANTI_GRAVITY, // 负物质反转重力
        MICRO_ANTI_GRAVITY // 漂浮粉略有失重感
    }

    public static GravityType getGravityType(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            var item = itemEntity.getItem();

            // 判断负物质
            boolean isNegativeMatter = item.is(ModItems.NEGATIVE_MATTER_NUGGET.get()) || item.is(ModItems.NEGATIVE_MATTER.get()) || item.is(
                ModBlocks.NEGATIVE_MATTER_BLOCK.get().asItem());
            if (isNegativeMatter) return GravityType.ANTI_GRAVITY;

            // 判断漂浮粉
            boolean isLevitationPowder = item.is(ModItems.LEVITATION_POWDER.get()) || item.is(ModBlocks.LEVITATION_POWDER_BLOCK.get()
                .asItem());
            if (isLevitationPowder) return GravityType.MICRO_ANTI_GRAVITY;
        }

        // 判断漂浮粉方块实体
        if (entity instanceof LevitatingBlockEntity levitatingBlock && levitatingBlock.getBlockState()
            .is(ModBlocks.LEVITATION_POWDER_BLOCK.get())) {
            return GravityType.ANTI_GRAVITY;
        }

        return GravityType.NORMAL;
    }

    // 处理特殊实体，最终得到重力向量
    public static Vec3 getGravityVector(Entity entity) {
        // 获取计算的引力
        Vec3 gravityVector = GravitySourceManager.calculateGravityVector(entity);

        // 如果没有受到引力，直接返回零
        if (gravityVector.equals(Vec3.ZERO)) {
            return Vec3.ZERO;
        }

        // 获取实体引力类型
        GravityType type = getGravityType(entity);

        // 根据类型修正向量方向/大小
        return switch (type) {
            case ANTI_GRAVITY -> gravityVector.reverse();
            case MICRO_ANTI_GRAVITY -> gravityVector.scale(-0.005);
            default -> gravityVector;
        };
    }

    // 应用维度重力
    public static void registerDimensionGravity(ResourceKey<Level> dimension, double gravity) {
        DIMENSION_GRAVITY_MAP.put(dimension, gravity);
    }

    public static double getDimensionGravity(Level level) {
        return DIMENSION_GRAVITY_MAP.getOrDefault(level.dimension(), 1.0);
    }

    // 重力源定义与存储
    public static class GravitySourceType {
        final double strength;
        final int radius;
        final double radiusSqr;

        public GravitySourceType(double strength, int radius) {
            this.strength = strength;
            this.radius = radius;
            this.radiusSqr = radius * radius;
        }
    }

    public static class GravitySource {
        public final BlockPos pos;
        public final GravitySourceType type;

        public GravitySource(BlockPos pos, GravitySourceType type) {
            this.pos = pos;
            this.type = type;
        }
    }

    // 重力源管理器
    public static class GravitySourceManager {
        private static final Map<Class<? extends Block>, GravitySourceType> REGISTRY = new HashMap<>();

        public static void registerSourceType(Class<? extends Block> blockClass, int radius, double strength) {
            REGISTRY.put(blockClass, new GravitySourceType(strength, radius));
        }

        public static GravitySourceType getType(Block block) {
            return REGISTRY.get(block.getClass());
        }

        public static void addSource(Level level, BlockPos pos, GravitySourceType type) {
            long chunkKey = ChunkPos.asLong(pos);
            List<GravitySource> list = GRAVITY_CACHE.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, k -> new ArrayList<>());

            // 检查列表中是否已经有该坐标的重力源
            boolean alreadyExists = false;
            for (GravitySource s : list) {
                if (s.pos.equals(pos)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                list.add(new GravitySource(pos, type));
            }
        }

        public static void removeSource(Level level, BlockPos pos) {
            long chunkKey = ChunkPos.asLong(pos);
            Map<Long, List<GravitySource>> dimCache = GRAVITY_CACHE.get(level.dimension());
            if (dimCache != null) {
                List<GravitySource> sources = dimCache.get(chunkKey);
                if (sources != null) {
                    sources.removeIf(s -> s.pos.equals(pos));
                    if (sources.isEmpty()) {
                        dimCache.remove(chunkKey);
                    }
                }
            }
        }

        private static double getEntityG(Entity entity) {
            return switch (entity) {
                case LivingEntity livingEntity -> 0.08;
                case Projectile projectile -> 0.05;
                default -> 0.04;
            };
        }

        public static Vec3 calculateGravityVector(Entity e) {
            var cache = GRAVITY_CACHE.get(e.level().dimension());
            if (cache == null) return Vec3.ZERO;

            Vec3 p = e.position();
            double G = getEntityG(e), fx = 0, fy = 0, fz = 0;
            int cx = ((int) Math.floor(p.x)) >> 4, cz = ((int) Math.floor(p.z)) >> 4;

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    var list = cache.get(ChunkPos.asLong(cx + x, cz + z));
                    if (list == null) continue;
                    for (var s : list) {
                        double dx = s.pos.getX() + 0.5 - p.x, dy = s.pos.getY() + 0.5 - p.y, dz = s.pos.getZ() + 0.5 - p.z;
                        double rSq = dx * dx + dy * dy + dz * dz;

                        if (rSq <= s.type.radiusSqr) {
                            double f = (G * s.type.strength) / (Math.max(rSq, 1.0) * Math.sqrt(rSq));
                            fx += dx * f;
                            fy += dy * f;
                            fz += dz * f;
                        }
                    }
                }
            }
            return new Vec3(fx, fy, fz);
        }
    }
}