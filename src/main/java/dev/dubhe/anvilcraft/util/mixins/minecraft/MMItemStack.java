package dev.dubhe.anvilcraft.util.mixins.minecraft;

import dev.dubhe.anvilcraft.item.IntrinsicEnchantedItem;
import lombok.experimental.UtilityClass;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@UtilityClass
public class MMItemStack {
    public static boolean intrinsicEnch(ItemStack self, boolean original) {
        return original || self.getItem() instanceof IntrinsicEnchantedItem item && !item.intrinsicEnchantments(self).isEmpty();
    }
}