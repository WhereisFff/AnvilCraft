package dev.dubhe.anvilcraft.mixin.piston;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.anvilcraft.api.injection.block.entity.IPistonMovingBlockEntityExtension;
import dev.dubhe.anvilcraft.block.piston.IMoveableEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = PistonBaseBlock.class, priority = 943)
abstract class PistonBaseBlockMixin {
    @Unique
    private CompoundTag anvilcraft$nbt;

    @WrapOperation(
        method = "isPushable",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z")
    )
    private static boolean isPushable(BlockState instance, Operation<Boolean> original) {
        return original.call(instance) && !(instance.getBlock() instanceof IMoveableEntityBlock);
    }

    @Inject(
        method = "moveBlocks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock("
                     + "Lnet/minecraft/core/BlockPos;"
                     + "Lnet/minecraft/world/level/block/state/BlockState;"
                     + "I"
                     + ")Z",
            ordinal = 1
        )
    )
    private void setBlock(
        Level level, BlockPos pos, Direction facing, boolean extending, CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 2) BlockPos blockpos,
        @Local(ordinal = 1) Direction direction,
        @Local(ordinal = 1) List<BlockState> list1,
        @Local(ordinal = 1) int k
    ) {
        if (level.isClientSide()) return;
        this.anvilcraft$nbt = new CompoundTag();
        if (list1.get(k).getBlock() instanceof IMoveableEntityBlock block) {
            this.anvilcraft$nbt = block.clearData(level, blockpos.relative(direction.getOpposite()));
        }
    }

    @WrapOperation(
        method = "moveBlocks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity("
                     + "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;"
                     + "Lnet/minecraft/world/level/block/state/BlockState;"
                     + "Lnet/minecraft/core/Direction;ZZ"
                     + ")Lnet/minecraft/world/level/block/entity/BlockEntity;",
            ordinal = 0
        )
    )
    private BlockEntity newMovingBlockEntity(
        BlockPos pos,
        BlockState blockState,
        BlockState movedState,
        Direction direction,
        boolean extending,
        boolean isSourcePiston,
        Operation<BlockEntity> original
    ) {
        BlockEntity blockEntity = original.call(pos, blockState, movedState, direction, extending, isSourcePiston);
        if (blockEntity instanceof IPistonMovingBlockEntityExtension entity) {
            entity.anvilcraft$setData(this.anvilcraft$nbt);
        }
        return blockEntity;
    }
}
