package dev.dubhe.anvilcraft.recipe.neo.predicate.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockCache;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class HasBlockBase<T extends HasBlockBase<T>> implements IRecipePredicate<T> {
    protected final Vec3 offset;
    protected final BlockStatePredicate predicate;

    public HasBlockBase(Vec3 offset, BlockStatePredicate predicate) {
        this.offset = offset;
        this.predicate = predicate;
    }

    @Override
    public boolean test(@NotNull InWorldRecipeContext context) {
        Vec3 pos = context.getPos().add(this.offset);
        BlockPos blockPos = BlockPos.containing(pos);
        BlockCache cache = context.computeIfAbsent(BlockCache.BLOCK_CACHE);
        BlockState blockState = cache.getBlockState(blockPos);
        return predicate.test(blockState);
    }

    public abstract static class AbstractType<T extends HasBlockBase<T>> implements IRecipePredicate.Type<T> {
        public final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(T::getOffset),
                BlockStatePredicate.CODEC.fieldOf("predicate").forGetter(T::getPredicate)
            ).apply(instance, this::of)
        );
        public final StreamCodec<RegistryFriendlyByteBuf, T> mapCodec = StreamCodec.of(this::encode, this::decode);

        public abstract T of(Vec3 offset, BlockStatePredicate predicate);

        @Override
        public @NotNull MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.mapCodec;
        }

        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull T hasBlock) {
            buf.writeVec3(hasBlock.getOffset());
            BlockStatePredicate.encode(buf, hasBlock.getPredicate());
        }

        public @NotNull T decode(@NotNull RegistryFriendlyByteBuf buf) {
            return this.of(buf.readVec3(), BlockStatePredicate.decode(buf));
        }
    }
}
