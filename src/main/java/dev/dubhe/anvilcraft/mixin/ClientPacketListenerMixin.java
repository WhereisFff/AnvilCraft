package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Redirect(
        method = "findTotem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
        )
    )
    private static boolean alsoCheckAmuletBox(ItemStack instance, Item item) {
        return instance.is(item) || instance.is(ModItems.AMULET_BOX);
    }
    
    @Inject(
        method = "findTotem",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void returnCorrect(Player player, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result.is(ModItems.AMULET_BOX)) {
            cir.setReturnValue(
                Optional.of(InventoryUtil.getFirstItem(player.getInventory(), Items.TOTEM_OF_UNDYING))
                    .filter(stack -> stack != ItemStack.EMPTY)
                    .orElse(result)
            );
        }
    }
}
