package dev.dubhe.anvilcraft.client.building;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dubhe.anvilcraft.client.support.HammerPreviewEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

import java.util.LinkedHashMap;

final class BuildingRodGhostFeatures extends MultiBufferSource.BufferSource implements AutoCloseable {
    private int alpha;
    private boolean placed;

    BuildingRodGhostFeatures() {
        super(new ByteBufferBuilder(65536), new LinkedHashMap<>());
    }

    void renderPreview(BlockEntity entity, PoseStack pose, int alpha) {
        this.placed = false;
        this.render(entity, pose, 0, LightTexture.FULL_BRIGHT, alpha);
    }

    void renderPreview(Entity entity, PoseStack pose, int alpha) {
        this.placed = false;
        this.alpha = alpha;
        Minecraft.getInstance().getEntityRenderDispatcher().render(entity, entity.getX(), entity.getY(), entity.getZ(),
            entity.getYRot(), 0, pose, this, LightTexture.FULL_BRIGHT);
    }

    void renderPlaced(BlockEntity entity, PoseStack pose, float partialTick, int light, int alpha) {
        this.placed = true;
        this.render(entity, pose, partialTick, light, alpha);
    }

    private void render(BlockEntity entity, PoseStack pose, float partialTick, int light, int alpha) {
        this.alpha = alpha;
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
        if (renderer != null) {
            renderer.render(entity, partialTick, pose, this, light, OverlayTexture.NO_OVERLAY);
        }
        HammerPreviewEntity.renderCached(entity, pose, this);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return new VertexConsumerWrapper(super.getBuffer(BuildingRodRenderTypes.ghost(type))) {
            @Override
            public VertexConsumer setColor(int color) {
                return this.setColor(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color),
                    FastColor.ARGB32.alpha(color));
            }

            @Override
            public VertexConsumer setColor(int red, int green, int blue, int opacity) {
                int alpha = BuildingRodGhostFeatures.this.alpha;
                this.parent.setColor(red, green, blue, BuildingRodGhostFeatures.this.placed ? opacity * alpha / 255 : alpha);
                return this;
            }

            @Override
            public VertexConsumer setUv2(int u, int v) {
                if (BuildingRodGhostFeatures.this.placed) {
                    this.parent.setUv2(u, v);
                } else {
                    this.parent.setUv2(240, 240);
                }
                return this;
            }
        };
    }

    @Override
    public void close() {
        this.sharedBuffer.close();
    }
}
