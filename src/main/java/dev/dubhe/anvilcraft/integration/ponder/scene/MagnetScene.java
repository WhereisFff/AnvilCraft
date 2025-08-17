package dev.dubhe.anvilcraft.integration.ponder.scene;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.block.MagnetBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderTags;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;


public class MagnetScene {
    public static void register(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.forComponents(
            ModBlocks.MAGNET_BLOCK,
            ModBlocks.HOLLOW_MAGNET_BLOCK,
            ModBlocks.FERRITE_CORE_MAGNET_BLOCK
        ).addStoryBoard("magnet/01", MagnetScene::crafting, AnvilCraftPonderTags.ANVIL);
    }

    private static void crafting(@NotNull SceneBuilder scene, @NotNull SceneBuildingUtil util) {
        scene.title("magnet", "Use magnet to draw the anvil");
        scene.configureBasePlate(0, 0, 5);

        Selection basePlate = util.select().fromTo(0, 0, 0, 5, 0, 5);
        scene.world().showSection(basePlate, Direction.UP);

        Selection cauldron = util.select().position(2, 1, 2);
        scene.world().showSection(cauldron, Direction.NORTH);

        Selection anvil = util.select().position(2, 2, 2);
        ElementLink<WorldSectionElement> anvilLink = scene.world().showIndependentSection(anvil, Direction.NORTH);
        scene.idle(5);

        scene.overlay().showText(30)
            .text("The anvil needs to be lifted and smashed down for processing")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(40);

        Selection magnet = util.select().position(2, 4, 2);
        scene.world().showIndependentSection(magnet, Direction.WEST);
        scene.idle(10);

        scene.world().moveSection(anvilLink, new Vec3(0, 1, 0), 4);
        scene.idle(5);

        scene.overlay().showText(30)
            .text("Magnets can attract anvils")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(40);
        // 放置红石块使磁铁失效
        Selection redstoneBlock = util.select().position(3, 4, 2);
        scene.world().showIndependentSection(redstoneBlock, Direction.WEST);
        scene.idle(10);
        scene.world().modifyBlock(new BlockPos(2, 4, 2),
            bs -> bs.setValue(MagnetBlock.LIT, true),
            false
        );

        scene.world().moveSection(anvilLink, new Vec3(0, -1, 0), 7);
        scene.idle(10);

        scene.overlay().showText(30)
            .text("Magnet will stop working when it receives a redstone signal.")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(40);

        scene.markAsFinished();
    }

}
