package dev.dubhe.anvilcraft.integration.ponder;

import dev.dubhe.anvilcraft.integration.ponder.scene.AnvilScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.logistics.ChuteScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.power.TransmissionPoleScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.recipe.HeaterScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.recipe.SpaceOvercompressorScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.redstone.BlockComparatorScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.redstone.BlockPlacerScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.redstone.MagnetScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.structure.ImpactPileScene;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AnvilCraftPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // base
        AnvilScene.register(helper);
        // power
        TransmissionPoleScene.register(helper);
        // recipe
        SpaceOvercompressorScene.register(helper);
        HeaterScene.register(helper);
        IronTrapdoorScene.register(helper);
        // redstone
        MagnetScene.register(helper);
        BlockComparatorScene.register(helper);
        BlockPlacerScene.register(helper);
        // structure
        ImpactPileScene.register(helper);
        // logistics
        ChuteScene.register(helper);
    }
}
