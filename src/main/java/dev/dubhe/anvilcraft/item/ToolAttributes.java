package dev.dubhe.anvilcraft.item;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 特殊工具属性
 */
public interface ToolAttributes {
    record FireReforging() {
        public static final Codec<FireReforging> CODEC = Codec.unit(FireReforging::new);
        public static final StreamCodec<FriendlyByteBuf, FireReforging> STREAM_CODEC = StreamCodec.of(
            FireReforging::encode,
            FireReforging::decode
        );

        private static void encode(FriendlyByteBuf buf, FireReforging value) {}

        private static FireReforging decode(FriendlyByteBuf buf) {
            return new FireReforging();
        }
    }

    record Tough() {
        public static final Codec<Tough> CODEC = Codec.unit(Tough::new);
        public static final StreamCodec<FriendlyByteBuf, Tough> STREAM_CODEC = StreamCodec.of(
            Tough::encode,
            Tough::decode
        );

        private static void encode(FriendlyByteBuf buf, Tough value) {}

        private static Tough decode(FriendlyByteBuf buf) {
            return new Tough();
        }
    }

    record Morph() {
        public static final Codec<Morph> CODEC = Codec.unit(Morph::new);
        public static final StreamCodec<FriendlyByteBuf, Morph> STREAM_CODEC = StreamCodec.of(
            Morph::encode,
            Morph::decode
        );

        private static void encode(FriendlyByteBuf buf, Morph value) {}

        private static Morph decode(FriendlyByteBuf buf) {
            return new Morph();
        }
    }
}
