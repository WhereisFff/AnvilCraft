package dev.dubhe.anvilcraft.client.support;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.anvilcraft.lib.v2.util.Util;
import dev.dubhe.anvilcraft.api.rendering.CacheableBlockEntityRenderer;
import dev.dubhe.anvilcraft.api.rendering.CacheableBlockEntityRenderers;
import dev.dubhe.anvilcraft.client.selection.ModelBlockSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/** 预览渲染期间临时替换方块实体朝向，结束时恢复实时状态。 */
public final class HammerPreviewEntity {
    private static boolean rendering;

    private HammerPreviewEntity() {
    }

    public static boolean isRendering() {
        return rendering;
    }

    public static void render(
        @Nullable BlockEntity entity, BlockState preview, float partialTick, PoseStack pose,
        MultiBufferSource.BufferSource buffers, boolean cached
    ) {
        withState(entity, preview, current -> {
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(current);
            if (renderer != null) {
                renderer.render(current, partialTick, pose, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
            if (cached) {
                renderCached(current, pose, buffers);
            }
        });
    }

    public static void visitBounds(@Nullable BlockEntity entity, BlockState preview, float partialTick, Consumer<AABB> bounds) {
        withState(entity, preview, current -> {
            for (var part : ModelBlockSelection.rendererParts(current, partialTick)) {
                bounds.accept(part.bounds());
            }
        });
    }

    private static void withState(@Nullable BlockEntity entity, BlockState preview, Consumer<BlockEntity> render) {
        if (entity == null || !preview.is(entity.getBlockState().getBlock()) || !entity.getType().isValid(preview)) {
            return;
        }
        BlockState original = entity.getBlockState();
        boolean previous = rendering;
        try {
            rendering = true;
            entity.setBlockState(preview);
            render.accept(entity);
        } finally {
            entity.setBlockState(original);
            rendering = previous;
        }
    }

    public static void renderCached(BlockEntity entity, PoseStack pose, MultiBufferSource.BufferSource buffers) {
        CacheableBlockEntityRenderer<BlockEntity> renderer = Util.cast(CacheableBlockEntityRenderers.get(entity.getType()));
        if (renderer != null) {
            renderer.render(entity, buffers, pose);
        }
    }
}
