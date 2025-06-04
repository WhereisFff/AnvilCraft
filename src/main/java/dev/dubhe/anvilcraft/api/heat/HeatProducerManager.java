package dev.dubhe.anvilcraft.api.heat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.block.PlasmaJetsBlock;
import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.util.Util;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.dubhe.anvilcraft.api.power.PowerGrid.GRID_TICK;

public class HeatProducerManager {
    private static final Map<Level, HeatProducerManager> INSTANCES = new HashMap<>();

    private final Level level;
    private final Set<BlockPos> heatableBlocks = Collections.synchronizedSet(new HashSet<>());
    private final Multimap<HeatProducerInfo<?>, BlockPos> producers = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public static HeatProducerManager getInstance(Level level) {
        if (level.isClientSide) return new HeatProducerManager(level);
        if (!INSTANCES.containsKey(level)) {
            INSTANCES.put(level, new HeatProducerManager(level));
        }
        return INSTANCES.get(level);
    }

    public HeatProducerManager(Level level) {
        this.level = level;
    }

    public static void addHeatableBlock(BlockPos pos, Level level) {
        HeatProducerManager.getInstance(level).heatableBlocks.add(pos);
    }

    public static void addProducer(BlockPos pos, Level level, HeatProducerInfo<?> info) {
        HeatProducerManager.getInstance(level).producers.put(info, pos);
    }

    public static void removeProducer(BlockPos pos, Level level, HeatProducerInfo<?> info) {
        HeatProducerManager.getInstance(level).producers.remove(info, pos);
    }

    public static void tickAll() {
        INSTANCES.forEach((ignored, manager) -> manager.tick());
    }

    public void tick() {
        if (this.level.getGameTime() % GRID_TICK != 0) return;
        Map<BlockPos, HeatTierLine.Point> heatableBlocks = new HashMap<>();
        for (HeatProducerInfo<?> info : this.producers.keySet()) {
            this.tickProducers(info, heatableBlocks);
        }

        Set<BlockPos> poses = new HashSet<>();
        poses.addAll(this.heatableBlocks);
        poses.addAll(heatableBlocks.keySet());
        for (BlockPos pos : poses) {
            this.tickHeatableBlock(pos, heatableBlocks.get(pos));
        }
    }

    private <T> void tickProducers(
        HeatProducerInfo<T> info,
        Map<BlockPos, HeatTierLine.Point> heatableBlocks
    ) {
        Collection<BlockPos> producerPoses = this.producers.get(info);
        Object2IntMap<BlockPos> heatablePosesAndProducerCount = new Object2IntArrayMap<>();
        for (Iterator<BlockPos> it = producerPoses.iterator(); it.hasNext();) {
            BlockPos producerPos = it.next();
            Optional<T> producerOp = info.getter().apply(this.level, producerPos);
            if (producerOp.isEmpty()) {
                it.remove();
            }
            producerOp.map(producer -> new Pair<>(info.posesGetter().apply(producer), info.countGetter().applyAsInt(producer)))
                .ifPresent(
                    pair -> pair.getFirst()
                        .forEach(heatablePos -> heatablePosesAndProducerCount.mergeInt(heatablePos, pair.getSecond(), Integer::sum))
                );
        }
        for (BlockPos heatablePos : heatablePosesAndProducerCount.keySet()) {
            Optional<HeatTierLine.Point> pointOp = info.line().getPoint(heatablePosesAndProducerCount.getInt(heatablePos));
            if (pointOp.isEmpty()) continue;
            heatableBlocks.merge(heatablePos, pointOp.get(), HeatTierLine.Point::merge);
        }
    }

    private void tickHeatableBlock(BlockPos pos, @Nullable HeatTierLine.Point point) {
        this.heatableBlocks.remove(pos);
        BlockState heatableState = this.level.getBlockState(pos);
        if (!heatableState.is(ModBlockTags.HEATABLE_BLOCKS)) return;
        this.heatableBlocks.add(pos);

        HeatableBlockEntity heatable = Util.castSafely(this.level.getBlockEntity(pos), HeatableBlockEntity.class).orElse(null);
        ResourceLocation id = HeatRecorder.BLOCK_TO_ID.get(heatableState.getBlock());
        HeatTier tier = HeatRecorder.getCurrentTier(heatableState.getBlock())
            .orElseThrow(() -> new IllegalStateException("Unexpected non tier heatable block!"));
        HeatTier tierDelta = Optional.ofNullable(point).map(HeatTierLine.Point::tier).orElse(tier);
        int durationDelta = Optional.ofNullable(point).map(HeatTierLine.Point::duration).orElse(0);
        if (tierDelta.compareTo(tier) > 0) {
            Block deltaBlock = HeatRecorder.getHeatableBlock(id, tierDelta)
                .orElseThrow(() -> new IllegalStateException("Unexpected non heatable block tier!"));
            this.level.setBlockAndUpdate(pos, deltaBlock.defaultBlockState());
            if (!(deltaBlock instanceof EntityBlock deltaEntityBlock)) return;
            BlockEntity deltaBlockEntity = deltaEntityBlock.newBlockEntity(pos, deltaBlock.defaultBlockState());
            if (!(deltaBlockEntity instanceof HeatableBlockEntity heatableEntity)) return;
            this.level.setBlockEntity(heatableEntity);
            heatable = heatableEntity;
        } else if (tierDelta.compareTo(tier) < 0) {
            durationDelta = 0;
        }
        if (heatable == null) return;

        if (durationDelta <= 0) {
            heatable.addDuration(-1);
        } else {
            heatable.addDurationInTick(durationDelta);
        }

        if (heatable.getDuration() <= 0) {
            Optional<Block> prevTierOp = HeatRecorder.getHeatableBlockPrevTier(heatable.getBlockState().getBlock());
            if (prevTierOp.isEmpty()) return;
            Block prevBlock = prevTierOp.get();
            this.level.setBlockAndUpdate(pos, prevBlock.defaultBlockState());
            if (!(prevBlock instanceof EntityBlock prevEntityBlock)) return;
            BlockEntity tierEntity = prevEntityBlock.newBlockEntity(pos, prevBlock.defaultBlockState());
            if (!(tierEntity instanceof HeatableBlockEntity heatableEntity)) return;
            heatableEntity.addDuration(10);
            this.level.setBlockEntity(heatableEntity);
        }
    }

}
