package dev.dubhe.anvilcraft.api.heat;

import com.google.common.collect.Comparators;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.block.HeaterBlock;
import dev.dubhe.anvilcraft.block.entity.HeliostatsBlockEntity;
import dev.dubhe.anvilcraft.block.entity.MineralFountainBlockEntity;
import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockEntities;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TempVariationManager {
    private static final Map<Level, TempVariationManager> INSTANCES = new HashMap<>();

    private final Level level;
    private final Set<BlockPos> heatableBlocks = Collections.synchronizedSet(new HashSet<>());
    private final Set<BlockPos> heliostats = Collections.synchronizedSet(new HashSet<>());
    private final Set<BlockPos> lavaMineralFountains = Collections.synchronizedSet(new HashSet<>());
    private final Set<BlockPos> plasmaJets = Collections.synchronizedSet(new HashSet<>());
    private final Multimap<BlockPos, TubeWallLayer> tubeWalls = Multimaps.synchronizedMultimap(HashMultimap.create());

    public static TempVariationManager getInstance(Level level) {
        if (level.isClientSide) return new TempVariationManager(level);
        if (!INSTANCES.containsKey(level)) {
            INSTANCES.put(level, new TempVariationManager(level));
        }
        return INSTANCES.get(level);
    }

    public TempVariationManager(Level level) {
        this.level = level;
    }

    public static void addHeatableBlock(BlockPos pos, Level level) {
        TempVariationManager.getInstance(level).heatableBlocks.add(pos);
    }

    public static void addHeliostats(BlockPos pos, Level level) {
        TempVariationManager.getInstance(level).heliostats.add(pos);
    }

    public static void addLavaMineralFountains(BlockPos pos, Level level) {
        TempVariationManager.getInstance(level).lavaMineralFountains.add(pos);
    }

    public static void trySpawnPlasmaJets(BlockPos pos, Level level) {
        TempVariationManager manager = TempVariationManager.getInstance(level);
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

    public static void tickAll() {
        INSTANCES.forEach((ignored, manager) -> manager.tick());
    }

    public void tick() {
        this.tryRaisePlasmaJets();

        Map<BlockPos, Pair<Optional<HeatTier>, Integer>> heatableBlocks = new HashMap<>();
        this.tickHeliostats(heatableBlocks);
        this.tickLavaMineralFountains(heatableBlocks);

        Set<BlockPos> poses = new HashSet<>();
        poses.addAll(this.heatableBlocks);
        poses.addAll(heatableBlocks.keySet());
        for (BlockPos pos : poses) {
            this.tickHeatableBlock(
                pos,
                Optional.ofNullable(heatableBlocks.get(pos)).flatMap(Pair::getFirst),
                Optional.ofNullable(heatableBlocks.get(pos)).map(Pair::getSecond).orElse(0)
            );
        }
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

    private void tickHeliostats(Map<BlockPos, Pair<Optional<HeatTier>, Integer>> heatableBlocks) {
        Set<BlockPos> irritatedPoses = new HashSet<>();
        for (Iterator<BlockPos> iterator = this.heliostats.iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            Optional<HeliostatsBlockEntity> heliostatsOp = this.level.getBlockEntity(pos, ModBlockEntities.HELIOSTATS.get());
            if (heliostatsOp.isEmpty() || !heliostatsOp.get().getWorkResult().isWorking()) {
                iterator.remove();
                continue;
            }
            irritatedPoses.add(heliostatsOp.get().getIrritatePos());
        }
        for (BlockPos irritatedPos : irritatedPoses) {
            BlockState heatable = this.level.getBlockState(irritatedPos);
            if (!heatable.is(ModBlockTags.HEATABLE_BLOCKS)) continue;
            int durationDiff = 80;
            Optional<HeatTier> tierOp = HeatableBlockRecorder.getCurrentTier(heatable.getBlock());
            if (tierOp.isPresent()) {
                HeatTier tier = HeatTier.findTierByCount(this.heliostats.size());
                int diff = tier.compareTo(tierOp.get());
                if (diff < 0) {
                    tierOp = Optional.empty();
                    durationDiff = 0;
                } else if (diff == 0) {
                    tierOp = Optional.empty();
                } else {
                    tierOp = Optional.of(tier);
                }
            }
            if (!heatableBlocks.containsKey(irritatedPos)) {
                heatableBlocks.put(irritatedPos, new Pair<>(tierOp, durationDiff));
            }
            int finalDurationDiff = durationDiff;
            Optional<HeatTier> finalTierOp = tierOp;
            heatableBlocks.computeIfPresent(
                irritatedPos,
                (pos, pair) -> new Pair<>(
                    pair.getFirst().flatMap(tier -> finalTierOp.map(tier1 -> Comparators.max(tier, tier1))),
                    pair.getSecond() + finalDurationDiff)
            );
        }
    }

    private void tickLavaMineralFountains(Map<BlockPos, Pair<Optional<HeatTier>, Integer>> heatableBlocks) {
        Set<BlockPos> heatingPoses = new HashSet<>();
        for (Iterator<BlockPos> iterator = this.lavaMineralFountains.iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            Optional<MineralFountainBlockEntity> lavaMineralFountainOp = this.level.getBlockEntity(pos, ModBlockEntities.MINERAL_FOUNTAIN.get());
            if (lavaMineralFountainOp.isEmpty() || !lavaMineralFountainOp.get().getAroundBlock().is(Blocks.LAVA)) {
                iterator.remove();
                continue;
            }
            heatingPoses.add(lavaMineralFountainOp.get().getBlockPos().above());
        }
        for (BlockPos heatingPos : heatingPoses) {
            BlockState heatable = this.level.getBlockState(heatingPos);
            if (!heatable.is(ModBlockTags.HEATABLE_BLOCKS)) continue;
            if (!heatableBlocks.containsKey(heatingPos)) {
                heatableBlocks.put(heatingPos, new Pair<>(Optional.of(HeatTier.REDHOT), 20));
            }
            heatableBlocks.computeIfPresent(
                heatingPos,
                (pos, pair) -> new Pair<>(
                    pair.getFirst().map(tier -> Comparators.max(tier, HeatTier.REDHOT)),
                    pair.getSecond() + 20)
            );
        }
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "RedundantClassCall"})
    private void tickHeatableBlock(BlockPos pos, Optional<HeatTier> tierDeltaOp, int durationDelta) {
        BlockState heatableState = this.level.getBlockState(pos);
        if (heatableState.isAir()) {
            this.heatableBlocks.remove(pos);
            return;
        }
        HeatableBlockEntity heatable = Util.castSafely(this.level.getBlockEntity(pos), HeatableBlockEntity.class).orElse(null);

        ResourceLocation id = HeatableBlockRecorder.BLOCK_TO_ID.get(heatableState.getBlock());
        if (tierDeltaOp.isPresent()) {
            HeatTier tier = tierDeltaOp.get();
            Optional<Block> tierBlockOp = HeatableBlockRecorder.getHeatableBlock(id, tier);
            if (tierBlockOp.isEmpty()) return;
            Block tierBlock = tierBlockOp.get();
            this.level.setBlockAndUpdate(pos, tierBlock.defaultBlockState());
            if (!EntityBlock.class.isInstance(tierBlock)) return;
            BlockEntity tierEntity = ((EntityBlock) tierBlock).newBlockEntity(pos, tierBlock.defaultBlockState());
            if (!HeatableBlockEntity.class.isInstance(tierEntity)) return;
            HeatableBlockEntity heatableEntity = (HeatableBlockEntity) tierEntity;
            this.level.addFreshBlockEntities(Collections.singleton(heatableEntity));
            this.level.blockEntityChanged(pos);
            heatable = heatableEntity;
        }

        if (heatable == null) return;
        if (durationDelta <= 0) {
            heatable.addDurationInTick(-1);
        } else {
            heatable.addDurationInTick(durationDelta);
        }

        if (heatable.getDuration() <= 0) {
            Optional<Block> prevTierOp = HeatableBlockRecorder.getHeatableBlockPrevTier(heatable.getBlockState().getBlock());
            if (prevTierOp.isEmpty()) return;
            Block prevTier = prevTierOp.get();
            this.level.setBlockAndUpdate(pos, prevTier.defaultBlockState());
            if (!EntityBlock.class.isInstance(prevTier)) return;
            BlockEntity tierEntity = ((EntityBlock) prevTier).newBlockEntity(pos, prevTier.defaultBlockState());
            if (!HeatableBlockEntity.class.isInstance(tierEntity)) return;
            HeatableBlockEntity heatableEntity = (HeatableBlockEntity) tierEntity;
            heatableEntity.setDuration(200);
            this.level.setBlockEntity(heatableEntity);
        }
    }

    public record TubeWallLayer(Pair<BlockPos, BlockPos> first, Pair<BlockPos, BlockPos> second) {
        public static TubeWallLayer of(BlockPos center) {
            return new TubeWallLayer(new Pair<>(center.north(), center.south()), new Pair<>(center.east(), center.west()));
        }
    }
}
