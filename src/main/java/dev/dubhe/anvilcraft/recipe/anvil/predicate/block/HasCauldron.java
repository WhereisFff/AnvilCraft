package dev.dubhe.anvilcraft.recipe.anvil.predicate.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.anvil.cache.BlockCache;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.util.WrapUtils;
import dev.dubhe.anvilcraft.util.CauldronUtil;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static final ResourceLocation NULL = ResourceLocation.withDefaultNamespace("null");
    private final ResourceLocation fluid;
    private final int consume;
    private final ResourceLocation transform;

    public HasCauldron(Vec3 offset, ResourceLocation fluid, int consume, ResourceLocation transform) {
        super(offset, HasCauldron.ofFluid(fluid, consume));
        this.fluid = fluid;
        this.consume = consume;
        this.transform = transform;
    }

    public static @NotNull HasCauldron empty(Vec3 offset) {
        return new HasCauldron(offset, EMPTY, 0, NULL);
    }

    public static BlockStatePredicate ofFluid(@NotNull ResourceLocation fluid, int consume) {
        if (fluid.equals(EMPTY)) {
            return BlockStatePredicate.builder()
                .of(Blocks.CAULDRON)
                .build();
        }
        Block block = HasCauldron.getDefaultCauldron(fluid);
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

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        if (this.fluid.equals(EMPTY)) return;
        BlockPos blockPos = BlockPos.containing(context.getPos().add(this.offset));
        BlockCache cache = context.computeIfAbsent(BlockCache.BLOCK_CACHE);
        BlockState state = cache.getBlockState(blockPos);
        if (state.is(Blocks.CAULDRON)) {
            Block block = this.getFluidCauldron();
            state = block.defaultBlockState();
        }
        IntegerProperty property = CauldronUtil.LEVEL_4;
        Optional<Integer> fluidLevel = state.getOptionalValue(property);
        if (fluidLevel.isEmpty()) {
            property = CauldronUtil.LEVEL_3;
            fluidLevel = state.getOptionalValue(property);
        }
        if (fluidLevel.isPresent()) {
            fluidLevel = Optional.of(Math.clamp(fluidLevel.orElse(0) - this.consume, 0, property.max));
            if (fluidLevel.orElse(0) == 0) {
                state = Blocks.CAULDRON.defaultBlockState();
            } else {
                state = state.setValue(property, fluidLevel.orElse(0));
            }
        }
        if (
            fluidLevel.orElse(0) > 0
                && this.transform != null
                && !this.transform.equals(this.fluid)
                && !this.transform.equals(HasCauldron.NULL)
        ) {
            Block block = this.getTransformCauldron();
            state = block.defaultBlockState();
            property = CauldronUtil.LEVEL_4;
            Optional<Integer> transformLevel = state.getOptionalValue(property);
            if (transformLevel.isEmpty()) property = CauldronUtil.LEVEL_3;
            transformLevel = Optional.of(Math.clamp(fluidLevel.orElse(0), 1, property.max));
            state = state.setValue(property, transformLevel.orElse(1));
        }
        cache.setBlock(blockPos, state);
        context.putAcceptor(BlockCache.BLOCK_CACHE.location(), BlockCache.DEFAULT_ACCEPTOR);
    }

    public static Block getDefaultCauldron(@NotNull ResourceLocation fluid) {
        String namespace = fluid.getNamespace();
        String path = fluid.getPath();
        ResourceLocation cauldron = ResourceLocation.fromNamespaceAndPath(namespace, "%s_cauldron".formatted(path));
        Holder.Reference<Block> reference = BuiltInRegistries.BLOCK.getHolder(cauldron).orElse(null);
        Block block = Blocks.WATER_CAULDRON;
        if (reference != null) block = reference.value();
        return block;
    }

    public Block getFluidCauldron() {
        return HasCauldron.getDefaultCauldron(this.fluid);
    }

    public Block getTransformCauldron() {
        return HasCauldron.getDefaultCauldron(this.transform);
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_CAULDRON.get();
    }

    public static class Type implements IRecipePredicate.Type<HasCauldron> {
        public final MapCodec<HasCauldron> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(HasCauldron::getOffset),
                ResourceLocation.CODEC.optionalFieldOf("fluid", EMPTY).forGetter(HasCauldron::getFluid),
                Codec.INT.optionalFieldOf("consume", 0).forGetter(HasCauldron::getConsume),
                ResourceLocation.CODEC.optionalFieldOf("transform", NULL).forGetter(HasCauldron::getTransform)
            ).apply(instance, HasCauldron::new)
        );

        public final StreamCodec<RegistryFriendlyByteBuf, HasCauldron> mapCodec = StreamCodec.composite(
            RecipeUtil.VEC3_STREAM_CODEC,
            HasCauldron::getOffset,
            ResourceLocation.STREAM_CODEC,
            HasCauldron::getFluid,
            ByteBufCodecs.INT,
            HasCauldron::getConsume,
            ResourceLocation.STREAM_CODEC,
            HasCauldron::getTransform,
            HasCauldron::new
        );

        @Override
        public @NotNull MapCodec<HasCauldron> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, HasCauldron> streamCodec() {
            return this.mapCodec;
        }
    }

    public static class Builder {
        private Vec3 offset = Vec3.ZERO;
        private ResourceLocation fluid = HasCauldron.EMPTY;
        private int consume = 0;
        private ResourceLocation transform = HasCauldron.NULL;

        public Builder offset(Vec3 offset) {
            this.offset = offset;
            return this;
        }

        public Builder offset(double x, double y, double z) {
            return this.offset(new Vec3(x, y, z));
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

        public Builder empty() {
            this.fluid = HasCauldron.EMPTY;
            return this;
        }

        public Builder fluid(ResourceLocation fluid) {
            this.fluid = fluid;
            return this;
        }

        public Builder cauldron(Block cauldron) {
            this.fluid = WrapUtils.cauldron2Fluid(cauldron);
            return this;
        }

        public Builder transform(ResourceLocation transform) {
            this.transform = transform;
            return this;
        }

        public Builder consume() {
            this.consume = 1;
            return this;
        }

        public Builder consume(int consume) {
            this.consume = consume;
            return this;
        }

        public Builder produce() {
            this.consume = -1;
            return this;
        }

        public Builder produce(int produce) {
            this.consume = -produce;
            return this;
        }

        public HasCauldron build() {
            return new HasCauldron(this.offset, this.fluid, this.consume, this.transform);
        }
    }
}
