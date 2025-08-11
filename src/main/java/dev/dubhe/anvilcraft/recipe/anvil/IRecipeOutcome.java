package dev.dubhe.anvilcraft.recipe.anvil;

import dev.dubhe.anvilcraft.init.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IRecipeOutcome<O extends IRecipeOutcome<O>> extends Consumer<InWorldRecipeContext>, IPrioritized {
    Type<O> getType();

    default NumberProvider getChance() {
        return ConstantValue.exactly(1.0f);
    }

    default void acceptWithChance(@NotNull InWorldRecipeContext context) {
        ServerLevel level = context.getLevel();
        if (level.getRandom().nextDouble() > context.getFloat(this.getChance())) return;
        this.accept(context);
    }

    interface Type<O extends IRecipeOutcome<O>> extends ISerializer<O> {
        default ResourceLocation getId() {
            return ModRegistries.OUTCOME_TYPE_REGISTRY.getKey(this);
        }
    }
}
