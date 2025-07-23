package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.anvilcraft.block.entity.DetectorSlidingRailBlockEntity;
import dev.dubhe.anvilcraft.event.PistonMoveBlockListener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PistonStructureResolver.class)
abstract class PistonStructureResolverMixin {
    @Shadow
    @Final
    private List<BlockPos> toPush;

    @Shadow
    @Final
    private Level level;

    @Inject(method = "resolve", at = @At("RETURN"))
    private void onPistonResolve(CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide()) {
            return;
        }
        if (!cir.getReturnValue()) return;
        List<BlockPos> toPushBlocks = new ArrayList<>(toPush);
        PistonMoveBlockListener.onPistonMoveBlocks(level, toPushBlocks);
    }

    @ModifyConstant(method = "addBlockLine", constant = @Constant(intValue = 12, ordinal = 0))
    private int updateMaxPushDepth(int constant) {
        DetectorSlidingRailBlockEntity.MAX_PUSH_DEPTH = constant;
        return constant;
    }

    @Redirect(
        method = "addBlockLine",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 0))
    private boolean useEnhancedCheck00(
        BlockState instance, BlockState state, @Local(ordinal = 0, argsOnly = true) BlockPos pos, @Local(ordinal = 1) BlockPos otherPos
    ) {
        return instance.canStickTo(pos, otherPos, state);
    }

    @Redirect(
        method = "addBlockLine",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 1))
    private boolean useEnhancedCheck01(
        BlockState instance, BlockState state, @Local(ordinal = 0, argsOnly = true) BlockPos otherPos, @Local(ordinal = 1) BlockPos pos
    ) {
        return instance.canStickTo(pos, otherPos, state);
    }

    @Redirect(
        method = "addBranchingBlocks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 0))
    private boolean useEnhancedCheck10(
        BlockState instance, BlockState state, @Local(ordinal = 0, argsOnly = true) BlockPos otherPos, @Local(ordinal = 1) BlockPos pos
    ) {
        return instance.canStickTo(pos, otherPos, state);
    }

    @Redirect(
        method = "addBranchingBlocks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;canStickTo(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            ordinal = 1))
    private boolean useEnhancedCheck11(
        BlockState instance, BlockState state, @Local(ordinal = 0, argsOnly = true) BlockPos pos, @Local(ordinal = 1) BlockPos otherPos
    ) {
        return instance.canStickTo(pos, otherPos, state);
    }
}
