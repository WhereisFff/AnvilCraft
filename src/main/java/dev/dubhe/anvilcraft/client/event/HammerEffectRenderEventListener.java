package dev.dubhe.anvilcraft.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.hammer.IHasHammerEffect;
import dev.dubhe.anvilcraft.block.multipart.IMultiPartBlockModelHolder.ModelRenderTarget;
import dev.dubhe.anvilcraft.client.support.HammerPreviewEntity;
import dev.dubhe.anvilcraft.client.support.HammerPreviewState;
import dev.dubhe.anvilcraft.integration.iris.IrisState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(Dist.CLIENT)
public class HammerEffectRenderEventListener {
    public static final Pair<Direction, Component>[] DIRECTION_TEXTS;
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(AnvilCraft.of("block/axis"));

    private static @Nullable HammerGhostBuffers ghostBuffers;
    private static @Nullable RenderType ghostType;

    static {
        Pair<Direction, Component>[] texts = new Pair[Direction.values().length - 2];
        int idx = 0;
        for (int i = 0; i < Direction.values().length; i++) {
            Direction direction = Direction.values()[i];
            MutableComponent component = Component.translatable("screen.anvilcraft.button.direction." + direction.getName());
            if (direction.getStepY() != 0) {
                continue;
            }
            texts[idx++] = Pair.of(direction, component);
        }
        DIRECTION_TEXTS = texts;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hideOutline(RenderHighlightEvent.Block event) {
        if (HammerPreviewState.hidesBlock(event.getTarget().getBlockPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        boolean shaders = IrisState.isShaderEnabled();
        var stage = shaders ? RenderLevelStageEvent.Stage.AFTER_LEVEL : RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
        if (event.getStage() != stage) {
            return;
        }
        var client = Minecraft.getInstance();
        var level = client.level;
        if (level == null || !(client.screen instanceof IHasHammerEffect effect) || !effect.shouldRender()
            || IrisState.isRenderingShadows()) {
            return;
        }
        RenderType type = effect.renderType();
        if (ghostBuffers == null || type != ghostType) {
            releaseBuffers();
            ghostType = type;
            ghostBuffers = new HammerGhostBuffers(type);
        }
        var matrices = RenderSystem.getModelViewStack();
        if (shaders) {
            matrices.pushMatrix().set(event.getModelViewMatrix());
            RenderSystem.applyModelViewMatrix();
        }
        try {
            draw(level, effect, event, ghostBuffers);
            ghostBuffers.endBatch();
        } finally {
            if (shaders) {
                matrices.popMatrix();
                RenderSystem.applyModelViewMatrix();
            }
        }
    }

    private static void draw(ClientLevel level, IHasHammerEffect effect, RenderLevelStageEvent event, HammerGhostBuffers buffers) {
        var original = new ModelRenderTarget(effect.hiddenBlockPos(), level.getBlockState(effect.hiddenBlockPos()));
        var selected = new ModelRenderTarget(effect.renderingBlockPos(), effect.renderingBlockState());
        var sources = original.parts();
        var targets = selected.parts();
        if (!original.state().is(selected.state().getBlock()) || sources.size() != targets.size()) {
            return;
        }
        var client = Minecraft.getInstance();
        var dispatcher = client.getBlockRenderer();
        var renderer = dispatcher.getModelRenderer();
        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        var tick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var type = effect.renderType();
        for (int i = 0; i < targets.size(); i++) {
            var source = sources.get(i);
            var part = targets.get(i);
            if (!level.getBlockState(source.pos()).is(source.state().getBlock())) {
                continue;
            }
            pose.pushPose();
            try {
                translate(pose, part, camera);
                var state = part.state();
                var model = dispatcher.getBlockModel(state);
                var entity = level.getBlockEntity(source.pos());
                var data = model.getModelData(level, part.pos(), state, entity == null ? ModelData.EMPTY : entity.getModelData());
                var random = RandomSource.create(state.getSeed(part.pos()));
                for (var layer : model.getRenderTypes(state, random, data)) {
                    renderer.renderModel(pose.last(), buffers.getBuffer(type), state, model, 1, 1, 1, LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY, data, layer);
                }
                HammerPreviewEntity.render(entity, state, tick, pose, buffers, true);
            } finally {
                pose.popPose();
            }
        }
        pose.pushPose();
        try {
            translate(pose, selected, camera);
            renderer.renderModel(pose.last(), buffers.getBuffer(type), selected.state(), client.getModelManager().getModel(MODEL),
                1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } finally {
            pose.popPose();
        }
    }

    private static void translate(PoseStack pose, ModelRenderTarget target, Vec3 camera) {
        var pos = target.pos();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
    }

    @SubscribeEvent
    public static void onUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            releaseBuffers();
        }
    }

    private static void releaseBuffers() {
        if (ghostBuffers != null) {
            ghostBuffers.close();
            ghostBuffers = null;
            ghostType = null;
        }
    }
}
