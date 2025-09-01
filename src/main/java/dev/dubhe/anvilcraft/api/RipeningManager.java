package dev.dubhe.anvilcraft.api;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.InductionLightBlock;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RipeningManager {
    private static final Map<Level, RipeningManager> INSTANCES = new HashMap<>();

    private final Level level;
    private final Set<BlockPos> lightBlocks = Collections.synchronizedSet(new HashSet<>());

    /**
     * 获取当前维度催熟实例
     */
    public static RipeningManager getInstance(Level level) {
        if (!INSTANCES.containsKey(level)) {
            INSTANCES.put(level, new RipeningManager(level));
        }
        return INSTANCES.get(level);
    }

    public RipeningManager(Level level) {
        this.level = level;
    }

    public static void tickAll() {
        INSTANCES.values().forEach(RipeningManager::tick);
    }

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

    private void tick() {
        if (level.getServer() == null || lightBlocks.isEmpty()) return;
        long curTime = level.getGameTime();
        if (lastTickRipen > curTime) { // time set xxx may change the gameTime.
            lastTickRipen = curTime;
            return;
        }
        if (curTime - lastTickRipen < AnvilCraft.CONFIG.inductionLightBlockRipeningCooldown) {
            return;
        }
        lastTickRipen = curTime;

        lightBlocks.removeIf(pos -> {
            BlockState lightBlockState = level.getBlockState(pos);
            return !(
                lightBlockState.getBlock() instanceof InductionLightBlock
                && InductionLightBlock.isLit(lightBlockState)
                && InductionLightBlock.canCropGrow(lightBlockState)
            );
        });

        HashSet<BlockPos> ripenedBlocks = new HashSet<>();
        for (BlockPos pos : lightBlocks) {
            doRipen(pos, ripenedBlocks);
        }
    }

    /**
     *
     */
    public static void addLightBlock(BlockPos pos, Level level) {
        RipeningManager inst = RipeningManager.getInstance(level);
        inst.lightBlocks.add(pos);
    }
}
