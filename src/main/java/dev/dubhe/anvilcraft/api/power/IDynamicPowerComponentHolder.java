package dev.dubhe.anvilcraft.api.power;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public interface IDynamicPowerComponentHolder {
    AABB anvilcraft$getPowerSupplyingBoundingBox();

    void anvilcraft$gridTick();

    DynamicPowerComponent anvilcraft$getPowerComponent();

    static IDynamicPowerComponentHolder of(ServerPlayer player) {
        return (IDynamicPowerComponentHolder) player;
    }
}
