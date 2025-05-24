package dev.dubhe.anvilcraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;

public class ThrownHeavyHalberdEntity extends ThrownTrident {
    public ThrownHeavyHalberdEntity(
        EntityType<? extends ThrownTrident> entityType, Level level) {
        super(entityType, level);
    }
}
