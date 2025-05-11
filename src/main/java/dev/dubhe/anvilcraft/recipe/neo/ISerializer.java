package dev.dubhe.anvilcraft.recipe.neo;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public interface ISerializer<T> {
    @NotNull MapCodec<T> codec();

    @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
