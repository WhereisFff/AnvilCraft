package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.Consumer;

public class EnchantmentUtil {
    public static void updateEnchantmentsForMultiphase(
        ItemStack stack, Consumer<ItemEnchantments.Mutable> updater
    ) {
        Multiphase multiphase = stack.get(ModComponents.MULTIPHASE);
        assert multiphase != null;
        ItemEnchantments enchantments = multiphase.getEnchantments();
        if (enchantments != null) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
            updater.accept(mutable);
            ItemEnchantments result = mutable.toImmutable();
            stack.set(ModComponents.MULTIPHASE, multiphase.applyEnchantments(result));
            stack.set(DataComponents.ENCHANTMENTS, result);
        }
    }
}
