package dev.dubhe.anvilcraft.api;

import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RipeningManager {
    private static final Map<Level, RipeningManager> INSTANCES = new HashMap<>();

    private final Level level;

    /**
     * 获取或新建一个当前维度催熟实例。
     */
    public static RipeningManager from(Level level) {
        return INSTANCES.computeIfAbsent(level, RipeningManager::new);
    }

    public RipeningManager(Level level) {
        this.level = level;
    }

    /**
     * @param pos 灯的位置
     * @param ripened 在本轮催熟中，已经被催熟过的位置。
     */
    private void doRipen(@NotNull BlockPos pos, @NotNull HashSet<BlockPos> ripened) {
        int rangeSize = AnvilCraft.CONFIG.inductionLightBlockRipeningRange;
        for (int dx = -rangeSize / 2; dx <= rangeSize / 2; dx++) {
            for (int dy = -rangeSize / 2; dy <= rangeSize / 2; dy++) {
                for (int dz = -rangeSize / 2; dz <= rangeSize / 2; dz++) {
                    BlockPos pos1 = pos.offset(dx, dy, dz);
                    if (ripened.contains(pos1)) continue;
                    BlockState state = level.getBlockState(pos1);
                    if (state.getBlock() instanceof BonemealableBlock growable
                        && !(growable instanceof GrassBlock)
                        && !(growable instanceof NyliumBlock)
                        && growable.isValidBonemealTarget(level, pos1, state)
                        && level.getBrightness(LightLayer.BLOCK, pos1) >= 10
                    ) {
                        growable.performBonemeal((ServerLevel) level, level.getRandom(), pos1, state);
                        level.addParticle(
                            ParticleTypes.HAPPY_VILLAGER,
                            pos1.getX() + 0.5,
                            pos1.getY() + 0.5,
                            pos1.getZ() + 0.5,
                            0.0,
                            0.0,
                            0.0
                        );
                        ripened.add(pos1);
                    }
                    if (state.is(Blocks.SUGAR_CANE)
                        && level.getBlockState(pos1.above()).is(Blocks.AIR)
                    ) {
                        level.setBlock(
                            pos1.above(),
                            Blocks.SUGAR_CANE.defaultBlockState(),
                            Block.UPDATE_ALL_IMMEDIATE
                        );
                    }
                    if (state.is(Blocks.CACTUS) && level.getBlockState(pos1.above()).is(Blocks.AIR)) {
                        level.setBlock(pos1.above(), Blocks.CACTUS.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                    }
                    if (state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE) != NetherWartBlock.MAX_AGE) {
                        level.setBlock(
                            pos1,
                            Blocks.NETHER_WART.defaultBlockState().setValue(
                                NetherWartBlock.AGE,
                                state.getValue(NetherWartBlock.AGE) + 1
                            ),
                            Block.UPDATE_ALL_IMMEDIATE
                        );
                    }
                }
            }
        }
    }


    private long lastTickRipen = -1;


    /**
     * 如果当前时间距离上次催熟不小于催熟冷却则清空重复催熟过滤器 ripened 并重新计时，返回 true
     * 如果时间差在 (0, 冷却) 之间则返回 false 无事发生
     * 如果为 0 则返回 true 无事发生（因为意味着其他灯已经调用过这个函数了）
     * 如果为负数说明有时间旅行（time set xxx），重置上次催熟时间。
     * @return if already cooldown for ripen
     */
    private boolean isRipenReady() {
        if (level.getServer() == null) return false;
        long curTime = level.getGameTime();
        long ticksBeforeLastRipen = curTime - lastTickRipen;
        if (ticksBeforeLastRipen == 0) return true; // another LightBlock is Ripened at this tick.
        if (ticksBeforeLastRipen < 0) { // time set xxx may change the gameTime.
            lastTickRipen = curTime - 1;
            return false;
        }
        if (ticksBeforeLastRipen >= AnvilCraft.CONFIG.inductionLightBlockRipeningCooldown) {
            lastTickRipen = curTime;
            ripened.clear();
            return true;
        }
        return false;
    }

    private final HashSet<BlockPos> ripened = new HashSet<>();

    public void doRipen(BlockPos blockPos) {
        if (isRipenReady()) doRipen(blockPos, ripened);
    }
}
