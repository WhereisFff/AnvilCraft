package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.api.heat.HeatProducerInfo;
import dev.dubhe.anvilcraft.api.heat.HeatRecorder;
import dev.dubhe.anvilcraft.api.heat.HeatTier;
import dev.dubhe.anvilcraft.api.heat.HeatTierLine;
import dev.dubhe.anvilcraft.block.entity.BaseLaserBlockEntity;
import dev.dubhe.anvilcraft.block.entity.HeliostatsBlockEntity;
import dev.dubhe.anvilcraft.block.entity.MineralFountainBlockEntity;
import dev.dubhe.anvilcraft.block.entity.PlasmaJetsBlockEntity;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.Set;

public class ModHeatProducerInfos {
    public static final HeatProducerInfo<HeliostatsBlockEntity> HELIOSTATS = HeatRecorder.registerProducerInfo(HeatProducerInfo.blockEntity(
        ModBlockEntities.HELIOSTATS,
        heliostats -> Set.of(heliostats.getIrritatePos()),
        HeatTierLine.builder()
            .addPoint(4, HeatTier.NORMAL)
            .addPoint(12, HeatTier.HEATED, 4)
            .addPoint(32, HeatTier.REDHOT, 4)
            .addPoint(80, HeatTier.GLOWING, 4)
            .addPoint(HeatTier.INCANDESCENT, 4)
            .build())
    );
    public static final HeatProducerInfo<MineralFountainBlockEntity> LAVA_MINERAL_FOUNTAIN = HeatRecorder.registerProducerInfo(
        HeatProducerInfo.blockEntity(
            (level, pos) -> level.getBlockEntity(pos, ModBlockEntities.MINERAL_FOUNTAIN.get())
                .filter(mineralFountain -> mineralFountain.getAroundBlock().is(Blocks.LAVA)),
            mineralFountain -> Set.of(mineralFountain.getBlockPos()),
            HeatTierLine.always(HeatTier.REDHOT, 20))
    );
    @SuppressWarnings("OptionalOfNullableMisuse")
    public static final HeatProducerInfo<BaseLaserBlockEntity> LASER_EMITTER = HeatRecorder.registerProducerInfo(
        HeatProducerInfo.blockEntity(
            (level, pos) -> Util.castSafely(level.getBlockEntity(pos), BaseLaserBlockEntity.class),
            laserEmitter -> Optional.ofNullable(laserEmitter.getIrradiateBlockPos())
                .map(Set::of)
                .orElse(Set.of()),
            HeatTierLine.builder()
                .addPoint(1, HeatTier.NORMAL)
                .addPoint(2, HeatTier.HEATED, 1)
                .addPoint(3, HeatTier.HEATED, 3)
                .addPoint(5, HeatTier.REDHOT, 1)
                .addPoint(9, HeatTier.REDHOT, 3)
                .addPoint(17, HeatTier.GLOWING, 1)
                .addPoint(33, HeatTier.GLOWING, 3)
                .addPoint(65, HeatTier.INCANDESCENT, 1)
                .addPoint(HeatTier.INCANDESCENT, 3)
                .build(),
            BaseLaserBlockEntity::getLaserLevel)
    );
    public static final HeatProducerInfo<PlasmaJetsBlockEntity> NO_MAGNET_PLASMA_JETS = HeatRecorder.registerProducerInfo(
        HeatProducerInfo.blockEntity(
            ModBlockEntities.PLASMA_JETS,
            plasmaJets -> plasmaJets.getHeatingPoses().getFirst(),
            HeatTierLine.always(HeatTier.GLOWING, 20))
    );
    public static final HeatProducerInfo<PlasmaJetsBlockEntity> MAGNET_PLASMA_JETS = HeatRecorder.registerProducerInfo(
        HeatProducerInfo.blockEntity(
            ModBlockEntities.PLASMA_JETS,
            plasmaJets -> plasmaJets.getHeatingPoses().getSecond(),
            HeatTierLine.always(HeatTier.INCANDESCENT, 20))
    );
}
