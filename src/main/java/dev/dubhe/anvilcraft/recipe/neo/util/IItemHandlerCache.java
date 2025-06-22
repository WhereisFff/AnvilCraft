package dev.dubhe.anvilcraft.recipe.neo.util;

import net.neoforged.neoforge.items.IItemHandler;

public interface IItemHandlerCache {
    IItemHandler getInput();

    IItemHandler getOutput();
}
