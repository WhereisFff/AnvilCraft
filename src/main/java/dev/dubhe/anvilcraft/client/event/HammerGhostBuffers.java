package dev.dubhe.anvilcraft.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dubhe.anvilcraft.client.init.ModRenderTypes;
import dev.dubhe.anvilcraft.client.support.RenderSupport;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

final class HammerGhostBuffers extends MultiBufferSource.BufferSource implements AutoCloseable {
    private final Map<RenderType, RenderType> layers = new HashMap<>();
    private final RenderType blockLayer;

    HammerGhostBuffers(RenderType blockLayer) {
        super(new ByteBufferBuilder(32768), new LinkedHashMap<>());
        this.blockLayer = blockLayer;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        RenderType layer = this.layers.computeIfAbsent(type, this::ghostLayer);
        boolean shaderTint = layer == ModRenderTypes.TRANSLUCENT_COLORED_OVERLAY;
        return new VertexConsumerWrapper(super.getBuffer(layer)) {
            @Override
            public VertexConsumer setColor(int red, int green, int blue, int alpha) {
                this.parent.setColor(shaderTint ? red : red * 3 / 5, shaderTint ? green : green * 9 / 10, blue, alpha * 3 / 5);
                return this;
            }
        };
    }

    private RenderType ghostLayer(RenderType type) {
        if (type == this.blockLayer || RenderType.chunkBufferLayers().contains(type)) {
            return this.blockLayer;
        }
        if (type instanceof RenderType.CompositeRenderType layer) {
            var texture = layer.state().textureState;
            if (type.format() == DefaultVertexFormat.BLOCK && texture instanceof RenderStateShard.TextureStateShard single
                && single.texture.filter(TextureAtlas.LOCATION_BLOCKS::equals).isPresent()) {
                return this.blockLayer;
            }
            if (layer.state().transparencyState != RenderStateShard.NO_TRANSPARENCY) {
                return type;
            }
            if (type.format() == DefaultVertexFormat.NEW_ENTITY) {
                var translucent = RenderSupport.useTranslucentIfPossible(type);
                if (translucent != type) {
                    return translucent;
                }
            }
        }
        return new RenderType("anvilcraft:hammer_ghost", type.format(), type.mode(), type.bufferSize(), false, true,
            () -> {
                type.setupRenderState();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }, () -> {
                RenderSystem.disableBlend();
                type.clearRenderState();
            }) {};
    }

    @Override
    public void close() {
        this.sharedBuffer.close();
    }
}
