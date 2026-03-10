package dev.dubhe.anvilcraft.util;

import com.google.common.collect.EvictingQueue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;

import java.util.List;

public class CodecUtil {
    /**
     * 基本等同于 {@link Codec#encodeStart(DynamicOps, Object)}。
     *
     * @param codec 编解码器
     * @param ops 操作集
     * @param input 需要编码的输入值
     * @param <T> 输入值的类型
     * @param <R> 编码后的类型
     * @return 编码结果
     */
    public static <T, R> DataResult<R> encodeStart(MapCodec<T> codec, DynamicOps<R> ops, T input) {
        return codec.encode(input, ops, codec.compressedBuilder(ops)).build(ops.emptyMap());
    }

    public static <T> MapCodec<EvictingQueue<T>> evictingQueueMapCodec(Codec<T> valueCodec) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT
                .fieldOf("max_size")
                .forGetter(queue -> queue.remainingCapacity() + queue.size()),
            valueCodec.listOf()
                .fieldOf("values")
                .forGetter(List::copyOf)
        ).apply(inst, (maxSize, values) -> Util.run(EvictingQueue.create(maxSize), queue -> values.forEach(queue::offer))));
    }

    public static <T> MapCodec<NonNullList<T>> nonNullListMapCodec(
        Codec<T> elementCodec,
        int size,
        String fieldName,
        String element,
        String thing,
        T defaultValue
    ) {
        return elementCodec
            .listOf(1, size)
            .fieldOf(fieldName)
            .flatXmap(
                tl -> {
                    T[] ta = Util.cast(tl.toArray());
                    if (ta.length == 0) {
                        return DataResult.error(() -> "No %1$s for %2$s".formatted(element, thing));
                    } else {
                        return ta.length > size
                               ? DataResult.error(() -> "Too many %1$s for %2$s. The maximum is: %3$d".formatted(element, thing, size))
                               : DataResult.success(NonNullList.of(defaultValue, ta));
                    }
                },
                DataResult::success
            );
    }
}
