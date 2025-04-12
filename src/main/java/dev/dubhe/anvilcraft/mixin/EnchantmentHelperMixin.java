package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.function.Consumer;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @WrapMethod(method = "updateEnchantments")
    private static ItemEnchantments checkMultiphase(ItemStack stack, Consumer<ItemEnchantments.Mutable> updater, Operation<ItemEnchantments> original) {
        ItemEnchantments result = original.call(stack, updater);
        if (stack.has(ModComponents.MULTIPHASE)) {
            Multiphase multiphase = Objects.requireNonNull(stack.get(ModComponents.MULTIPHASE));
            stack.set(ModComponents.MULTIPHASE, multiphase.applyEnchantments(result));
        }
        return result;
    }

    @Redirect(
        method = "setEnchantments",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;set(" +
                     "Lnet/minecraft/core/component/DataComponentType;" +
                     "Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T> T setToMultiphase(ItemStack instance, DataComponentType<? super T> type, T value) {
        if (value instanceof ItemEnchantments enchantments && instance.has(ModComponents.MULTIPHASE)) {
            Multiphase multiphase = Objects.requireNonNull(instance.get(ModComponents.MULTIPHASE));
            instance.set(ModComponents.MULTIPHASE, multiphase.applyEnchantments(enchantments));
        }
        return instance.set(type, value);
    }
}
