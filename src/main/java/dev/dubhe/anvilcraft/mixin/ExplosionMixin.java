package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.api.IHasMultiBlock;
import dev.dubhe.anvilcraft.recipe.anvil.collision.BlockTransform;
import dev.dubhe.anvilcraft.util.BlockTransformExplosion;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

@Mixin(Explosion.class)
abstract class ExplosionMixin implements BlockTransformExplosion {

    @Unique
    public HashMap<Block, BlockTransform> anvilcraft$blockTransformMap = new HashMap<>();

    @Shadow
    @Final
    private Level level;

    @Shadow
    public abstract boolean interactsWithBlocks();

    @Shadow
    @Final
    private Explosion.BlockInteraction blockInteraction;

    @Inject(
            method = "finalizeExplosion",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V",
                    shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void finalizeExplosion(
            boolean pSpawnParticles,
            CallbackInfo ci,
            boolean flag,
            List<Pair<ItemStack, BlockPos>> list,
            ObjectListIterator<BlockPos> var4,
            BlockPos blockpos) {
        BlockState state = this.level.getBlockState(blockpos);
        Block block = state.getBlock();
        if (block instanceof IHasMultiBlock multiBlock) {
            multiBlock.onRemove(level, blockpos, state);
        }
    }

    @WrapOperation(
            method = "finalizeExplosion",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/world/level/block/state/BlockState;onExplosionHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;Ljava/util/function/BiConsumer;)V"
            )
    )
    private void anvilCraft$explosionBlockTransform(
            BlockState instance,
            Level level,
            BlockPos blockPos,
            Explosion explosion,
            BiConsumer<ItemStack, BlockPos> biConsumer,
            Operation<Void> original
    ) {
        BlockTransform blockTransform;
        if ((blockTransform = anvilcraft$blockTransformMap.get(instance.getBlock())) != null) {
            blockTransform.progress(level, blockPos);
        }
        original.call(instance, level, blockPos, explosion, biConsumer);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public void setBlockTransformExplosion(Collection<BlockTransform> blockTransformExplosions) {
        for (BlockTransform blockTransform : blockTransformExplosions) {
            if (blockTransform.inputBlock().getTag() == null) {
                anvilcraft$blockTransformMap.put(blockTransform.inputBlock().getBlock(), blockTransform);
            } else {
                for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(blockTransform.inputBlock().getTag())) {
                    anvilcraft$blockTransformMap.put(blockHolder.value(), blockTransform);
                }
            }
        }
    }
}
