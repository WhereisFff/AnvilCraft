package dev.dubhe.anvilcraft.recipe.neo.predicate.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockCache;
import dev.dubhe.anvilcraft.recipe.neo.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.util.CauldronUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class HasCauldron extends HasBlockBase<HasCauldron> {
    public static final ResourceLocation EMPTY = ResourceLocation.withDefaultNamespace("empty");
    private final ResourceLocation fluid;

    public HasCauldron(Vec3 offset, ResourceLocation fluid) {
        super(offset, HasCauldron.ofFluid(fluid));
        this.fluid = fluid;
    }

    public static @NotNull HasCauldron empty(Vec3 offset) {
        return new HasCauldron(offset, EMPTY);
    }

    public static BlockStatePredicate ofFluid(@NotNull ResourceLocation fluid) {
        if (fluid.equals(EMPTY)) {
            return BlockStatePredicate.builder()
                .of(Blocks.CAULDRON)
                .build();
        }
        String namespace = fluid.getNamespace();
        String path = fluid.getPath();
        ResourceLocation cauldron = ResourceLocation.fromNamespaceAndPath(namespace, "%s_cauldron".formatted(path));
        Holder.Reference<Block> reference = BuiltInRegistries.BLOCK.getHolder(cauldron).orElse(null);
        Block block = Blocks.WATER_CAULDRON;
        if (reference != null) block = reference.value();
        return BlockStatePredicate.builder()
            .of(block)
            .withMin(CauldronUtil.LEVEL_4, 1)
            .build();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        if (this.fluid.equals(EMPTY)) return;
        BlockPos blockPos = BlockPos.containing(context.getPos().add(this.offset));
        BlockCache cache = context.computeIfAbsent(BlockCache.BLOCK_CACHE);
        BlockState state = cache.getBlockState(blockPos);
        IntegerProperty property = CauldronUtil.LEVEL_4;
        Optional<Integer> optionalValue = state.getOptionalValue(CauldronUtil.LEVEL_4);
        if (optionalValue.isEmpty()) {
            property = CauldronUtil.LEVEL_3;
            optionalValue = state.getOptionalValue(CauldronUtil.LEVEL_3);
        }
        int value = optionalValue.orElse(0) - 1;
        if (value <= 0) {
            cache.setBlock(blockPos, Blocks.CAULDRON);
        }
        cache.setBlock(blockPos, state.setValue(property, value));
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_CAULDRON.get();
    }

    public static class Type implements IRecipePredicate.Type<HasCauldron> {
        public final MapCodec<HasCauldron> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(HasCauldron::getOffset),
                ResourceLocation.CODEC.fieldOf("predicate").forGetter(HasCauldron::getFluid)
            ).apply(instance, HasCauldron::new)
        );
        public final StreamCodec<RegistryFriendlyByteBuf, HasCauldron> mapCodec = StreamCodec.of(this::encode, this::decode);

        @Override
        public @NotNull MapCodec<HasCauldron> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, HasCauldron> streamCodec() {
            return this.mapCodec;
        }

        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull HasCauldron hasCauldron) {
            buf.writeVec3(hasCauldron.getOffset());
            buf.writeResourceLocation(hasCauldron.fluid);
        }

        public @NotNull HasCauldron decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new HasCauldron(buf.readVec3(), buf.readResourceLocation());
        }
    }
}
