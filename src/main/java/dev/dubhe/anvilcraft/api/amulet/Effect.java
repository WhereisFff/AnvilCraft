package dev.dubhe.anvilcraft.api.amulet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

public class Effect {
    private final InventoryTick inventoryTick;
    private final ImmuneDamage immuneDamage;

    public Effect(InventoryTick inventoryTick, ImmuneDamage immuneDamage) {
        this.inventoryTick = inventoryTick;
        this.immuneDamage = immuneDamage;
    }

    public void inventoryTick(ServerPlayer player, ItemStack amulet, Boolean isEnabled) {
        this.inventoryTick.inventoryTick(player, amulet, isEnabled);
    }

    public boolean shouldImmuneDamage(ServerPlayer player, DamageSource source) {
        return this.immuneDamage.shouldImmuneDamage(player, source);
    }
}
