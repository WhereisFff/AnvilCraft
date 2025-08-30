package dev.dubhe.anvilcraft.integration.ponder;

import dev.dubhe.anvilcraft.integration.ponder.scene.AnvilScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.logistics.ChuteScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.logistics.MagneticChuteScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.logistics.PoweredSlidingRailScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.logistics.SlidingRailScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.logistics.SlidingRailStopScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.power.TransmissionPoleScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.recipe.BlockRecipeScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.recipe.HeaterScene;
import dev.dubhe.anvilcraft.integration.ponder.scene.recipe.IronTrapdoorScene;
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
        BlockRecipeScene.register(helper);
        IronTrapdoorScene.register(helper);
        // redstone
        MagnetScene.register(helper);
        BlockComparatorScene.register(helper);
        BlockPlacerScene.register(helper);
        // structure
        ImpactPileScene.register(helper);
        // logistics
        ChuteScene.register(helper);
        MagneticChuteScene.register(helper);
        SlidingRailScene.register(helper);
        SlidingRailStopScene.register(helper);
        PoweredSlidingRailScene.register(helper);
    }
}
