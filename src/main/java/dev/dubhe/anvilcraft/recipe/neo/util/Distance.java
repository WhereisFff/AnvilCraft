package dev.dubhe.anvilcraft.recipe.neo.util;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record Distance(Type type, int distance, boolean isHorizontal) {
    public static final Distance DEFAULT = new Distance(Type.MANHATTAN, 1, true);
    public static final Codec<Distance> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        Type.LOWER_NAME_CODEC.fieldOf("type").forGetter(Distance::type),
        Codec.INT.fieldOf("distance").forGetter(Distance::distance),
        Codec.BOOL.fieldOf("isHorizontal").forGetter(Distance::isHorizontal)
    ).apply(ins, Distance::new));
    public static final StreamCodec<ByteBuf, Distance> STREAM_CODEC = StreamCodec.composite(
        Type.STREAM_CODEC, Distance::type,
        ByteBufCodecs.VAR_INT, Distance::distance,
        ByteBufCodecs.BOOL, Distance::isHorizontal,
        Distance::new
    );

    public boolean isInRange(Vec3 original, Vec3 other) {
        Vec3 dV = original.subtract(other);
        return switch (this.type) {
            case EUCLIDEAN -> dV.x * dV.x + dV.z * dV.z + (this.isHorizontal ? 0 : dV.y * dV.y) < this.distance * this.distance;
            case MANHATTAN -> Math.abs(dV.x) + Math.abs(dV.z) + (this.isHorizontal ? 0 : Math.abs(dV.y)) < this.distance;
            case CHEBYSHEV -> (this.isHorizontal ? Math.max(dV.x, dV.z) : Math.max(Math.max(dV.x, dV.z), dV.y)) < this.distance;
        };
    }

    public Iterable<BlockPos> getAllPosesInRange(Vec3 centerPos) {
        final BlockPos center = BlockPos.containing(centerPos.x, centerPos.y, centerPos.z);
        return switch (this.type) {
            case EUCLIDEAN -> () -> new AbstractIterator<>() {
                private final int radiusSq = distance * distance;

                private int x = -distance;
                private int y = -distance;
                private int z = -distance - 1;

                @Override
                protected BlockPos computeNext() {
                    while (x <= distance) {
                        while (y <= distance) {
                            while (++z <= distance) {
                                if (x * x + y * y + z * z <= radiusSq) {
                                    return center.offset(x, y, z);
                                }
                            }
                            z = -distance - 1;
                            y++;
                        }
                        y = -distance;
                        x++;
                    }
                    return endOfData();
                }
            };
            case MANHATTAN -> BlockPos.withinManhattan(center, this.distance, this.distance, this.distance);
            case CHEBYSHEV -> BlockPos.betweenClosed(
                center.offset(-this.distance, -this.distance, -this.distance),
                center.offset(this.distance, this.distance, this.distance));
        };
    }

    public enum Type {
        EUCLIDEAN, MANHATTAN, CHEBYSHEV
        ;

        public static final Codec<Type> LOWER_NAME_CODEC = CodecUtil.enumCodecInLowerName(Type.class);
        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = CodecUtil.enumStreamCodec(Type.class);
    }
}
