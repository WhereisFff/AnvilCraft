package dev.dubhe.anvilcraft.api.tooltip.providers;

import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * 拥有作用范围的方块实体
 */
public interface IHasAffectRange {
    @Nullable
    AABB shape();
}
