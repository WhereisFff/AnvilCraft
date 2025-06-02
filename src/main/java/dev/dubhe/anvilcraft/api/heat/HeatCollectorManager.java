package dev.dubhe.anvilcraft.api.heat;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.util.AabbUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeatCollectorManager {
    private static final Map<Level, HeatCollectorManager> INSTANCES = new HashMap<>();
    public static final List<HeatSourceEntry> SOURCE_ENTRIES = new ArrayList<>();

    private final Set<BlockPos> heatCollectors = Collections.synchronizedSet(new HashSet<>());

    public static void clear() {
        INSTANCES.clear();
    }

    /**
     * 获取当前维度的ThermoManager
     */
    public static HeatCollectorManager getInstance(Level level) {
        synchronized (INSTANCES) {
            if (INSTANCES.get(level) == null) {
                HeatCollectorManager.INSTANCES.put(level, new HeatCollectorManager());
            }
            return HeatCollectorManager.INSTANCES.get(level);
        }
    }

    public static void registerEntry(HeatSourceEntry entry) {
        SOURCE_ENTRIES.add(entry);
    }

    public static boolean canPlaceCollector(BlockPos pos, Level level) {
        HeatCollectorManager manager = getInstance(level);
        AABB validRange = AabbUtil.create(
            pos.above(4).north(4).east(4),
            pos.below(4).south(4).west(4));
        for (BlockPos checkedPos : manager.heatCollectors) {
            if (validRange.contains(checkedPos.getCenter())) {
                return false;
            }
        }
        manager.heatCollectors.add(pos);
        return true;
    }

    static {
        registerEntry(HeatSourceEntry.forever(4, ModBlocks.HEATED_NETHERITE.get()));
        registerEntry(HeatSourceEntry.forever(4, ModBlocks.HEATED_TUNGSTEN.get()));
        registerEntry(HeatSourceEntry.forever(16, ModBlocks.REDHOT_NETHERITE.get()));
        registerEntry(HeatSourceEntry.forever(16, ModBlocks.REDHOT_TUNGSTEN.get()));
        registerEntry(HeatSourceEntry.forever(64, ModBlocks.GLOWING_NETHERITE.get()));
        registerEntry(HeatSourceEntry.forever(64, ModBlocks.GLOWING_TUNGSTEN.get()));
        registerEntry(HeatSourceEntry.forever(256, ModBlocks.INCANDESCENT_NETHERITE.get()));
        registerEntry(HeatSourceEntry.forever(256, ModBlocks.INCANDESCENT_TUNGSTEN.get()));

        registerEntry(HeatSourceEntry.simple(2, Blocks.MAGMA_BLOCK, Blocks.NETHERRACK));
        registerEntry(HeatSourceEntry.predicate(
            4,
            CampfireBlock::isLitCampfire,
            t -> t.setValue(CampfireBlock.LIT, false)
        ));
        registerEntry(HeatSourceEntry.predicate(
            4,
            state -> state.getFluidState().isSourceOfType(Fluids.LAVA),
            it -> Blocks.OBSIDIAN.defaultBlockState()
        ));
        registerEntry(HeatSourceEntry.simple(4, Blocks.LAVA_CAULDRON, ModBlocks.OBSIDIAN_CAULDRON.get()));

        registerEntry(HeatSourceEntry.forever(2, ModBlocks.URANIUM_BLOCK.get()));
        registerEntry(HeatSourceEntry.forever(4, ModBlocks.EMBER_METAL_BLOCK.get()));
    }

    HeatCollectorManager() {
    }
}
