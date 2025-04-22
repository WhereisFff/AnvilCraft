package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.item.IonoCraftBackpackItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {
    //飘升机背包飞行时无挖掘惩罚
    @Redirect(method = "getDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean redirectOnGroundCheck(Player player) {
        boolean noDiggingPenalty = !IonoCraftBackpackItem.getByPlayer(player).isEmpty() && player.getAbilities().flying;
        return noDiggingPenalty || player.onGround();
    }
}
