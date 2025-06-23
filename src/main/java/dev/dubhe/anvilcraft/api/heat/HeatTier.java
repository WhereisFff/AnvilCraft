package dev.dubhe.anvilcraft.api.heat;

import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public enum HeatTier implements Comparable<HeatTier> {
    NORMAL(0, 4, 0),
    HEATED(4, 12, 4),
    REDHOT(12, 32, 16),
    GLOWING(32, 80, 64),
    INCANDESCENT(80, Integer.MAX_VALUE, 128),
    ;

    public static final Codec<HeatTier> INDEX_CODEC = CodecUtil.enumCodecInInt(HeatTier.class);
    public static final Codec<HeatTier> LOWER_NAME_CODEC = CodecUtil.enumCodecInLowerName(HeatTier.class);
    public static final StreamCodec<ByteBuf, HeatTier> STREAM_CODEC = CodecUtil.enumStreamCodec(HeatTier.class);

    @Getter
    private final int remainCount;
    @Getter
    private final int toNextCount;
    @Getter
    private final int powerProduce;

    HeatTier(int remainCount, int toNextCount, int powerProduce) {
        this.remainCount = remainCount;
        this.toNextCount = toNextCount;
        this.powerProduce = powerProduce;
    }

    public Component toComponent() {
        return Component.translatable("tooltip.anvilcraft.heat.tier." + this);
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
