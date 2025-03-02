package dev.dubhe.anvilcraft.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface IInventoryCarriedAware {
    void onCarriedUpdate(ItemStack itemStack, ServerPlayer serverPlayer);
}
