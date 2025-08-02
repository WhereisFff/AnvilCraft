package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.injection.block.state.IBlockStateExtension;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Getter
public class ChanceBlockState {
    private final BlockState state;
    private final double chance;

    public ChanceBlockState(BlockState state, double chance) {
        this.state = state;
        this.chance = chance;
    }

    public static final MapCodec<ChanceBlockState> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            IBlockStateExtension.MAP_CODEC
                .forGetter(ChanceBlockState::getState),
            Codec.DOUBLE.optionalFieldOf("chance", 1.0)
                .forGetter(ChanceBlockState::getChance)
        ).apply(instance, ChanceBlockState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceBlockState> STREAM_CODEC = StreamCodec.of(ChanceBlockState::encode, ChanceBlockState::decode);

    public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ChanceBlockState setBlock) {
        RegistryOps<Tag> ops = HolderLookup.Provider
            .create(Stream.of(BuiltInRegistries.BLOCK.asLookup()))
            .createSerializationContext(NbtOps.INSTANCE);
        DataResult<Tag> encode = BlockState.CODEC.encode(setBlock.state, NbtOps.INSTANCE, ops.empty());
        Tag tag = encode.getOrThrow();
        buf.writeNbt(tag);
        buf.writeDouble(setBlock.chance);
    }

    public static @NotNull ChanceBlockState decode(@NotNull RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.BLOCK.asLookup())).createSerializationContext(NbtOps.INSTANCE);
        BlockState blockState = BlockState.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst();
        double chance = buf.readDouble();
        return new ChanceBlockState(blockState, chance);
    }
}
