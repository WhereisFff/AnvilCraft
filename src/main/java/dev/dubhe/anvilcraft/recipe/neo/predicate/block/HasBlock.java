package dev.dubhe.anvilcraft.recipe.neo.predicate.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class HasBlock implements IRecipePredicate<HasBlock> {
    private final Vec3 offset;
    private final BlockStatePredicate predicate;

    public HasBlock(Vec3 offset, BlockStatePredicate predicate) {
        this.offset = offset;
        this.predicate = predicate;
    }

    @Override
    public boolean test(@NotNull InWorldRecipeContext inWorldRecipeContext) {
        Vec3 pos = inWorldRecipeContext.getPos().add(this.offset);
        BlockPos blockPos = BlockPos.containing(pos);
        BlockState blockState = inWorldRecipeContext.getLevel().getBlockState(blockPos);
        return predicate.test(blockState);
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_BLOCK.get();
    }

    public static class Type implements IRecipePredicate.Type<HasBlock> {
        public static final MapCodec<HasBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(HasBlock::getOffset),
                BlockStatePredicate.CODEC.fieldOf("predicate").forGetter(HasBlock::getPredicate)
            ).apply(instance, HasBlock::new)
        );

        @Override
        public @NotNull MapCodec<HasBlock> codec() {
            return Type.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, HasBlock> streamCodec() {
            return StreamCodec.of(Type::encode, Type::decode);
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull HasBlock hasBlock) {
            buf.writeVec3(hasBlock.getOffset());
            BlockStatePredicate.encode(buf, hasBlock.getPredicate());
        }

        public static @NotNull HasBlock decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new HasBlock(buf.readVec3(), BlockStatePredicate.decode(buf));
        }
    }
}
