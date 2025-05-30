package dev.dubhe.anvilcraft.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.block.HeaterBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlasmaJetsManager {
    private static final Map<Level, PlasmaJetsManager> INSTANCES = new HashMap<>();

    private final Level level;
    private final Set<BlockPos> plasmaJets = Collections.synchronizedSet(new HashSet<>());
    private final Multimap<BlockPos, TubeWallLayer> tubeWalls = Multimaps.synchronizedMultimap(HashMultimap.create());

    /**
     * 获取当前维度等离子喷流管理器实例
     */
    public static PlasmaJetsManager getInstance(Level level) {
        if (!INSTANCES.containsKey(level)) {
            INSTANCES.put(level, new PlasmaJetsManager(level));
        }
        return INSTANCES.get(level);
    }

    public PlasmaJetsManager(Level level) {
        this.level = level;
    }

    /**
     *
     */
    public static void trySpawnPlasmaJets(BlockPos pos, Level level) {
        PlasmaJetsManager manager = PlasmaJetsManager.getInstance(level);
        BlockState cauldron = level.getBlockState(pos.below());
        BlockState heater = level.getBlockState(pos.below().below());
        if (!cauldron.is(ModBlocks.FIRE_CAULDRON)
            || !heater.is(ModBlocks.HEATER)
            || heater.getValue(HeaterBlock.OVERLOAD)
        ) return;
        for (int i = 0; i < 8; i++) {
            if (!level.getBlockState(pos.above(i)).isAir()) return;
        }
        manager.plasmaJets.add(pos);
    }

    public void tick() {
        this.tryRaisePlasmaJets();
    }

    private void tryRaisePlasmaJets() {
        for (BlockPos pos : this.plasmaJets) {
            if (this.tubeWalls.get(pos).size() == 4) continue;
            if (this.level.getBlockState(pos.north()).isAir()
                || this.level.getBlockState(pos.south()).isAir()
                || this.level.getBlockState(pos.east()).isAir()
                || this.level.getBlockState(pos.west()).isAir()
            ) continue;
            this.tubeWalls.put(pos, TubeWallLayer.of(pos));
            this.level.removeBlock(pos, false);
            this.level.setBlock(pos, ModBlocks.PLASMA_JETS.getDefaultState(), 3);
        }
    }

    public record TubeWallLayer(Pair<BlockPos, BlockPos> first, Pair<BlockPos, BlockPos> second) {
        public static TubeWallLayer of(BlockPos center) {
            return new TubeWallLayer(new Pair<>(center.north(), center.south()), new Pair<>(center.east(), center.west()));
        }
    }
}
