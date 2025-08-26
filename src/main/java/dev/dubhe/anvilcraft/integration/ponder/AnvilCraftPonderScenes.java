package dev.dubhe.anvilcraft.integration.ponder;

import dev.dubhe.anvilcraft.integration.ponder.scene.AnvilScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.power.TransmissionPoleScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.HeaterScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.ImpactPileScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.redstone.BlockComparatorScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.redstone.MagnetScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.recipe.SpaceOvercompressorScene;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AnvilCraftPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // base
        AnvilScene.register(helper);
        // recipe
        SpaceOvercompressorScene.register(helper);
        HeaterScene.register(helper);
        // redstone
        MagnetScene.register(helper);
        BlockComparatorScene.register(helper);
        // structure
        ImpactPileScene.register(helper);
        // power
        TransmissionPoleScene.register(helper);
    }
}
