package dev.dubhe.anvilcraft.recipe.neo;

import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.init.ModRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface RecipeOutcomeType<T extends RecipeOutcome> {
    default ResourceLocation getId() {
        return ModRegistries.BuiltIn.RECIPE_OUTCOME.getKey(this);
    }

    @NotNull Codec<T> codec();

    @NotNull StreamCodec<? super ByteBuf, T> streamCodec();

    static <T extends RecipeOutcome> @NotNull RecipeOutcomeType<T> of(@NotNull Codec<T> codec, @NotNull StreamCodec<? super ByteBuf, T> streamCodec) {
        return new RecipeOutcomeType<>() {
            @Override
            public @NotNull Codec<T> codec() {
                return codec;
            }

            @Override
            public @NotNull StreamCodec<? super ByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }
}
