package dev.dubhe.anvilcraft.api.injection.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IPistonMovingBlockEntityExtension {
    default @Nullable CompoundTag anvilcraft$clearData() {
        throw new AssertionError();
    }

    default void anvilcraft$setData(@Nullable CompoundTag nbt) {
        throw new AssertionError();
    }

    default @Nullable BlockState anvilcraft$getMoveState() {
        throw new AssertionError();
    }
}
