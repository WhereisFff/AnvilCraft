package dev.dubhe.anvilcraft.recipe.neo.outcome;

import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModRecipeOutcomeTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class DamageAnvil implements IRecipeOutcome<DamageAnvil> {
    public static final InWorldRecipeData<Boolean> DAMAGE_ANVIL = InWorldRecipeData.of(AnvilCraft.of("damage_anvil"), false);

    @Override
    public IRecipeOutcome.Type<DamageAnvil> getType() {
        return ModRecipeOutcomeTypes.DAMAGE_ANVIL.get();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        context.put(DAMAGE_ANVIL, true);
    }

    public static class Type implements IRecipeOutcome.Type<DamageAnvil> {
        @Override
        public @NotNull MapCodec<DamageAnvil> codec() {
            return MapCodec.unit(new DamageAnvil());
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DamageAnvil> streamCodec() {
            return StreamCodec.unit(new DamageAnvil());
        }
    }
}
