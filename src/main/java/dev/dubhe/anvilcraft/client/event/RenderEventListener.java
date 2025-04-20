package dev.dubhe.anvilcraft.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.dubhe.anvilcraft.api.tooltip.HudTooltipManager;
import dev.dubhe.anvilcraft.client.ModInspectionClient;
import dev.dubhe.anvilcraft.client.PowerGridClient;
import dev.dubhe.anvilcraft.item.AnvilHammerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderEventListener {

    @SubscribeEvent
    public static void onRenderInspection(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        DeltaTracker deltaTracker = event.getPartialTick();
        LevelRenderer renderer = event.getLevelRenderer();
        ModInspectionClient.INSTANCE.onRenderInspectionAction(
            poseStack,
            renderer,
            camera,
            deltaTracker
        );
    }


    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        if (Minecraft.getInstance().options.hideGui) return;
        Entity entity = event.getCamera().getEntity();
        MultiBufferSource.BufferSource bufferSource =
            event.getLevelRenderer().renderBuffers.bufferSource();

        Vec3 vec3 = event.getCamera().getPosition();
        double camX = vec3.x();
        double camY = vec3.y();
        double camZ = vec3.z();
        PowerGridClient.renderTransmitterLine(event.getPoseStack(), bufferSource, vec3);
        VertexConsumer vertexConsumer3 = bufferSource.getBuffer(RenderType.lines());
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack mainHandItem = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandItem = livingEntity.getItemInHand(InteractionHand.OFF_HAND);
            ItemStack handItem = mainHandItem.isEmpty() ? offHandItem : mainHandItem;
            if (!handItem.isEmpty()) {
                HudTooltipManager.INSTANCE.renderHandItemLevelTooltip(
                    handItem, event.getPoseStack(), vertexConsumer3, camX, camY, camZ);
            }
        }
        if (!(entity instanceof Player player)) return;
        if (!AnvilHammerItem.isWearing(player)) return;
        PowerGridClient.render(event.getPoseStack(), bufferSource, vec3);
        HitResult hit = Minecraft.getInstance().hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
            if (Minecraft.getInstance().level == null) return;
            BlockEntity e = Minecraft.getInstance().level.getBlockEntity(blockPos);
            if (e == null) return;
            HudTooltipManager.INSTANCE.renderAffectRange(e, event.getPoseStack(), vertexConsumer3, camX, camY, camZ);
        }
    }
}
