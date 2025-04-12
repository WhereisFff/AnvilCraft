package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.init.ModBlocks;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin {
    @Inject(method = "isValidBlock", at = @At("HEAD"), cancellable = true)
    private void voj(@NotNull BlockState state, @NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(state.is(ModBlocks.GIANT_ANVIL.get()) || state.is(BlockTags.ANVIL));
    }

    @Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;"
                     + "set(Lnet/minecraft/core/component/DataComponentType;"
                     + "Ljava/lang/Object;)Ljava/lang/Object;",
            ordinal = 0))
    private <T> T setToMultiphase(ItemStack instance, DataComponentType<? super T> component, T value) {
        if (value instanceof Component name && instance.has(ModComponents.MULTIPHASE)) {
            Multiphase multiphase = Objects.requireNonNull(instance.get(ModComponents.MULTIPHASE));
            instance.set(ModComponents.MULTIPHASE, multiphase.applyCustomName(name));
        }
        return instance.set(component, value);
    }

    @Redirect(
        method = "createResult",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private <T> T setToMultiphaseEmpty(ItemStack instance, DataComponentType<? extends T> component) {
        if (instance.has(ModComponents.MULTIPHASE)) {
            Multiphase multiphase = Objects.requireNonNull(instance.get(ModComponents.MULTIPHASE));
            Component name = instance.get(DataComponents.ITEM_NAME);
            if (name == null || name.equals(Component.empty())) name = instance.getItem().getDescription();
            instance.set(ModComponents.MULTIPHASE, multiphase.applyCustomName(name));
        }
        return instance.remove(component);
    }
}
