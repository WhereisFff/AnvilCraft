package dev.dubhe.anvilcraft.client.support;

import dev.dubhe.anvilcraft.api.rendering.CacheableBERenderingPipeline;
import dev.dubhe.anvilcraft.api.rendering.CacheableBlockEntityRenderers;
import dev.dubhe.anvilcraft.block.multipart.IMultiPartBlockModelHolder.ModelRenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/** 区块编译线程与实体渲染器共享当前轮盘隐藏的部件位置。 */
public final class HammerPreviewState {
    private static volatile Set<BlockPos> hidden = Set.of();

    private HammerPreviewState() {
    }

    public static void begin(Level level, BlockPos selected, ModelRenderTarget original) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(selected.immutable());
        for (var part : original.parts()) {
            if (level.getBlockState(part.pos()).is(part.state().getBlock())) {
                positions.add(part.pos());
            }
        }
        hidden = Set.copyOf(positions);
        refresh(hidden);
    }

    public static void end() {
        Set<BlockPos> positions = hidden;
        hidden = Set.of();
        refresh(positions);
    }

    public static boolean hidesBlock(BlockPos pos) {
        return hidden.contains(pos);
    }

    public static boolean hidesNeighbor(BlockPos pos, Direction face) {
        var positions = hidden;
        return !positions.isEmpty() && positions.contains(pos.relative(face));
    }

    private static void refresh(Set<BlockPos> positions) {
        var client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        var pipeline = CacheableBERenderingPipeline.getInstance();
        for (BlockPos pos : positions) {
            client.levelRenderer.setBlockDirty(pos, true);
            var entity = client.level.getBlockEntity(pos);
            if (entity != null && pipeline != null && CacheableBlockEntityRenderers.get(entity.getType()) != null) {
                pipeline.update(entity);
            }
        }
    }
}
