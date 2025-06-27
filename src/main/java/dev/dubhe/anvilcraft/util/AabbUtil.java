package dev.dubhe.anvilcraft.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.AABB;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AabbUtil {
    public static AABB centerSectionTo3x3x3(BlockPos pos) {
        return centerSectionTo3x3x3(SectionPos.of(pos));
    }

    public static AABB centerSectionTo3x3x3(SectionPos center) {
        return new AABB(
            center.minBlockX() - 16,
            center.minBlockY() - 16,
            center.minBlockZ() - 16,
            center.maxBlockX() + 16 + 1,
            center.maxBlockY() + 16 + 1,
            center.maxBlockZ() + 16 + 1
        );
    }
}
