package dev.dubhe.anvilcraft.recipe.anvil.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasCauldron;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class HasCauldronSimple {
    private final ResourceLocation fluid;
    private final int consume;
    private final ResourceLocation transform;

    public HasCauldronSimple(ResourceLocation fluid, int consume, ResourceLocation transform) {
        this.fluid = fluid;
        this.consume = consume;
        this.transform = transform;
    }

    public static final MapCodec<HasCauldronSimple> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            ResourceLocation.CODEC
                .optionalFieldOf("fluid", HasCauldron.EMPTY)
                .forGetter(HasCauldronSimple::getFluid),
            Codec.INT
                .optionalFieldOf("consume", 0)
                .forGetter(HasCauldronSimple::getConsume),
            ResourceLocation.CODEC
                .optionalFieldOf("transform", HasCauldron.NULL)
                .forGetter(HasCauldronSimple::getTransform)
        ).apply(instance, HasCauldronSimple::new)
    );

    public HasCauldron toHasCauldron(Vec3 offset) {
        return new HasCauldron(offset, fluid, consume, transform);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, HasCauldronSimple> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        HasCauldronSimple::getFluid,
        ByteBufCodecs.INT,
        HasCauldronSimple::getConsume,
        ResourceLocation.STREAM_CODEC,
        HasCauldronSimple::getTransform,
        HasCauldronSimple::new
    );

    public static @NotNull Builder empty() {
        return Builder.empty();
    }

    public static @NotNull Builder fluid(ResourceLocation fluid) {
        return Builder.of(fluid);
    }

    public static class Builder {
        private ResourceLocation fluid = HasCauldron.EMPTY;
        private int consume = 0;
        private ResourceLocation transform = HasCauldron.NULL;

        public static @NotNull Builder empty() {
            return new Builder();
        }

        public static @NotNull Builder of(ResourceLocation fluid) {
            Builder builder = new Builder();
            builder.fluid = fluid;
            return builder;
        }

        public @NotNull Builder fluid(ResourceLocation fluid) {
            this.fluid = fluid;
            return this;
        }

        public @NotNull Builder transform(ResourceLocation transform) {
            this.transform = transform;
            return this;
        }

        public Builder consume(int consume) {
            this.consume = consume;
            return this;
        }

        public HasCauldronSimple build() {
            return new HasCauldronSimple(fluid, consume, transform);
        }
    }
}
