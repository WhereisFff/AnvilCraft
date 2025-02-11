package dev.dubhe.anvilcraft.item;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 特殊工具属性
 */
public interface ToolAttributes {
    record FireReforging() {
        public static final FireReforging INSTANCE = new FireReforging();
        public static final Codec<FireReforging> CODEC = Codec.unit(FireReforging::new);
        public static final StreamCodec<FriendlyByteBuf, FireReforging> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }

    record Tough() {
        public static final Tough INSTANCE = new Tough();
        public static final Codec<Tough> CODEC = Codec.unit(Tough::new);
        public static final StreamCodec<FriendlyByteBuf, Tough> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }

    record Morph() {
        public static final Morph INSTANCE = new Morph();
        public static final Codec<Morph> CODEC = Codec.unit(Morph::new);
        public static final StreamCodec<FriendlyByteBuf, Morph> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }
}
