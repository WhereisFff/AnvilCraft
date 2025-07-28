package dev.dubhe.anvilcraft.recipe.neo.wrap;

import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeTrigger;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class BlockCompressRecipe extends InWorldRecipe {
    public BlockCompressRecipe(@NotNull ItemStack icon, IRecipeTrigger trigger, @Unmodifiable List<IRecipePredicate<?>> conflicting, @Unmodifiable List<IRecipePredicate<?>> nonConflicting, @Unmodifiable List<IRecipeOutcome<?>> outcomes, int priority, boolean compatible) {
        super(
            icon,
            trigger,
            conflicting,
            nonConflicting,
            outcomes,
            priority,
            compatible
        );
    }
}
