package dev.dubhe.anvilcraft.integration.iris;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.dubhe.anvilcraft.client.renderer.RenderState;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jspecify.annotations.Nullable;

public class IrisState {
    private static final boolean HAS_IRIS_API = IrisState.class.getClassLoader()
        .getResource("net/irisshaders/iris/Iris.class") != null;

    private static boolean hasIrisApi() {
        return HAS_IRIS_API && RenderState.isIrisPresent();
    }

    public static boolean isRenderingShadows() {
        return hasIrisApi() && IrisApi.getInstance().isRenderingShadowPass();
    }

    public static boolean isBlockFormat(VertexFormat format) {
        return format == DefaultVertexFormat.BLOCK || hasIrisApi() && format == IrisVertexFormats.TERRAIN;
    }

    public static int shaderGeneration() {
        return hasIrisApi() ? Iris.getPipelineManager().getVersionCounterForSodiumShaderReload() : 0;
    }

    public static boolean isShaderEnabled() {
        if (hasIrisApi()) {
            return isShaderEnabledInternal();
        }
        return false;
    }

    private static boolean isShaderEnabledInternal() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    /** 缓存管线按原版顶点布局上传，创建缓冲时禁止光影扩展该布局。 */
    public static BufferBuilder createUnextendedBufferBuilder(
        ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format
    ) {
        if (!hasIrisApi()) {
            return new BufferBuilder(buffer, mode, format);
        }
        return createUnextendedBufferBuilderInternal(buffer, mode, format);
    }

    private static BufferBuilder createUnextendedBufferBuilderInternal(
        ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format
    ) {
        boolean previous = ImmediateState.skipExtension.get();
        ImmediateState.skipExtension.set(true);
        try {
            return new BufferBuilder(buffer, mode, format);
        } finally {
            ImmediateState.skipExtension.set(previous);
        }
    }

    public static void uploadUnextended(VertexBuffer buffer, MeshData data) {
        if (!hasIrisApi()) {
            buffer.upload(data);
            return;
        }
        boolean extended = ImmediateState.renderWithExtendedVertexFormat;
        ImmediateState.renderWithExtendedVertexFormat = false;
        try {
            buffer.upload(data);
        } finally {
            ImmediateState.renderWithExtendedVertexFormat = extended;
        }
    }

    public static void setupUnextendedBufferState(@Nullable VertexFormat previous, VertexFormat current) {
        if (!hasIrisApi()) {
            setupBufferState(previous, current);
            return;
        }
        boolean extended = ImmediateState.renderWithExtendedVertexFormat;
        ImmediateState.renderWithExtendedVertexFormat = false;
        try {
            setupBufferState(previous, current);
        } finally {
            ImmediateState.renderWithExtendedVertexFormat = extended;
        }
    }

    private static void setupBufferState(@Nullable VertexFormat previous, VertexFormat current) {
        if (previous != null) previous.clearBufferState();
        current.setupBufferState();
    }

    public static MultiBufferSource getHandBufferSource(MultiBufferSource fallback) {
        if (!hasIrisApi()) return fallback;
        try {
            return getHandBufferSourceInternal(fallback);
        } catch (LinkageError ignored) {
            return fallback;
        }
    }

    private static MultiBufferSource getHandBufferSourceInternal(MultiBufferSource fallback) {
        return HandRenderer.INSTANCE.isActive() ? HandRenderer.INSTANCE.getBufferSource() : fallback;
    }

}
