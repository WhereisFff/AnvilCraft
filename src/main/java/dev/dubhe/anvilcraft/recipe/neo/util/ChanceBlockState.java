package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.injection.block.state.IBlockStateExtension;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SetBlock;
import dev.dubhe.anvilcraft.util.CodecUtil;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Getter
public class ChanceBlockState {
    private final BlockState state;
    private final NumberProvider chance;

    public ChanceBlockState(BlockState state, NumberProvider chance) {
        this.state = state;
        this.chance = chance;
    }

    public ChanceBlockState(BlockState state, float chance) {
        this.state = state;
        this.chance = ConstantValue.exactly(chance);
    }

    public static final MapCodec<ChanceBlockState> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            IBlockStateExtension.MAP_CODEC
                .forGetter(ChanceBlockState::getState),
            CodecUtil.NUMBER_PROVIDER_CODEC
                .optionalFieldOf("chance", ConstantValue.exactly(1.0f))
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
        RecipeUtil.toNetwork(buf, setBlock.chance);
    }

    public static @NotNull ChanceBlockState decode(@NotNull RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.BLOCK.asLookup())).createSerializationContext(NbtOps.INSTANCE);
        BlockState blockState = BlockState.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst();
        NumberProvider chance = RecipeUtil.fromNetwork(buf);
        return new ChanceBlockState(blockState, chance);
    }

    public SetBlock toSetBlock(Vec3 offset) {
        return SetBlock.builder().block(this.getState()).offset(offset).chance(this.chance).build();
    }
}
