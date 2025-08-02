package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasCauldron;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
                .optionalFieldOf("transform", HasCauldron.EMPTY)
                .forGetter(HasCauldronSimple::getTransform)
        ).apply(instance, HasCauldronSimple::new)
    );

    public HasCauldron toHasCauldron(Vec3 offset) {
        return new HasCauldron(offset, fluid, consume, transform);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, HasCauldronSimple> STREAM_CODEC = StreamCodec.of(
        HasCauldronSimple::encode,
        HasCauldronSimple::decode
    );

    public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull HasCauldronSimple hasCauldronSimple) {
        buf.writeResourceLocation(hasCauldronSimple.getFluid());
        buf.writeInt(hasCauldronSimple.getConsume());
        buf.writeResourceLocation(hasCauldronSimple.getTransform());
    }

    public static @NotNull HasCauldronSimple decode(@NotNull RegistryFriendlyByteBuf buf) {
        return new HasCauldronSimple(buf.readResourceLocation(), buf.readInt(), buf.readResourceLocation());
    }

    public static @NotNull Builder empty() {
        return Builder.empty();
    }

    public static @NotNull Builder fluid(ResourceLocation fluid) {
        return Builder.of(fluid);
    }

    public static class Builder {
        private ResourceLocation fluid = HasCauldron.EMPTY;
        private int consume = 0;
        private ResourceLocation transform = HasCauldron.EMPTY;

        public static @NotNull Builder empty() {
            return new Builder();
        }

        public static @NotNull Builder of(ResourceLocation fluid) {
            Builder builder = new Builder();
            builder.fluid = fluid;
            return builder;
        }

        public @NotNull Builder fluid(ResourceLocation fluid) {
            if (this.transform.equals(this.fluid)) {
                this.transform = fluid;
            }
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
