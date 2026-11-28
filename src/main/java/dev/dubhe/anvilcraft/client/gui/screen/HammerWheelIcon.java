package dev.dubhe.anvilcraft.client.gui.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.block.multipart.IMultiPartBlockModelHolder.ModelRenderTarget;
import dev.dubhe.anvilcraft.client.support.HammerPreviewEntity;
import dev.dubhe.anvilcraft.client.support.RenderSupport;
import dev.dubhe.anvilcraft.util.FullBrightLevelProxy;
import dev.dubhe.anvilcraft.util.VertexConsumerWithPose;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class HammerWheelIcon {
    private final ClientLevel level;
    private final ModelRenderTarget target;
    private final List<ModelRenderTarget> parts;
    private final List<ModelRenderTarget> sources;
    private final FullBrightLevelProxy view;
    private final AABB bounds;
    private final RandomSource random = RandomSource.create();

    HammerWheelIcon(ClientLevel level, ModelRenderTarget target, List<ModelRenderTarget> sources) {
        this.level = level;
        this.target = target;
        this.parts = target.parts();
        this.sources = sources;
        Map<BlockPos, BlockState> states = this.parts.stream().collect(Collectors.toMap(ModelRenderTarget::pos, ModelRenderTarget::state));
        this.view = new FullBrightLevelProxy(level) {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
            }

            @Override
            public FluidState getFluidState(BlockPos pos) {
                return this.getBlockState(pos).getFluidState();
            }
        };
        this.bounds = this.bounds();
    }

    private AABB bounds() {
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        var min = new Vector3f(Float.POSITIVE_INFINITY);
        var max = new Vector3f(Float.NEGATIVE_INFINITY);
        final float tick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        for (int index = 0; index < this.parts.size(); index++) {
            var part = this.parts.get(index);
            var offset = part.pos().subtract(this.target.pos());
            var state = part.state();
            var model = dispatcher.getBlockModel(state);
            var entity = this.level.getBlockEntity(this.sources.get(index).pos());
            ModelData data = model.getModelData(this.view, part.pos(), state, entity == null ? ModelData.EMPTY : entity.getModelData());
            long seed = state.getSeed(part.pos());
            this.random.setSeed(seed);
            for (var layer : model.getRenderTypes(state, this.random, data)) {
                for (int side = 0; side <= 6; side++) {
                    this.random.setSeed(seed);
                    var face = side == 6 ? null : Direction.from3DDataValue(side);
                    for (var quad : model.getQuads(state, face, this.random, data, layer)) {
                        int[] vertices = quad.getVertices();
                        int stride = vertices.length / 4;
                        for (int vertex = 0; vertex < 4; vertex++) {
                            int i = vertex * stride;
                            var point = new Vector3f(Float.intBitsToFloat(vertices[i]) + offset.getX(),
                                Float.intBitsToFloat(vertices[i + 1]) + offset.getY(),
                                Float.intBitsToFloat(vertices[i + 2]) + offset.getZ());
                            min.min(point);
                            max.max(point);
                        }
                    }
                }
            }
            HammerPreviewEntity.visitBounds(entity, state, tick, bounds -> {
                var box = bounds.move(offset);
                min.min(new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ));
                max.max(new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ));
            });
        }
        return min.isFinite() ? new AABB(min.x, min.y, min.z, max.x, max.y, max.z) : new AABB(BlockPos.ZERO);
    }

    void render(PoseStack pose, Camera camera, float menuScale) {
        var rotation = new Matrix4f().rotate(Axis.XP.rotationDegrees(camera.getXRot()))
            .rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180));
        double width = Math.abs(rotation.m00()) * this.bounds.getXsize() + Math.abs(rotation.m10()) * this.bounds.getYsize()
            + Math.abs(rotation.m20()) * this.bounds.getZsize();
        double height = Math.abs(rotation.m01()) * this.bounds.getXsize() + Math.abs(rotation.m11()) * this.bounds.getYsize()
            + Math.abs(rotation.m21()) * this.bounds.getZsize();
        float scale = 20 * menuScale / (float) Math.max(1, Math.max(width, height));
        var center = this.bounds.getCenter();
        var client = Minecraft.getInstance();
        var buffers = client.renderBuffers().bufferSource();
        var dispatcher = client.getBlockRenderer();
        float tick = client.getTimer().getGameTimeDeltaPartialTick(true);
        pose.pushPose();
        try {
            pose.translate(0, 0, 100);
            pose.scale(scale, -scale, scale);
            pose.mulPose(rotation);
            pose.translate(-center.x, -center.y, -center.z);
            RenderSystem.setupGui3DDiffuseLighting(RenderSupport.L1, RenderSupport.L2);
            for (int i = 0; i < this.parts.size(); i++) {
                var part = this.parts.get(i);
                var source = this.sources.get(i);
                var offset = part.pos().subtract(this.target.pos());
                var state = part.state();
                pose.pushPose();
                pose.translate(offset.getX(), offset.getY(), offset.getZ());
                var model = dispatcher.getBlockModel(state);
                var entity = this.level.getBlockEntity(source.pos());
                ModelData data = entity == null ? ModelData.EMPTY : entity.getModelData();
                for (var layer : model.getRenderTypes(state, this.random, data)) {
                    dispatcher.renderBatched(state, part.pos(), this.view, pose, buffers.getBuffer(layer), true, this.random, data, layer);
                }
                var fluid = state.getFluidState();
                if (!fluid.isEmpty()) {
                    dispatcher.renderLiquid(part.pos(), this.view, new VertexConsumerWithPose(
                        buffers.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluid)), pose.last(), part.pos()), state, fluid);
                }
                HammerPreviewEntity.render(entity, state, tick, pose, buffers, false);
                pose.popPose();
            }
            buffers.endBatch();
        } finally {
            pose.popPose();
            Lighting.setupFor3DItems();
        }
    }
}
