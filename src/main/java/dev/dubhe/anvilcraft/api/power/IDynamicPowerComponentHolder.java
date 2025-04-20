package dev.dubhe.anvilcraft.api.power;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public interface IDynamicPowerComponentHolder {
    AABB anvilCraft$getPowerSupplyingBoundingBox();

    void anvilCraft$gridTick();

    DynamicPowerComponent anvilCraft$getPowerComponent();

    static IDynamicPowerComponentHolder of(ServerPlayer player) {
        return (IDynamicPowerComponentHolder) player;
    }
}
