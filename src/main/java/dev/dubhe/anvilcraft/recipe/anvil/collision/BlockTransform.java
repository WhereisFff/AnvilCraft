package dev.dubhe.anvilcraft.recipe.anvil.collision;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.elements.InputBlock;
import dev.dubhe.anvilcraft.recipe.elements.OutputBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public record BlockTransform(
    InputBlock inputBlock,
    OutputBlock outputBlock,
    float chance,
    int maxCount
) {
    public static final Codec<BlockTransform> CODEC = RecordCodecBuilder.create(it -> it.group(
            InputBlock.CODEC.fieldOf("input").forGetter(BlockTransform::inputBlock),
            OutputBlock.CODEC.fieldOf("output").forGetter(BlockTransform::outputBlock),
            Codec.FLOAT.fieldOf("chance").forGetter(BlockTransform::chance),
            Codec.INT.fieldOf("max_count").forGetter(BlockTransform::maxCount)
        ).apply(it, BlockTransform::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTransform> STREAM_CODEC = StreamCodec.of(
        BlockTransform::encode, BlockTransform::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, BlockTransform blockTransform) {
        InputBlock.STREAM_CODEC.encode(buf, blockTransform.inputBlock);
        OutputBlock.STREAM_CODEC.encode(buf, blockTransform.outputBlock);
        buf.writeFloat(blockTransform.chance);
        buf.writeVarInt(blockTransform.maxCount);
    }

    private static BlockTransform decode(RegistryFriendlyByteBuf buf) {
        return new BlockTransform(
            InputBlock.STREAM_CODEC.decode(buf),
            OutputBlock.STREAM_CODEC.decode(buf),
            buf.readFloat(),
            buf.readVarInt()
        );
    }

    public Boolean progress(Level level, BlockPos pos) {
        Pair<BlockState, CompoundTag> output;
        if (inputBlock.is(level.getBlockState(pos)) && (output = outputBlock.getResult(level.random)) != null) {
            if (chance < level.random.nextFloat()) return false;
            level.setBlockAndUpdate(pos, output.getFirst());
            Optional.ofNullable(level.getBlockEntity(pos))
                .ifPresent(be -> be.loadCustomOnly(output.getSecond(), level.registryAccess()));
            return true;
        }
        return false;
    }
}
