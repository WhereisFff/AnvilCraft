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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;


public class MagnetScene {
    public static void register(@NotNull PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registrationHelper.withKeyFunction(RegistryEntry::getId);
        helper.forComponents(
                ModBlocks.MAGNET_BLOCK,
                ModBlocks.HOLLOW_MAGNET_BLOCK,
                ModBlocks.FERRITE_CORE_MAGNET_BLOCK
            )
            .addStoryBoard(
                "platform/555",
                MagnetScene::crafting,
                AnvilCraftPonderTags.MAGNET_BLOCK,
                AnvilCraftPonderTags.ANVIL
            );
    }

    private static void crafting(@NotNull SceneBuilder scene, @NotNull SceneBuildingUtil util) {
        scene.title("magnet", "Use magnet to attract the anvil");
        scene.configureBasePlate(0, 0, 5);

        Selection basePlate = util.select().fromTo(0, 0, 0, 5, 0, 5);
        scene.world().showSection(basePlate, Direction.UP);
        // 创建锅
        scene.world().setBlock(new BlockPos(2, 1, 2), Blocks.CAULDRON.defaultBlockState(), false);
        Selection cauldron = util.select().position(2, 1, 2);
        scene.world().showSection(cauldron, Direction.NORTH);
        // 创建铁砧
        scene.world().setBlock(new BlockPos(2, 2, 2), Blocks.ANVIL.defaultBlockState(), false);
        Selection anvil = util.select().position(2, 2, 2);
        ElementLink<WorldSectionElement> anvilLink = scene.world().showIndependentSection(anvil, Direction.NORTH);
        scene.idle(5);

        scene.overlay().showText(30)
            .text("The anvil needs to be lifted and fallen for processing")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(40);
        // 创建磁铁
        scene.world().setBlock(new BlockPos(2, 4, 2), ModBlocks.MAGNET_BLOCK.getDefaultState(), false);
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
        scene.world().setBlock(new BlockPos(3, 4, 2), Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
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
            .pointAt(util.vector().blockSurface(util.grid().at(2, 4, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(40);

        scene.markAsFinished();
    }

}
