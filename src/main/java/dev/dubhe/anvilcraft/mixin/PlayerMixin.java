package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.dubhe.anvilcraft.item.IonoCraftBackpackItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    //飘升机背包飞行时无挖掘惩罚
    @ModifyExpressionValue(method = "getDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean modifyOnGround(boolean original) {
        Player player = (Player)(Object)this;
        boolean noDiggingPenalty = !IonoCraftBackpackItem.getByPlayer(player).isEmpty() && player.getAbilities().flying;
        return noDiggingPenalty || original;
    }
}