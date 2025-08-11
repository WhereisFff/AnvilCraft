package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.injection.block.state.IBlockStateExtension;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SetBlock;
import dev.dubhe.anvilcraft.util.CodecUtil;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;

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

    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceBlockState> STREAM_CODEC = StreamCodec.composite(
        BlockState.STREAM_CODEC,
        ChanceBlockState::getState,
        RecipeUtil.NUMBER_PROVIDER_STREAM_CODEC,
        ChanceBlockState::getChance,
        ChanceBlockState::new
    );

    public SetBlock toSetBlock(Vec3 offset) {
        return SetBlock.builder().block(this.getState()).offset(offset).chance(this.chance).build();
    }
}
