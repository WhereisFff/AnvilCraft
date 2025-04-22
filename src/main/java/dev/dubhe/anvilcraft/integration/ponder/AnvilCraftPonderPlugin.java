package dev.dubhe.anvilcraft.integration.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

import dev.dubhe.anvilcraft.AnvilCraft;
import org.jetbrains.annotations.NotNull;

public class AnvilCraftPonderPlugin implements PonderPlugin {

    /**
     * @return the modID of the mod that added this plugin
     */
    @Override
    public @NotNull String getModId() {
        return AnvilCraft.MOD_ID;
    }

    /**
     * Register all the Ponder Scenes added by your Mod
     *
     */
    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AnvilCraftPonderScenes.register(helper);
    }

    /**
     * Register all the Ponder Tags added by your Mod
     *
     */
    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        AnvilCraftPonderTags.register(helper);
    }
}
