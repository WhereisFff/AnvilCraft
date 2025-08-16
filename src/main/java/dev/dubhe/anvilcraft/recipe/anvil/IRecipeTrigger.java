package dev.dubhe.anvilcraft.recipe.anvil;

import dev.dubhe.anvilcraft.init.ModRegistries;
import net.minecraft.resources.ResourceLocation;

public interface IRecipeTrigger extends IPrioritized {
    default ResourceLocation getId() {
        return ModRegistries.TRIGGER_REGISTRY.getKey(this);
    }
}
