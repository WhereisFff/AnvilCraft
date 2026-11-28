package dev.dubhe.anvilcraft.client.building;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.building.BlueprintBlockConfiguration;
import dev.dubhe.anvilcraft.building.BlueprintBlockEntities;
import dev.dubhe.anvilcraft.building.BlueprintMultiblocks;
import dev.dubhe.anvilcraft.building.BlueprintPlacement;
import dev.dubhe.anvilcraft.building.BuildingEntityTransform;
import dev.dubhe.anvilcraft.building.BuildingRodService;
import dev.dubhe.anvilcraft.building.EntityBuildAdapters;
import dev.dubhe.anvilcraft.building.StructureSnapshot;
import dev.dubhe.anvilcraft.integration.iris.IrisState;
import dev.dubhe.anvilcraft.item.BuildingRodItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID, value = Dist.CLIENT)
public final class BuildingRodRenderer {
    @Nullable
    private static StructureSnapshot built;
    private static Rotation rotation = Rotation.NONE;
    private static Mirror mirror = Mirror.NONE;
    private static int layer = -1;
    private static final int BLUEPRINT_ALPHA = 110;
    static final int MATERIAL_ALPHA = 166;
    static final float MATERIAL_SCALE = 0.64f;
    private static final BuildingRodAnimation ANIMATION = new BuildingRodAnimation();
    private static List<BuildingRodService.Cell> placementCells = List.of();
    private static @Nullable BoundingBox placementBounds;
    private static final Map<BlockPos, BoundingBox> PLACEMENT_UNITS = new HashMap<>();
    private static final List<BlockEntity> BLOCK_ENTITIES = new ArrayList<>();
    private static final List<Entity> BLUEPRINT_ENTITIES = new ArrayList<>();
    private static @Nullable BuildingRodGhostFeatures features;
    private static @Nullable VertexBuffer mesh;
    private static boolean shaderPack;
    private static int shaderGeneration;

    private BuildingRodRenderer() {
    }

    public static void clear() {
        BuildingRodRenderer.ANIMATION.clear();
        BuildingRodRenderer.clearMesh();
        if (features != null) {
            features.close();
            features = null;
        }
    }

    @EventBusSubscriber(modid = AnvilCraft.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    static final class Reload {
        @SubscribeEvent
        static void reload(ModelEvent.BakingCompleted event) {
            clear();
            BuildingRodPlacementAnimation.clear();
            BuildingRodModelParts.clear();
            BuildingRodRenderTypes.clearLayers();
        }
    }

    static void turn(int quarterTurns) {
        BuildingRodRenderer.ANIMATION.turn(quarterTurns);
    }

    private static void clearMesh() {
        BuildingRodRenderer.built = null;
        if (mesh != null) {
            mesh.close();
            mesh = null;
        }
        placementCells = List.of();
        placementBounds = null;
        PLACEMENT_UNITS.clear();
        BLOCK_ENTITIES.clear();
        BLUEPRINT_ENTITIES.clear();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (IrisState.isRenderingShadows()) {
            return;
        }
        boolean shaders = IrisState.isShaderEnabled();
        var stage = shaders ? RenderLevelStageEvent.Stage.AFTER_LEVEL : RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
        if (event.getStage() != stage) {
            return;
        }
        int generation = IrisState.shaderGeneration();
        if (shaderPack != shaders || shaderGeneration != generation) {
            clearMesh();
            shaderPack = shaders;
            shaderGeneration = generation;
        }
        var matrices = RenderSystem.getModelViewStack();
        matrices.pushMatrix().set(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();
        try {
            renderProjection(event);
        } finally {
            matrices.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private static void renderProjection(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        BuildingRodPlacementAnimation.render(mc.level, event);
        StructureSnapshot snapshot = BuildingRodClient.snapshot;
        if (snapshot == null) {
            if (BuildingRodItem.isHeld(mc.player) && BuildingRodItem.isPlacementMaterial(BuildingRodItem.material(mc.player))) {
                renderMaterial(event, mc.level);
            }
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        var bounds = BuildingRodClient.selectionBounds();
        if (bounds != null) {
            AABB box = AABB.of(bounds).move(-camera.x, -camera.y, -camera.z);
            var buffers = mc.renderBuffers().bufferSource();
            boolean valid = !BuildingRodClient.blueprintPlacements().isEmpty();
            LevelRenderer.renderLineBox(event.getPoseStack(), buffers.getBuffer(RenderType.lines()), box,
                valid ? 0.2f : 1, valid ? 1 : 0.2f, 0.8f, 1);
            buffers.endBatch(RenderType.lines());
        }
        if (BuildingRodClient.traditional() && !BuildingRodTraditionalControls.isActive()
            || BuildingRodClient.target == null && !BuildingRodClient.locked) {
            return;
        }
        var placement = BuildingRodClient.placement;
        if (built != snapshot || rotation != placement.rotation() || mirror != placement.mirror()
            || layer != BuildingRodClient.layer) {
            build(snapshot, placement);
        }
        var copies = BuildingRodClient.blueprintPlacements();
        for (var copy : copies) {
            renderCopy(event, snapshot, copy, copies.size() == 1);
        }
    }

    private static void renderMaterial(RenderLevelStageEvent event, ClientLevel level) {
        List<BuildingRodService.Cell> cells = BuildingRodClient.placementPreview();
        if (placementCells != cells && !placementCells.equals(cells)) {
            clearMesh();
            if (!cells.isEmpty()) {
                buildMaterial(level, cells);
            }
            placementCells = cells;
        }
        Vec3 camera = event.getCamera().getPosition();
        BoundingBox bounds = BuildingRodClient.selectionBounds();
        if (placementBounds != null) {
            var pose = event.getPoseStack();
            pose.pushPose();
            try {
                pose.translate(placementBounds.minX() - camera.x, placementBounds.minY() - camera.y, placementBounds.minZ() - camera.z);
                drawGhost(event, MATERIAL_ALPHA);
            } finally {
                pose.popPose();
            }
            bounds = bounds == null ? placementBounds : bounds.encapsulate(placementBounds);
        }
        if (bounds != null) {
            AABB box = AABB.of(bounds).move(-camera.x, -camera.y, -camera.z).inflate(0.002);
            var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
            var quads = buffers.getBuffer(RenderType.debugQuads());
            float green = cells.isEmpty() ? 0.333f : 1;
            float blue = cells.isEmpty() ? 0.467f : 1;
            for (Direction face : Direction.values()) {
                LevelRenderer.renderFace(event.getPoseStack(), quads, face, (float) box.minX, (float) box.minY, (float) box.minZ,
                    (float) box.maxX, (float) box.maxY, (float) box.maxZ, 1, green, blue, 28f / 255);
            }
            buffers.endBatch(RenderType.debugQuads());
            LevelRenderer.renderLineBox(event.getPoseStack(), buffers.getBuffer(BuildingRodRenderTypes.BOUNDS), box, 1, green, blue, 1);
            buffers.endBatch(BuildingRodRenderTypes.BOUNDS);
        }
    }

    private static void buildMaterial(ClientLevel level, List<BuildingRodService.Cell> cells) {
        BoundingBox bounds = new BoundingBox(cells.getFirst().pos());
        Map<BlockPos, BoundingBox> groups = new HashMap<>();
        var view = new BlueprintRenderView(level, cells.getFirst().pos(), PLACEMENT_UNITS, false);
        for (var cell : cells) {
            BlockPos pos = cell.pos();
            bounds.encapsulate(pos);
            var core = BlueprintMultiblocks.core(pos, cell.state());
            var unit = groups.computeIfAbsent(core, ignored -> new BoundingBox(pos));
            unit.encapsulate(pos);
            PLACEMENT_UNITS.put(pos, unit);
            var entity = BlueprintBlockEntities.create(level, pos, cell.state(), cell.config().isEmpty() ? null : cell.config());
            if (entity != null) {
                BLOCK_ENTITIES.add(entity);
            }
            view.put(pos, cell.state(), entity);
        }
        placementBounds = bounds;
        bake(view, MATERIAL_ALPHA);
    }

    private static void bake(BlueprintRenderView view, int alpha) {
        var renderer = Minecraft.getInstance().getBlockRenderer();
        var type = BuildingRodRenderTypes.BLOCK_GHOST;
        try (var memory = new ByteBufferBuilder(type.bufferSize())) {
            var buffer = IrisState.createUnextendedBufferBuilder(memory, type.mode(), type.format());
            var vertices = new PlacementVertices();
            for (var block : view.blocks()) {
                BlockPos pos = block.getKey();
                var unit = PLACEMENT_UNITS.get(pos);
                Vec3 pivot = unit == null ? Vec3.atLowerCornerOf(pos) : AABB.of(unit).getCenter();
                vertices.bake(view, renderer, pos, block.getValue(), pivot);
                var relative = placementBounds == null ? pivot
                    : pivot.subtract(placementBounds.minX(), placementBounds.minY(), placementBounds.minZ());
                vertices.draw(buffer, 0, vertices.end(), (float) relative.x, (float) relative.y,
                    (float) relative.z, unit == null ? 1 : MATERIAL_SCALE, alpha);
                vertices.clear();
            }
            var data = buffer.build();
            if (data != null) {
                mesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
                try {
                    mesh.bind();
                    IrisState.uploadUnextended(mesh, data);
                } finally {
                    VertexBuffer.unbind();
                }
            }
        }
    }

    private static void drawGhost(RenderLevelStageEvent event, int alpha) {
        var pose = event.getPoseStack();
        if (mesh != null) {
            var type = BuildingRodRenderTypes.BLOCK_GHOST;
            type.setupRenderState();
            try {
                mesh.bind();
                var modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose.last().pose());
                mesh.drawWithShader(modelView, event.getProjectionMatrix(), RenderSystem.getShader());
            } finally {
                VertexBuffer.unbind();
                type.clearRenderState();
            }
        }
        if (BLOCK_ENTITIES.isEmpty() && BLUEPRINT_ENTITIES.isEmpty()) {
            return;
        }
        if (features == null) {
            features = new BuildingRodGhostFeatures();
        }
        for (var entity : BLOCK_ENTITIES) {
            BlockPos pos = entity.getBlockPos();
            pose.pushPose();
            try {
                var unit = PLACEMENT_UNITS.get(pos);
                if (unit == null || placementBounds == null) {
                    pose.translate(pos.getX(), pos.getY(), pos.getZ());
                } else {
                    Vec3 pivot = AABB.of(unit).getCenter();
                    pose.translate(pivot.x - placementBounds.minX(), pivot.y - placementBounds.minY(), pivot.z - placementBounds.minZ());
                    pose.scale(MATERIAL_SCALE, MATERIAL_SCALE, MATERIAL_SCALE);
                    pose.translate(pos.getX() - pivot.x, pos.getY() - pivot.y, pos.getZ() - pivot.z);
                }
                features.renderPreview(entity, pose, alpha);
            } finally {
                pose.popPose();
            }
        }
        for (var entity : BLUEPRINT_ENTITIES) {
            features.renderPreview(entity, pose, alpha);
        }
        features.endBatch();
    }

    private static void renderCopy(
        RenderLevelStageEvent event, StructureSnapshot snapshot, BlueprintPlacement placement,
        boolean animate
    ) {
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        Vec3 center = AABB.of(placement.bounds(snapshot.size())).getCenter();
        BuildingRodAnimation.Pose visual = !animate ? new BuildingRodAnimation.Pose(
            center.x,
            center.y,
            center.z,
            placement.yawDegrees(),
            placement.mirrorX(),
            placement.mirrorZ()
        ) : BuildingRodRenderer.ANIMATION.sample(
            center.x,
            center.y,
            center.z,
            placement.yawDegrees(),
            placement.mirrorX(),
            placement.mirrorZ(),
            System.nanoTime()
        );
        pose.pushPose();
        pose.translate(visual.x() - camera.x, visual.y() - camera.y, visual.z() - camera.z);
        // 先撤销已烘焙姿态，再套用插值姿态；旋转和镜像共用结构中心。
        pose.mulPose(Axis.YP.rotationDegrees(visual.yaw()));
        pose.scale(visual.mirrorX() * placement.mirrorX(), 1, visual.mirrorZ() * placement.mirrorZ());
        pose.mulPose(Axis.YP.rotationDegrees(-placement.yawDegrees()));
        pose.translate(placement.anchor().getX() - center.x, placement.anchor().getY() - center.y, placement.anchor().getZ() - center.z);
        Minecraft mc = Minecraft.getInstance();
        if (BuildingRodClient.traditional()) {
            var local = new BlueprintPlacement(BlockPos.ZERO, placement.rotation(), placement.mirror());
            var outlineBuffers = mc.renderBuffers().bufferSource();
            LevelRenderer.renderLineBox(
                pose,
                outlineBuffers.getBuffer(RenderType.lines()),
                AABB.of(local.bounds(snapshot.size())),
                0.2f,
                0.9f,
                1,
                0.8f
            );
            outlineBuffers.endBatch(RenderType.lines());
        }
        boolean reversed = pose.last().pose().determinant3x3() < 0;
        int originalFace = reversed ? GL11.glGetInteger(GL11.GL_FRONT_FACE) : GL11.GL_CCW;
        try {
            if (reversed) {
                GL11.glFrontFace(originalFace == GL11.GL_CCW ? GL11.GL_CW : GL11.GL_CCW);
            }
            drawGhost(event, BLUEPRINT_ALPHA);
        } finally {
            if (reversed) {
                GL11.glFrontFace(originalFace);
            }
            pose.popPose();
        }
    }

    private static void build(StructureSnapshot snapshot, BlueprintPlacement placement) {
        BuildingRodRenderer.clearMesh();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        var view = new BlueprintRenderView(mc.level, placement.anchor(), Map.of(), false);
        BlueprintPlacement local = new BlueprintPlacement(BlockPos.ZERO, placement.rotation(), placement.mirror());
        List<BlueprintMultiblocks.PlacedBlock> blocks;
        try {
            blocks = BlueprintMultiblocks.expand(snapshot, local, BuildingRodClient.layer);
        } catch (IllegalArgumentException exception) {
            BuildingRodRenderer.built = snapshot;
            BuildingRodRenderer.rotation = placement.rotation();
            BuildingRodRenderer.mirror = placement.mirror();
            BuildingRodRenderer.layer = BuildingRodClient.layer;
            return;
        }
        BlockPos sourceOrigin = BlueprintBlockConfiguration.sourceOrigin(snapshot);
        for (var entry : blocks) {
            BlockPos pos = entry.pos();
            var state = entry.state();
            var data = entry.nbt().map(nbt -> {
                var transformed = nbt.copy();
                BlueprintBlockConfiguration.transform(transformed, local, sourceOrigin);
                return transformed;
            }).orElse(null);
            BlockEntity entity = BlueprintBlockEntities.create(mc.level, pos, state, data);
            if (entity != null) {
                BLOCK_ENTITIES.add(entity);
            }
            view.put(pos, state, entity);
        }
        for (var entry : snapshot.entities()) {
            EntityBuildAdapters.create(BuildingEntityTransform.transform(entry, local), mc.level)
                .filter(entity -> !EntityBuildAdapters.isTransient(entity)).ifPresent(BLUEPRINT_ENTITIES::add);
        }
        bake(view, BLUEPRINT_ALPHA);
        BuildingRodRenderer.built = snapshot;
        BuildingRodRenderer.rotation = placement.rotation();
        BuildingRodRenderer.mirror = placement.mirror();
        BuildingRodRenderer.layer = BuildingRodClient.layer;
    }
}
