package dev.dubhe.anvilcraft.integration.ponder;

import dev.dubhe.anvilcraft.integration.ponder.scene.AnvilScene;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AnvilCraftPonderScenes {
    public static void register(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AnvilScene.register(helper);
    }
}
