package dev.dubhe.anvilcraft.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import dev.dubhe.anvilcraft.block.entity.BatchCrafterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PackagerBlockEntity.class)
@Debug(export = true)
abstract class PackagerBlockEntityMixin {
    @ModifyVariable(method = "unwrapBox", at = @At("STORE"), ordinal = 1)
    private boolean injected(boolean targetIsCrafter, @Local BlockEntity targetBE) {
        return targetIsCrafter || targetBE instanceof BatchCrafterBlockEntity;
    }
}
