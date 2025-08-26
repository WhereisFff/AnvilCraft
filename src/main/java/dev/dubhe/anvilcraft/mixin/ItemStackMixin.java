package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.item.MultitoolItem;
import dev.dubhe.anvilcraft.item.ResonatorItem;
import dev.dubhe.anvilcraft.item.property.component.Multiphase;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
        switch (value) {
            case Component customName when component.equals(DataComponents.CUSTOM_NAME) ->
                this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.peekFirst().withCustomName(customName)));
            case Component itemName when component.equals(DataComponents.ITEM_NAME) ->
                this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.peekFirst().withItemName(itemName)));
            case Integer repairCost when component.equals(DataComponents.REPAIR_COST) ->
                this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.peekFirst().withRepairCost(repairCost)));
            case ItemEnchantments enchantments when component.equals(EnchantmentHelper.getComponentType(Util.cast(this))) ->
                this.set(ModComponents.MULTIPHASE, multiphase.withAlpha(multiphase.peekFirst().withEnchantments(enchantments)));
            default -> {
            }
        }
    }

    @WrapOperation(
        method = "is(Lnet/minecraft/tags/TagKey;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder$Reference;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private boolean tryUseResonatorVer1(Holder.Reference<Item> instance, TagKey<Item> tagKey, Operation<Boolean> original) {
        if (instance instanceof ResonatorItem.ResonatorHolder holder) {
            return holder.is(ResonatorItem.getMode(Util.cast(this)), tagKey);
        } else if (instance instanceof MultitoolItem.MultitoolHolder holder) {
            return holder.is(MultitoolItem.getMode(Util.cast(this)), tagKey);
        }
        return original.call(instance, tagKey);
    }

    @WrapOperation(
        method = "is(Lnet/minecraft/core/HolderSet;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderSet;contains(Lnet/minecraft/core/Holder;)Z")
    )
    private boolean tryUseResonatorVer2(HolderSet<Item> instance, Holder<Item> tHolder, Operation<Boolean> original) {
        if (instance instanceof MultitoolItem.MultitoolHolder holder) {
            return holder.is(MultitoolItem.getMode(Util.cast(this)), instance);
        } else if (instance instanceof ResonatorItem.ResonatorHolder holder) {
            return holder.is(ResonatorItem.getMode(Util.cast(this)), instance);
        }
        return original.call(instance, tHolder);
    }
}
