package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.entity.LevitatingBlockEntity;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.util.GravityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlock.class)
public abstract class FallingBlockMixin extends Block {

    public FallingBlockMixin(Properties properties) {
        super(properties);
    }

    @Shadow
    public static boolean isFree(BlockState state) {
        throw new AssertionError();
    }

    @Inject(
        method = "tick",
        at = @At("HEAD"),
        cancellable = true
    )
    private void anvilcraft$SmartFallingLogic(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // 获取重力方向和大小
        GravityManager.GravityType gravityType = GravityManager.getFallingBlockGravityType(state.getBlock());
        double gravity = (gravityType == GravityManager.GravityType.ANTI_GRAVITY) ? 0.04 : -0.04;

        // G取0.04计算重力大小
        Vec3 gravityVector = GravityManager.GravitySourceManager.calculateGravityVector(level, Vec3.atCenterOf(pos), 0.04);

        // 漂浮粉块反向重力
        if (gravityType == GravityManager.GravityType.ANTI_GRAVITY) {
            gravityVector = gravityVector.reverse();
        }

        // 计算合力 Y 分量
        double gravityVectorY = gravity + gravityVector.y;

        // 如果合力向上，检查上方是否为空；如果合力向下，检查下方是否为空
        Direction direction = gravityVectorY > 0 ? Direction.UP : Direction.DOWN;
        BlockPos blockPos = pos.relative(direction);

        // 检查目标位置是否可以让方块移动
        if (isFree(level.getBlockState(blockPos))) {
            if (state.is(ModBlocks.LEVITATION_POWDER_BLOCK.get())) {
                LevitatingBlockEntity.levitate(level, pos, state);
            } else {
                FallingBlockEntity.fall(level, pos, state);
            }
            ci.cancel();
        } else {
            ci.cancel();
        }
    }
}