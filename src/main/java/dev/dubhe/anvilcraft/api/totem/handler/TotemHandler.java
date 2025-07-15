package dev.dubhe.anvilcraft.api.totem.handler;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface TotemHandler {
    boolean execute(DamageSource damageSource, LivingEntity entity, ItemStack totemItem);
    void shrink(ItemStack totemItem);
}
