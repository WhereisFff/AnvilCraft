package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Shadow
    @Nullable
    public abstract <T> T set(DataComponentType<? super T> component, @Nullable T value);

    @Inject(method = "set", at = @At("TAIL"))
    private <T> void setForMultiphase(DataComponentType<? super T> component, T value, CallbackInfoReturnable<T> cir) {
        if (!this.has(ModComponents.MULTIPHASE)) return;
        Multiphase multiphase = this.get(ModComponents.MULTIPHASE);
        if (multiphase == null) return;
        if (component.equals(DataComponents.CUSTOM_NAME) && value instanceof Component customName) {
            this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.alpha().withCustomName(customName)));
        } else if (component.equals(DataComponents.ITEM_NAME) && value instanceof Component itemName) {
            this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.alpha().withItemName(itemName)));
        } else if (component.equals(DataComponents.REPAIR_COST) && value instanceof Integer repairCost) {
            this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.alpha().withRepairCost(repairCost)));
        } else if (
            component.equals(EnchantmentHelper.getComponentType(Util.cast(this))) && value instanceof ItemEnchantments enchantments
        ) {
            this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.alpha().withEnchantments(enchantments)));
        }
    }
}
