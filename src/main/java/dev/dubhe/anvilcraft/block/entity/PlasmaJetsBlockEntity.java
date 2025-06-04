package dev.dubhe.anvilcraft.block.entity;

import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.api.chargecollector.ChargeCollectorManager;
import dev.dubhe.anvilcraft.api.heat.HeatProducerManager;
import dev.dubhe.anvilcraft.block.FireCauldronBlock;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModHeatProducerInfos;
import net.createmod.catnip.data.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static dev.dubhe.anvilcraft.api.power.PowerGrid.GRID_TICK;

public class PlasmaJetsBlockEntity extends BlockEntity {
    private static final int MAX_DURATION = 10 * 60 * 20;
    private final Set<TubeWallLayer> tubeWalls = new HashSet<>();
    private int duration;

    public PlasmaJetsBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static PlasmaJetsBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new PlasmaJetsBlockEntity(type, pos, blockState);
    }

    private void tryRaise() {
        if (this.tubeWalls.size() == 4) return;
        BlockPos pos = this.getBlockPos();
        if (this.level != null
            && (this.level.getBlockState(pos.north()).isAir()
                || this.level.getBlockState(pos.south()).isAir()
                || this.level.getBlockState(pos.east()).isAir()
                || this.level.getBlockState(pos.west()).isAir())
        ) return;
        this.tubeWalls.add(TubeWallLayer.of(pos));
        this.level.removeBlock(pos, false);
        this.level.setBlock(pos.above(), ModBlocks.PLASMA_JETS.getDefaultState(), 3);
    }

    public void tick() {
        this.tryRaise();
        this.duration--;

        if (this.level == null) return;

        boolean wallBroken = false;
        for (TubeWallLayer layer : this.tubeWalls) {
            if (layer.isBroken(this.level)) {
                wallBroken = true;
                break;
            }
        }
        boolean blocked = false;
        for (int i = 1; i <= this.tubeWalls.size(); i++) {
            if (!this.level.getBlockState(this.getBlockPos().below(i)).isAir()) {
                blocked = true;
                break;
            }
        }
        BlockPos cauldronPos = this.getBlockPos().below(this.tubeWalls.size() + 1);
        boolean cauldronExisting = this.level.getBlockState(cauldronPos)
            .is(ModBlocks.FIRE_CAULDRON);
        boolean belowCauldronIsNotHeater = !this.level.getBlockState(cauldronPos.below(1))
            .is(ModBlocks.HEATER);
        if (wallBroken || blocked || !cauldronExisting || belowCauldronIsNotHeater) {
            this.level.removeBlockEntity(this.getBlockPos());
            this.level.removeBlock(this.getBlockPos(), false);
            HeatProducerManager.removeProducer(this.getBlockPos(), this.level, ModHeatProducerInfos.NO_MAGNET_PLASMA_JETS);
            HeatProducerManager.removeProducer(this.getBlockPos(), this.level, ModHeatProducerInfos.MAGNET_PLASMA_JETS);
            return;
        }
        HeatProducerManager.addProducer(this.getBlockPos(), this.level, ModHeatProducerInfos.NO_MAGNET_PLASMA_JETS);
        HeatProducerManager.addProducer(this.getBlockPos(), this.level, ModHeatProducerInfos.MAGNET_PLASMA_JETS);

        BlockState cauldronState = this.level.getBlockState(cauldronPos);
        Optional<Integer> cauldronLevel = cauldronState.getOptionalValue(FireCauldronBlock.LEVEL)
            .filter(i -> i < 1);
        if (cauldronLevel.isPresent() && this.duration + MAX_DURATION / 2 < MAX_DURATION) {
            this.duration += MAX_DURATION / 2;
            FireCauldronBlock.lowerFillLevel(cauldronState, this.level, cauldronPos);
        }

        if (this.level.getGameTime() % GRID_TICK != 0) return;
        for (TubeWallLayer layer : this.tubeWalls) {
            Pair<BlockPos, BlockPos> posPair = switch (layer.isMagnet(this.level)) {
                case TRUE -> layer.first;
                case FALSE -> layer.second;
                case DEFAULT -> null;
            };
            if (posPair == null) continue;
            BlockPos pos = posPair.getFirst();
            double uncharged = 512;
            for (ChargeCollectorManager.Entry entry : ChargeCollectorManager.getInstance(this.level).getNearestChargeCollect(pos)) {
                ChargeCollectorBlockEntity entity = entry.getBlockEntity();
                if (ChargeCollectorManager.getInstance(level).canCollect(entity, pos)) {
                    uncharged = entity.incomingCharge(uncharged, pos);
                    if (uncharged == 0) {
                        break;
                    }
                }
            }
            pos = posPair.getSecond();
            uncharged = 512;
            for (ChargeCollectorManager.Entry entry : ChargeCollectorManager.getInstance(this.level).getNearestChargeCollect(pos)) {
                ChargeCollectorBlockEntity entity = entry.getBlockEntity();
                if (ChargeCollectorManager.getInstance(level).canCollect(entity, pos)) {
                    uncharged = entity.incomingCharge(uncharged, pos);
                    if (uncharged == 0) {
                        break;
                    }
                }
            }
        }
    }

    public Pair<Set<BlockPos>, Set<BlockPos>> getHeatingPoses() {
        if (this.getLevel() != null) {
            return this.getHeatingPoses(this.getLevel());
        }
        return new Pair<>(Set.of(), Set.of());
    }

    public Pair<Set<BlockPos>, Set<BlockPos>> getHeatingPoses(Level level) {
        Set<BlockPos> noMagnet = new HashSet<>();
        Set<BlockPos> magnet = new HashSet<>();
        for (TubeWallLayer layer : this.tubeWalls) {
            if (layer.isMagnet(level) == TriState.DEFAULT) {
                noMagnet.addAll(layer.getHeatablePoses(level));
            } else {
                magnet.addAll(layer.getHeatablePoses(level));
            }
        }
        return new Pair<>(noMagnet, magnet);
    }

    public record TubeWallLayer(Pair<BlockPos, BlockPos> first, Pair<BlockPos, BlockPos> second) {
        public static TubeWallLayer of(BlockPos center) {
            return new TubeWallLayer(new Pair<>(center.north(), center.south()), new Pair<>(center.east(), center.west()));
        }

        public boolean isBroken(Level level) {
            return level.getBlockState(this.second.getFirst()).isAir()
                   && level.getBlockState(this.second.getSecond()).isAir()
                   && level.getBlockState(this.first.getFirst()).isAir()
                   && level.getBlockState(this.first.getSecond()).isAir();
        }

        /**
         * @return {@link TriState#DEFAULT default} 说明该层不是磁铁层
         * {@link TriState#TRUE true} 说明 {@link TubeWallLayer#first() 第一对} 是可加热方块
         * {@link TriState#FALSE false} 说明 {@link TubeWallLayer#second() 第二对} 是可加热方块
         */
        public TriState isMagnet(Level level) {
            if (level.getBlockState(this.first.getFirst()).is(ModBlockTags.MAGNET)
                && level.getBlockState(this.first.getSecond()).is(ModBlockTags.MAGNET)
                && level.getBlockState(this.second.getFirst()).is(ModBlockTags.HEATABLE_BLOCKS)
                && level.getBlockState(this.second.getSecond()).is(ModBlockTags.HEATABLE_BLOCKS)
            ) {
                return TriState.TRUE;
            } else if (level.getBlockState(this.second.getFirst()).is(ModBlockTags.MAGNET)
                       && level.getBlockState(this.second.getSecond()).is(ModBlockTags.MAGNET)
                       && level.getBlockState(this.first.getFirst()).is(ModBlockTags.HEATABLE_BLOCKS)
                       && level.getBlockState(this.first.getSecond()).is(ModBlockTags.HEATABLE_BLOCKS)
            ) {
                return TriState.FALSE;
            }
            return TriState.DEFAULT;
        }

        public Set<BlockPos> getHeatablePoses(Level level) {
            Set<BlockPos> poses = new HashSet<>();
            if (level.getBlockState(this.first.getFirst()).is(ModBlockTags.HEATABLE_BLOCKS)) {
                poses.add(this.first.getFirst());
            }
            if (level.getBlockState(this.first.getSecond()).is(ModBlockTags.HEATABLE_BLOCKS)) {
                poses.add(this.first.getSecond());
            }
            if (level.getBlockState(this.second.getFirst()).is(ModBlockTags.HEATABLE_BLOCKS)) {
                poses.add(this.second.getFirst());
            }
            if (level.getBlockState(this.second.getSecond()).is(ModBlockTags.HEATABLE_BLOCKS)) {
                poses.add(this.second.getSecond());
            }
            return poses;
        }
    }
}
