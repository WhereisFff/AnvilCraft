package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.outcome.DamageAnvil;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeOutcomeTypes {
    public static final DeferredRegister<IRecipeOutcome.Type<?>> OUTCOME_TYPE = DeferredRegister
        .create(ModRegistries.OUTCOME_TYPE_REGISTRY, AnvilCraft.MOD_ID);

    public static final DeferredHolder<IRecipeOutcome.Type<?>, DamageAnvil.Type> DAMAGE_ANVIL = OUTCOME_TYPE
        .register("damage_anvil", DamageAnvil.Type::new);
}
