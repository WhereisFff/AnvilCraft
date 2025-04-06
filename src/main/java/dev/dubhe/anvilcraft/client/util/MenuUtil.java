package dev.dubhe.anvilcraft.client.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MenuUtil {
    /**
     * 使用传入的参数构建一个新的DataSlot
     *
     * @param getter 在{@link DataSlot#get()}方法内调用
     * @param setter 在{@link DataSlot#set(int)}方法内调用
     * @return 一个新的DataSlot
     */
    public static DataSlot newDataSlot(Supplier<Integer> getter, Consumer<Integer> setter) {
        return new DataSlot() {
            @Override
            public int get() {
                return getter.get();
            }

            @Override
            public void set(int value) {
                setter.accept(value);
            }
        };
    }

    /**
     * 将{@code slotInfos}中的{@link Pair}使用{@link MenuUtil#newDataSlot(Supplier, Consumer)}包装，
     * 调用{@code actionAddSingle}并传入包装后的{@link DataSlot}
     *
     * @param actionAddSingle 添加单个{@link DataSlot}的操作，一般为{@link AbstractContainerMenu#addDataSlot(DataSlot)}
     * @param slotInfos 一些参数Pair，每个Pair都存储了{@link MenuUtil#newDataSlot(Supplier, Consumer)}的两个参数
     *
     * @see MenuUtil#newDataSlot(Supplier, Consumer)
     * @see MenuUtil#addDataSlots(Consumer, DataSlot...)
     */
    @SafeVarargs
    @SuppressWarnings("JavadocReference")
    public static void addDataSlots(Consumer<DataSlot> actionAddSingle, Pair<Supplier<Integer>, Consumer<Integer>>... slotInfos) {
        for (Pair<Supplier<Integer>, Consumer<Integer>> slotInfo : slotInfos) {
            actionAddSingle.accept(newDataSlot(slotInfo.getFirst(), slotInfo.getSecond()));
        }
    }

    /**
     * 调用{@code actionAddSingle}并传入{@code slots}中的{@link DataSlot}
     *
     * @param actionAddSingle 添加单个{@link DataSlot}的操作，一般为{@link AbstractContainerMenu#addDataSlot(DataSlot)}
     * @param slots 一些{@link DataSlot}
     *
     * @see MenuUtil#addDataSlots(Consumer, Pair...)
     */
    @SuppressWarnings("JavadocReference")
    public static void addDataSlots(Consumer<DataSlot> actionAddSingle, DataSlot... slots) {
        for (DataSlot slot : slots) {
            actionAddSingle.accept(slot);
        }
    }
}
