package dev.dubhe.anvilcraft.client.building;

import dev.dubhe.anvilcraft.building.StructureSnapshotCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** 按蓝图局部坐标或材料的世界坐标烘焙模型，使缩放单元保留内部的面剔除和流体连接。 */
public final class BlueprintRenderView implements BlockAndTintGetter {
    private final ClientLevel level;
    private final BlockPos tintPos;
    private final Map<BlockPos, BoundingBox> units;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<BlockPos, BlockEntity> entities = new HashMap<>();
    private final boolean placed;
    private @Nullable BoundingBox unit;
    private boolean separateFluids;

    BlueprintRenderView(ClientLevel level, BlockPos tintPos, Map<BlockPos, BoundingBox> units, boolean placed) {
        this.level = level;
        this.tintPos = tintPos;
        this.units = units;
        this.placed = placed;
    }

    void put(BlockPos pos, BlockState state, @Nullable BlockEntity entity) {
        this.blocks.put(pos, state);
        if (entity != null) {
            this.entities.put(pos, entity);
        }
    }

    Iterable<Map.Entry<BlockPos, BlockState>> blocks() {
        return this.blocks.entrySet();
    }

    void selectUnit(BlockPos pos) {
        this.unit = this.units.get(pos);
    }

    public boolean exposesFace(BlockPos pos, Direction face) {
        return this.placed && this.unit != null && this.units.get(pos.relative(face)) != this.unit;
    }

    int coveredFaces(BlockPos pos, BlockState state) {
        if (!this.placed) {
            return 0;
        }
        var selected = this.unit;
        this.unit = null;
        int covered = 0;
        try {
            for (var face : Direction.values()) {
                BlockPos adjacent = pos.relative(face);
                if (this.units.get(adjacent) != selected && !Block.shouldRenderFace(state, this, pos, face, adjacent)) {
                    covered |= 1 << face.ordinal();
                }
            }
        } finally {
            this.unit = selected;
        }
        return covered;
    }

    boolean placed() {
        return this.placed;
    }

    void separateFluids(boolean separate) {
        this.separateFluids = separate;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        boolean visible = this.unit == null || this.units.get(pos) == this.unit;
        if (this.placed) {
            return this.separateFluids && !visible ? Blocks.AIR.defaultBlockState() : this.level.getBlockState(pos);
        }
        return visible ? this.blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState()) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        if (this.placed) {
            return this.level.getBlockEntity(pos);
        }
        return this.unit == null || this.units.get(pos) == this.unit ? this.entities.get(pos) : null;
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        if (this.placed) {
            return this.level.getModelData(pos);
        }
        var entity = this.getBlockEntity(pos);
        return entity == null ? ModelData.EMPTY : entity.getModelData();
    }

    @Override
    public @Nullable AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        return this.placed ? this.level.getAuxLightManager(pos) : null;
    }

    @Override
    public int getHeight() {
        return this.units.isEmpty() ? 2 * StructureSnapshotCodec.MAX_AXIS : this.level.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return this.units.isEmpty() ? -StructureSnapshotCodec.MAX_AXIS : this.level.getMinBuildHeight();
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return !this.units.isEmpty() && this.level.isOutsideBuildHeight(y);
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return this.level.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        if (!this.units.isEmpty()) {
            return this.level.getBlockTint(pos, resolver);
        }
        var plains = this.level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        return resolver.getColor(plains.value(), this.tintPos.getX(), this.tintPos.getZ());
    }

    @Override
    public int getBrightness(LightLayer type, BlockPos pos) {
        return this.placed ? this.level.getBrightness(type, pos) : 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount) {
        return this.placed ? this.level.getRawBrightness(pos, amount) : 15;
    }
}
