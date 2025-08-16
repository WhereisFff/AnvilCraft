package dev.dubhe.anvilcraft.recipe.anvil.outcome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.injection.block.state.IBlockStateExtension;
import dev.dubhe.anvilcraft.init.ModRecipeOutcomeTypes;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.anvil.cache.BlockCache;
import dev.dubhe.anvilcraft.util.CodecUtil;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class SetBlock implements IRecipeOutcome<SetBlock> {
    private final BlockState state;
    private final Vec3 offset;
    private final NumberProvider chance;

    public SetBlock(BlockState state, Vec3 offset, NumberProvider chance) {
        this.state = state;
        this.offset = offset;
        this.chance = chance;
    }

    public static @NotNull Builder builder() {
        return new Builder();
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
                IBlockStateExtension.MAP_CODEC
                    .forGetter(SetBlock::getState),
                Vec3.CODEC
                    .fieldOf("offset")
                    .forGetter(SetBlock::getOffset),
                CodecUtil.NUMBER_PROVIDER_CODEC
                    .optionalFieldOf("chance", ConstantValue.exactly(1.0f))
                    .forGetter(SetBlock::getChance)
            ).apply(instance, SetBlock::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SetBlock> STREAM_CODEC = StreamCodec.composite(
            BlockState.STREAM_CODEC,
            SetBlock::getState,
            RecipeUtil.VEC3_STREAM_CODEC,
            SetBlock::getOffset,
            RecipeUtil.NUMBER_PROVIDER_STREAM_CODEC,
            SetBlock::getChance,
            SetBlock::new
        );

        @Override
        public @NotNull MapCodec<SetBlock> codec() {
            return Type.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, SetBlock> streamCodec() {
            return Type.STREAM_CODEC;
        }
    }

    public static class Builder {
        private BlockState state = Blocks.AIR.defaultBlockState();
        private Vec3 offset = Vec3.ZERO;
        private NumberProvider chance = ConstantValue.exactly(1.0f);

        public Builder offset(Vec3 offset) {
            this.offset = offset;
            return this;
        }

        public Builder offset(double x, double y, double z) {
            this.offset = new Vec3(x, y, z);
            return this;
        }

        public Builder below(double below) {
            return this.offset(Vec3.ZERO.subtract(0, below, 0));
        }

        public Builder below() {
            return this.below(1);
        }

        public Builder above(double above) {
            return this.offset(Vec3.ZERO.add(0, above, 0));
        }

        public Builder above() {
            return this.above(1);
        }

        public Builder chance(NumberProvider chance) {
            this.chance = chance;
            return this;
        }

        public Builder chance(float chance) {
            return this.chance(ConstantValue.exactly(chance));
        }

        public Builder block(BlockState state) {
            this.state = state;
            return this;
        }

        public Builder block(@NotNull Block block) {
            this.state = block.defaultBlockState();
            return this;
        }

        public SetBlock build() {
            return new SetBlock(state, offset, chance);
        }
    }
}
