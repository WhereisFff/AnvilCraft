package dev.dubhe.anvilcraft.util;

import net.minecraft.world.inventory.DataSlot;

public class MenuUtil {
    public static DataSlot standalone(int initValue) {
        return new DataSlot() {
            private int value = initValue;

            @Override
            public int get() {
                return this.value;
            }

            @Override
            public void set(int value) {
                this.value = value;
            }
        };
    }
}
