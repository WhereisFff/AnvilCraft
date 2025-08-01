package dev.dubhe.anvilcraft.recipe.neo.predicate.block;

import com.mojang.serialization.Codec;
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
    private final int consume;

    public HasCauldron(Vec3 offset, ResourceLocation fluid, int consume) {
        super(offset, HasCauldron.ofFluid(fluid, consume));
        this.fluid = fluid;
        this.consume = consume;
    }

    public static @NotNull HasCauldron empty(Vec3 offset) {
        return new HasCauldron(offset, EMPTY, 0);
    }

    public static BlockStatePredicate ofFluid(@NotNull ResourceLocation fluid, int consume) {
        if (fluid.equals(EMPTY)) {
            return BlockStatePredicate.builder()
                .of(Blocks.CAULDRON)
                .build();
        }
        Block block = getDefaultCauldron(fluid);
        BlockState state = block.defaultBlockState();
        IntegerProperty property = CauldronUtil.LEVEL_4;
        Optional<Integer> optionalValue = state.getOptionalValue(CauldronUtil.LEVEL_4);
        if (optionalValue.isEmpty()) {
            property = CauldronUtil.LEVEL_3;
        }
        if (consume > 0) {
            return BlockStatePredicate.builder()
                .of(block)
                .withMin(property, consume)
                .build();
        }
        return BlockStatePredicate.builder()
            .of(block, Blocks.CAULDRON)
            .build();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        if (this.fluid.equals(EMPTY)) return;
        BlockPos blockPos = BlockPos.containing(context.getPos().add(this.offset));
        BlockCache cache = context.computeIfAbsent(BlockCache.BLOCK_CACHE);
        BlockState state = cache.getBlockState(blockPos);
        if (state.is(Blocks.CAULDRON)) {
            Block block = getDefaultCauldron(fluid);
            state = block.defaultBlockState();
        }
        IntegerProperty property = CauldronUtil.LEVEL_4;
        Optional<Integer> optionalValue = state.getOptionalValue(CauldronUtil.LEVEL_4);
        if (optionalValue.isEmpty()) {
            property = CauldronUtil.LEVEL_3;
            optionalValue = state.getOptionalValue(CauldronUtil.LEVEL_3);
        }
        int value = Math.min(Math.max(optionalValue.orElse(0) - this.consume, 0), property.max);
        if (value == 0) {
            cache.setBlock(blockPos, Blocks.CAULDRON);
        }
        cache.setBlock(blockPos, state.setValue(property, value));
    }

    private static Block getDefaultCauldron(@NotNull ResourceLocation fluid) {
        String namespace = fluid.getNamespace();
        String path = fluid.getPath();
        ResourceLocation cauldron = ResourceLocation.fromNamespaceAndPath(namespace, "%s_cauldron".formatted(path));
        Holder.Reference<Block> reference = BuiltInRegistries.BLOCK.getHolder(cauldron).orElse(null);
        Block block = Blocks.WATER_CAULDRON;
        if (reference != null) block = reference.value();
        return block;
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_CAULDRON.get();
    }

    public static class Type implements IRecipePredicate.Type<HasCauldron> {
        public final MapCodec<HasCauldron> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(HasCauldron::getOffset),
                ResourceLocation.CODEC.optionalFieldOf("fluid", EMPTY).forGetter(HasCauldron::getFluid),
                Codec.INT.optionalFieldOf("consume", 0).forGetter(HasCauldron::getConsume)
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
            buf.writeResourceLocation(hasCauldron.getFluid());
            buf.writeInt(hasCauldron.getConsume());
        }

        public @NotNull HasCauldron decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new HasCauldron(
                buf.readVec3(),
                buf.readResourceLocation(),
                buf.readInt()
            );
        }
    }
}
