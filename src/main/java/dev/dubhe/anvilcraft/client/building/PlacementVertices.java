package dev.dubhe.anvilcraft.client.building;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/** 每批共享紧凑的单元局部顶点，重放顶点时才选择当前帧的输出格式。 */
final class PlacementVertices implements VertexConsumer {
    private static final int STRIDE = 8;
    private static final int BOTTOM_FACE = 1 << 2;
    private final IntArrayList data = new IntArrayList();
    private int vertex;
    private float dx;
    private float dy;
    private float dz;
    private boolean placed;
    private final RandomSource random = RandomSource.create();

    void clear() {
        this.data.clear();
    }

    int end() {
        return this.data.size();
    }

    int bake(BlueprintRenderView view, BlockRenderDispatcher renderer, BlockPos pos, BlockState state, Vec3 pivot) {
        float x = (float) (pos.getX() - pivot.x);
        float y = (float) (pos.getY() - pivot.y);
        float z = (float) (pos.getZ() - pivot.z);
        this.placed = view.placed();
        view.selectUnit(pos);
        int mask = view.coveredFaces(pos, state);
        this.bakeModel(view, renderer, pos, state, x, y, z, mask, false);
        int terrainEnd = this.bakeFluid(view, renderer, pos, state, x, y, z);
        if (mask != 0) {
            this.bakeModel(view, renderer, pos, state, x, y, z, mask, true);
        }
        return terrainEnd;
    }

    private void bakeModel(BlueprintRenderView view, BlockRenderDispatcher renderer, BlockPos pos, BlockState state,
                           float x, float y, float z, int mask, boolean coveredOnly) {
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        this.dx = x;
        this.dy = y;
        this.dz = z;
        BakedModel model = renderer.getBlockModel(state);
        ModelData data = model.getModelData(view, pos, state, view.getModelData(pos));
        long seed = state.getSeed(pos);
        this.random.setSeed(seed);
        var pose = new PoseStack();
        BakedModel selected = mask == 0 ? model : new BakedModelWrapper<>(model) {
            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState block, @Nullable Direction side, RandomSource random,
                                            ModelData modelData, @Nullable RenderType layer) {
                boolean covered = side != null && (mask & (1 << side.ordinal())) != 0;
                return covered == coveredOnly ? this.originalModel.getQuads(block, side, random, modelData, layer) : List.of();
            }
        };
        for (RenderType layer : model.getRenderTypes(state, this.random, data)) {
            pose.pushPose();
            try {
                renderer.getModelRenderer().tesselateBlock(view, selected, state, pos, pose, this, true,
                    this.random, seed, OverlayTexture.NO_OVERLAY, data, layer);
            } finally {
                pose.popPose();
            }
        }
    }

    private int bakeFluid(
        BlueprintRenderView view, BlockRenderDispatcher renderer, BlockPos pos, BlockState state, float x, float y, float z
    ) {
        var fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            this.dx = x - SectionPos.sectionRelative(pos.getX());
            this.dy = y - SectionPos.sectionRelative(pos.getY());
            this.dz = z - SectionPos.sectionRelative(pos.getZ());
            if (IClientFluidTypeExtensions.of(fluid).renderFluid(fluid, view, pos, this, state)) {
                return this.end();
            }
            var fluids = renderer.getLiquidBlockRenderer();
            final int first = this.end();
            fluids.tesselate(view, pos, this, state, fluid);
            if (!view.placed()) {
                return this.end();
            }
            int terrainEnd = this.end();
            // 保留真实世界的水面高度与流向，只为缩小后露出的方向补面。
            float[] heights = new float[4];
            Arrays.fill(heights, fluid.getType().isSame(view.getFluidState(pos.above()).getType()) ? y + 1f : Float.NaN);
            int faces = 0;
            for (int index = first; index < terrainEnd; index += 4 * STRIDE) {
                int face = this.fluidFace(index, x, y, z);
                faces |= face;
                if (face != BOTTOM_FACE) {
                    this.alignFluidHeights(index, x, y, z, heights, true);
                }
            }
            view.separateFluids(true);
            try {
                fluids.tesselate(view, pos, this, state, fluid);
            } finally {
                view.separateFluids(false);
            }
            int write = terrainEnd;
            int[] vertices = this.data.elements();
            for (int read = terrainEnd; read < this.end(); read += 4 * STRIDE) {
                int face = this.fluidFace(read, x, y, z);
                if ((faces & face) == 0) {
                    if (face != BOTTOM_FACE) {
                        this.alignFluidHeights(read, x, y, z, heights, false);
                    }
                    System.arraycopy(vertices, read, vertices, write, 4 * STRIDE);
                    write += 4 * STRIDE;
                }
            }
            this.data.size(write);
            return terrainEnd;
        }
        return this.end();
    }

    // 补面的上沿与真实水面共用四角高度，避免相邻水源被分开缩放时出现缝隙。
    private void alignFluidHeights(int first, float x, float y, float z, float[] heights, boolean sample) {
        int[] values = this.data.elements();
        for (int i = first; i < first + 4 * STRIDE; i += STRIDE) {
            float height = Float.intBitsToFloat(values[i + 1]);
            if (height - y <= 0.01f) {
                continue;
            }
            int corner = (Float.intBitsToFloat(values[i]) - x > 0.5f ? 1 : 0)
                | (Float.intBitsToFloat(values[i + 2]) - z > 0.5f ? 2 : 0);
            if (sample) {
                heights[corner] = height;
            } else if (!Float.isNaN(heights[corner])) {
                values[i + 1] = Float.floatToRawIntBits(heights[corner]);
            }
        }
    }

    private int fluidFace(int first, float x, float y, float z) {
        int[] values = this.data.elements();
        for (int axis = 0; axis < 3; axis++) {
            int index = first + axis;
            int coordinate = values[index];
            if (coordinate == values[index + STRIDE] && coordinate == values[index + 2 * STRIDE]
                && coordinate == values[index + 3 * STRIDE]) {
                float origin = axis == 0 ? x : axis == 1 ? y : z;
                boolean positive = Float.intBitsToFloat(coordinate) - origin > (axis == 1 ? 0.01f : 0.5f);
                return 1 << (axis * 2 + (positive ? 1 : 0));
            }
        }
        return 1 << 3;
    }

    void draw(VertexConsumer out, int first, int end, float x, float y, float z, float scale, int alpha) {
        int[] vertices = this.data.elements();
        for (int i = first; i < end; i += STRIDE) {
            int color = vertices[i + 3];
            int normal = vertices[i + 7];
            int opacity = this.placed ? ARGB32.alpha(color) * alpha / 255 : alpha;
            int light = this.placed ? vertices[i + 6] : LightTexture.FULL_BRIGHT;
            out.addVertex(x + Float.intBitsToFloat(vertices[i]) * scale, y + Float.intBitsToFloat(vertices[i + 1]) * scale,
                z + Float.intBitsToFloat(vertices[i + 2]) * scale, ARGB32.color(opacity, color),
                Float.intBitsToFloat(vertices[i + 4]), Float.intBitsToFloat(vertices[i + 5]), 0, light,
                (byte) normal / 127f, (byte) (normal >>> 8) / 127f, (byte) (normal >>> 16) / 127f);
        }
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.vertex = this.data.size();
        this.data.add(Float.floatToRawIntBits(x + this.dx));
        this.data.add(Float.floatToRawIntBits(y + this.dy));
        this.data.add(Float.floatToRawIntBits(z + this.dz));
        for (int i = 3; i < STRIDE; i++) {
            this.data.add(0);
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        this.data.set(this.vertex + 3, color);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this.setColor(ARGB32.color(alpha, red, green, blue));
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        this.data.set(this.vertex + 4, Float.floatToRawIntBits(u));
        this.data.set(this.vertex + 5, Float.floatToRawIntBits(v));
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        this.data.set(this.vertex + 6, u | (v << 16));
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        this.data.set(this.vertex + 7, ((byte) (x * 127) & 255) | (((byte) (y * 127) & 255) << 8) | ((byte) (z * 127) << 16));
        return this;
    }

}
