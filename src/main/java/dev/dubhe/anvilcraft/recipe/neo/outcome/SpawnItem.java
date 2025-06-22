package dev.dubhe.anvilcraft.recipe.neo.outcome;

import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;

public class SpawnItem implements IRecipeOutcome<SpawnItem> {
    @Override
    public Type<SpawnItem> getType() {
        return null;
    }

    @Override
    public void accept(InWorldRecipeContext context) {

    }
}
