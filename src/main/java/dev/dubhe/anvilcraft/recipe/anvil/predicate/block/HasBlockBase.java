package dev.dubhe.anvilcraft.recipe.anvil.predicate.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.anvil.cache.BlockCache;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
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
        ServerLevel level = context.getLevel();
        return predicate.test(level, cache, blockPos);
    }

    public abstract static class AbstractType<T extends HasBlockBase<T>> implements IRecipePredicate.Type<T> {
        public final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(T::getOffset),
                BlockStatePredicate.CODEC.fieldOf("predicate").forGetter(T::getPredicate)
            ).apply(instance, this::of)
        );
        public final StreamCodec<RegistryFriendlyByteBuf, T> mapCodec = StreamCodec.composite(
            RecipeUtil.VEC3_STREAM_CODEC,
            T::getOffset,
            BlockStatePredicate.STREAM_CODEC,
            T::getPredicate,
            this::of
        );

        public abstract T of(Vec3 offset, BlockStatePredicate predicate);

        @Override
        public @NotNull MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.mapCodec;
        }
    }
}
