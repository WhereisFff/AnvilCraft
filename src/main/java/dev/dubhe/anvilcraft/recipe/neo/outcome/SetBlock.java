package dev.dubhe.anvilcraft.recipe.neo.outcome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeOutcomeTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockCache;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Getter
public class SetBlock implements IRecipeOutcome<SetBlock> {
    private final BlockState state;
    private final Vec3 offset;
    private final double chance;

    public SetBlock(BlockState state, Vec3 offset, double chance) {
        this.state = state;
        this.offset = offset;
        this.chance = chance;
    }

    @Override
    public Type getType() {
        return ModRecipeOutcomeTypes.SET_BLOCK.get();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        BlockCache cache = context.computeIfAbsent(BlockCache.BLOCK_CACHE);
        cache.setBlock(BlockPos.containing(context.getPos().add(this.offset)), this.state);
        context.putAcceptor(BlockCache.BLOCK_CACHE.location(), BlockCache.DEFAULT_ACCEPTOR);
    }

    public static class Type implements IRecipeOutcome.Type<SetBlock> {
        private static final MapCodec<SetBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                BlockState.CODEC.fieldOf("state")
                    .forGetter(SetBlock::getState),
                Vec3.CODEC.fieldOf("offset")
                    .forGetter(SetBlock::getOffset),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0)
                    .forGetter(SetBlock::getChance)
            ).apply(instance, SetBlock::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SetBlock> STREAM_CODEC = StreamCodec.of(Type::encode, Type::decode);

        @Override
        public @NotNull MapCodec<SetBlock> codec() {
            return Type.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, SetBlock> streamCodec() {
            return Type.STREAM_CODEC;
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull SetBlock setBlock) {
            RegistryOps<Tag> ops = HolderLookup.Provider
                .create(Stream.of(BuiltInRegistries.BLOCK.asLookup()))
                .createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> encode = BlockState.CODEC.encode(setBlock.state, NbtOps.INSTANCE, ops.empty());
            Tag tag = encode.getOrThrow();
            buf.writeNbt(tag);
            buf.writeVec3(setBlock.offset);
            buf.writeDouble(setBlock.chance);
        }

        public static @NotNull SetBlock decode(@NotNull RegistryFriendlyByteBuf buf) {
            RegistryOps<Tag> ops = HolderLookup.Provider.create(Stream.of(BuiltInRegistries.BLOCK.asLookup())).createSerializationContext(NbtOps.INSTANCE);
            BlockState blockState = BlockState.CODEC.decode(ops, buf.readNbt()).getOrThrow().getFirst();
            Vec3 vec3 = buf.readVec3();
            double chance = buf.readDouble();
            return new SetBlock(blockState, vec3, chance);
        }
    }
}
