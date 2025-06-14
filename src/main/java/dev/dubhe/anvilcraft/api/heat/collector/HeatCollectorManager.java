package dev.dubhe.anvilcraft.api.heat.collector;

import dev.dubhe.anvilcraft.block.entity.HeatCollectorBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.util.Util;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.dubhe.anvilcraft.api.power.PowerGrid.GRID_TICK;

public class HeatCollectorManager {
    private static final Map<Level, HeatCollectorManager> INSTANCES = new HashMap<>();
    private static final List<HeatSourceEntry> SOURCE_ENTRIES = new ArrayList<>();

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

        registerEntry(HeatSourceEntry.predicateAlways(
            2,
            state -> state.is(ModBlockTags.STORAGE_BLOCKS_URANIUM),
            ModBlocks.URANIUM_BLOCK.get()
        ));
        registerEntry(HeatSourceEntry.forever(4, ModBlocks.EMBER_METAL_BLOCK.get()));
        registerEntry(HeatSourceEntry.predicateAlways(
            8,
            state -> state.is(ModBlockTags.STORAGE_BLOCKS_PLUTONIUM),
            ModBlocks.PLUTONIUM_BLOCK.get()
        ));
    }

    private final Level level;
    private final Set<BlockPos> heatCollectors = Collections.synchronizedSet(new HashSet<>());

    public static void clear() {
        INSTANCES.clear();
    }

    /**
     * 获取当前维度的HeatCollectorManager
     */
    public static HeatCollectorManager getInstance(Level level) {
        synchronized (INSTANCES) {
            if (INSTANCES.get(level) == null) {
                HeatCollectorManager.INSTANCES.put(level, new HeatCollectorManager(level));
            }
            return HeatCollectorManager.INSTANCES.get(level);
        }
    }

    public static void registerEntry(HeatSourceEntry entry) {
        SOURCE_ENTRIES.add(entry);
    }

    public static Optional<HeatSourceEntry> getEntry(BlockState state) {
        for (HeatSourceEntry entry : HeatCollectorManager.SOURCE_ENTRIES) {
            if (entry.accepts(state) > 0) return Optional.of(entry);
        }
        return Optional.empty();
    }

    public static void addHeatCollector(BlockPos pos, Level level) {
        getInstance(level).heatCollectors.add(pos);
    }

    public static void removeHeatCollector(BlockPos pos, Level level) {
        getInstance(level).heatCollectors.remove(pos);
    }

    public static boolean canPlaceCollector(BlockPos pos, Level level) {
        HeatCollectorManager manager = getInstance(level);
        AABB validRange = AABB.ofSize(pos.getCenter(), 9, 9, 9);
        for (BlockPos checkedPos : manager.heatCollectors) {
            if (validRange.contains(checkedPos.getCenter())) {
                return false;
            }
        }
        manager.heatCollectors.add(pos);
        return true;
    }

    HeatCollectorManager(Level level) {
        this.level = level;
    }

    public static void tickAll() {
        INSTANCES.values().forEach(HeatCollectorManager::tick);
    }

    private void tick() {
        if (this.level.getGameTime() % GRID_TICK != 0) return;
        List<HeatCollectorBlockEntity> collectors = this.getCollectorsFromNWToSE();
        Map<Entry, Double2ObjectMap<HeatCollectorBlockEntity>> heatSources = new HashMap<>();
        for (HeatCollectorBlockEntity collector : collectors) {
            for (BlockPos pos : collector.getCollectableSourcePoses()) {
                BlockState state = this.level.getBlockState(pos);
                getEntry(state)
                    .ifPresent(entry -> heatSources
                        .computeIfAbsent(
                            new Entry(pos, state, entry), it -> new Double2ObjectAVLTreeMap<>())
                        .put(
                            Vector3i.distance(
                                pos.getX(), pos.getY(), pos.getZ(),
                                collector.getPos().getX(), collector.getPos().getY(), collector.getPos().getZ()
                            ), collector)
                    );
            }
        }
        for (Entry entry : heatSources.keySet()) {
            int heat = entry.accepts();
            for (HeatCollectorBlockEntity entity : heatSources.get(entry).values()) {
                heat = entity.inputtingHeat(heat);
                if (heat == 0) break;
            }
            if (this.level.getGameTime() % entry.entry().timeToTransform() == 0) {
                this.level.setBlockAndUpdate(entry.pos(), entry.transform());
            }
        }
    }

    /**
     * 获取集热器的List集合(以从西北到东南排序)
     */
    private List<HeatCollectorBlockEntity> getCollectorsFromNWToSE() {
        List<HeatCollectorBlockEntity> collectors = new ArrayList<>();
        for (Iterator<BlockPos> iterator = this.heatCollectors.iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            Util.castSafely(this.level.getBlockEntity(pos), HeatCollectorBlockEntity.class)
                .ifPresentOrElse(collectors::add, iterator::remove);
        }
        collectors.sort(Comparator.comparing(BlockEntity::getBlockPos));
        return collectors;
    }

    private record Entry(BlockPos pos, BlockState state, HeatSourceEntry entry) {
        public int accepts() {
            return this.entry.accepts(this.state);
        }

        public BlockState transform() {
            return this.entry.transform(this.state);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof Entry entry1 && this.pos.equals(entry1.pos));
        }
    }
}
