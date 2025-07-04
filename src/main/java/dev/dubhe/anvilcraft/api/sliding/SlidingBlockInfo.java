package dev.dubhe.anvilcraft.api.sliding;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public record SlidingBlockInfo(int x, int y, int z, BlockState state, CompoundTag entityData) {
    public static final Codec<SlidingBlockInfo> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        Codec.INT.fieldOf("x").forGetter(SlidingBlockInfo::x),
        Codec.INT.fieldOf("y").forGetter(SlidingBlockInfo::y),
        Codec.INT.fieldOf("z").forGetter(SlidingBlockInfo::z),
        BlockState.CODEC.fieldOf("state").forGetter(SlidingBlockInfo::state),
        CompoundTag.CODEC.fieldOf("entityData").forGetter(SlidingBlockInfo::entityData)
    ).apply(ins, SlidingBlockInfo::new));
    public static final StreamCodec<ByteBuf, SlidingBlockInfo> SIMPLE_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SlidingBlockInfo::x,
        ByteBufCodecs.VAR_INT, SlidingBlockInfo::y,
        ByteBufCodecs.VAR_INT, SlidingBlockInfo::z,
        CodecUtil.BLOCK_STATE_STREAM_CODEC, SlidingBlockInfo::state,
        SlidingBlockInfo::new
    );
    public static final StreamCodec<ByteBuf, SlidingBlockInfo> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SlidingBlockInfo::x,
        ByteBufCodecs.VAR_INT, SlidingBlockInfo::y,
        ByteBufCodecs.VAR_INT, SlidingBlockInfo::z,
        CodecUtil.BLOCK_STATE_STREAM_CODEC, SlidingBlockInfo::state,
        ByteBufCodecs.COMPOUND_TAG, SlidingBlockInfo::entityData,
        SlidingBlockInfo::new
    );

    public SlidingBlockInfo(int x, int y, int z, BlockState state) {
        this(x, y, z, state, new CompoundTag());
    }

    public BlockPos getPos(BlockPos center) {
        return center.offset(this.x, this.y, this.z);
    }
}
