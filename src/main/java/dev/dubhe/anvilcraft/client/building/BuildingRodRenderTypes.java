package dev.dubhe.anvilcraft.client.building;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.dubhe.anvilcraft.client.init.ModShaders;
import dev.dubhe.anvilcraft.integration.iris.IrisState;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;

final class BuildingRodRenderTypes {
    private static final Map<RenderType, RenderType> LAYERS = new HashMap<>();
    static final RenderType BLOCK_GHOST = blockLayer(false);
    static final RenderType PLACED = blockLayer(true);
    static final RenderType BOUNDS = RenderType.create("anvilcraft:building_rod_bounds", DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES, 1536, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2)))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false));

    private BuildingRodRenderTypes() {
    }

    private static RenderType blockLayer(boolean placed) {
        return RenderType.create(placed ? "anvilcraft:building_rod_placed" : "anvilcraft:building_rod_ghost",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 786432, true, true,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(ModShaders::getBuildingRodGhost))
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(placed
                    ? new RenderStateShard.DepthTestStateShard("less", GL11.GL_LESS) : RenderStateShard.LEQUAL_DEPTH_TEST)
                .setWriteMaskState(placed ? RenderStateShard.COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(false));
    }

    static RenderType ghost(RenderType type) {
        return LAYERS.computeIfAbsent(type, BuildingRodRenderTypes::translucent);
    }

    static void clearLayers() {
        LAYERS.clear();
    }

    private static RenderType translucent(RenderType type) {
        if (type instanceof RenderType.CompositeRenderType composite) {
            if (composite.state().textureState instanceof RenderStateShard.TextureStateShard texture && texture.texture.isPresent()) {
                if (IrisState.isBlockFormat(type.format()) && texture.texture.get().equals(TextureAtlas.LOCATION_BLOCKS)) {
                    return BLOCK_GHOST;
                }
                if (composite.state().transparencyState == RenderStateShard.NO_TRANSPARENCY
                    && type.format() == DefaultVertexFormat.NEW_ENTITY) {
                    return RenderType.entityTranslucentCull(texture.texture.get());
                }
            }
            if (composite.state().transparencyState != RenderStateShard.NO_TRANSPARENCY) {
                return type;
            }
        }
        return new GhostLayer(type);
    }

    private static final class GhostLayer extends RenderType {
        GhostLayer(RenderType type) {
            super("anvilcraft:building_rod_feature", type.format(), type.mode(), type.bufferSize(), false, false, () -> {
                type.setupRenderState();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }, () -> {
                RenderSystem.disableBlend();
                type.clearRenderState();
            });
        }
    }
}
