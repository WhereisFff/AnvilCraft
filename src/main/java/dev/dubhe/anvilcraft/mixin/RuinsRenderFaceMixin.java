package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.dubhe.anvilcraft.block.RuinsBlock;
import dev.dubhe.anvilcraft.block.entity.RuinsBlockEntity;
import dev.dubhe.anvilcraft.client.building.BlueprintRenderView;
import dev.dubhe.anvilcraft.client.building.BuildingRodPlacementAnimation;
import dev.dubhe.anvilcraft.client.support.HammerPreviewState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
abstract class RuinsRenderFaceMixin {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void anvilcraft$showPreviewFace(
        BlockState state, BlockGetter level, BlockPos pos, Direction face, BlockPos neighbor, CallbackInfoReturnable<Boolean> result
    ) {
        boolean exposed = level instanceof BlueprintRenderView view
            ? view.exposesFace(pos, face)
            : BuildingRodPlacementAnimation.hidesNeighbor(pos, face) || HammerPreviewState.hidesNeighbor(pos, face);
        if (exposed) {
            result.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(
        method = "shouldRenderFace",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)"
            + "Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private static BlockState anvilcraft$disguisedNeighbor(
        BlockState neighbor, BlockState state, BlockGetter level, BlockPos offset, Direction face, BlockPos pos
    ) {
        if (neighbor.getBlock() instanceof RuinsBlock && level.getBlockEntity(pos) instanceof RuinsBlockEntity ruins) {
            return ruins.getDisplayState();
        }
        return neighbor;
    }
}
