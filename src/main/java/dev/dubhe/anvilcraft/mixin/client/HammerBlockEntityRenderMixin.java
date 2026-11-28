package dev.dubhe.anvilcraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dubhe.anvilcraft.client.building.BuildingRodPlacementAnimation;
import dev.dubhe.anvilcraft.client.support.HammerPreviewState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
abstract class HammerBlockEntityRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void anvilcraft$hideOriginal(
        E entity, float partialTick, PoseStack pose, MultiBufferSource buffers, CallbackInfo ci
    ) {
        if (HammerPreviewState.hidesBlock(entity.getBlockPos()) || BuildingRodPlacementAnimation.hidesBlock(entity.getBlockPos())) {
            ci.cancel();
        }
    }
}
