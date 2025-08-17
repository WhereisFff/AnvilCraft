package dev.dubhe.anvilcraft.recipe.anvil.cache;

import net.neoforged.neoforge.items.IItemHandler;

public interface IItemHandlerCache {
    IItemHandler getInput();

    IItemHandler getOutput();
}
