package dev.dubhe.anvilcraft.integration.ponder.scene.recipe;


import dev.dubhe.anvilcraft.block.HeaterBlock;
import dev.dubhe.anvilcraft.block.TransmissionPoleBlock;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderTags;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.WorldInstructions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HeaterScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<Item> helper = registrationHelper.withKeyFunction(
            BuiltInRegistries.ITEM::getKey
        );
        helper.forComponents(ModBlocks.HEATER.asItem())
            .addStoryBoard(
                "platform/555",
                HeaterScene::crafting,
                AnvilCraftPonderTags.PROCESSING_COMPONENTS
            );
    }

    private static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("heater", "Use heater to execute the high-heat recipe");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        // Start Create Heater Block
        BlockPos heaterBlockPos = new BlockPos(2, 1, 2);
        scene.world().setBlock(heaterBlockPos, ModBlocks.HEATER.getDefaultState(), true);
        scene.world().showIndependentSection(util.select().position(heaterBlockPos), Direction.NORTH);
        scene.overlay().showText(30)
            .text("When placing down the heater, it will not work if there is no valid power grid nearby "
                + "or if the grid is already overloaded.")
            .pointAt(util.vector().blockSurface(heaterBlockPos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(40);
        BlockPos transmissionPolePos = new BlockPos(4, 1, 2);
        placeTransmissionPole(scene.world(), util, transmissionPolePos);
        scene.overlay().showText(15)
            .text("It requires 16 kW of power to operate.")
            .pointAt(util.vector().blockSurface(heaterBlockPos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(20);
        scene.overlay().showText(40)
            .text("Assuming we are now connected to a valid power grid...")
            .pointAt(util.vector().blockSurface(transmissionPolePos.above(2), Direction.UP))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(20);
        // Let's Light On
        scene.world().setBlock(transmissionPolePos.above(2),
            ModBlocks.TRANSMISSION_POLE.getDefaultState()
                .setValue(TransmissionPoleBlock.HALF, Vertical3PartHalf.TOP)
                .setValue(HeaterBlock.OVERLOAD, false),
            false
        );
        scene.world().setBlock(heaterBlockPos,
            ModBlocks.HEATER.getDefaultState()
                .setValue(HeaterBlock.OVERLOAD, false),
            false
        );
        scene.overlay().showText(20)
            .text("The heater is now working properly.")
            .pointAt(util.vector().blockSurface(
                heaterBlockPos, Direction.UP
            ))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(30);
        BlockPos cauldronBlockPos = heaterBlockPos.above(1);
        scene.overlay().showText(15)
            .text("Place a cauldron on the heater")
            .pointAt(util.vector().blockSurface(
                cauldronBlockPos, Direction.DOWN
            ))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(20);
        scene.world().setBlock(cauldronBlockPos, Blocks.CAULDRON.defaultBlockState(), true);
        scene.world().showIndependentSection(util.select().position(cauldronBlockPos), Direction.NORTH);
        scene.idle(25);
        BlockPos inputItemPos = heaterBlockPos.above(2);
        scene.overlay().showText(20)
            .text("Drop some raw minerals into the cauldron...")
            .pointAt(util.vector().blockSurface(inputItemPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(10);
        ElementLink<EntityElement> inputItemEntityLink = scene.world().createItemEntity(
            util.vector().centerOf(inputItemPos),
            new Vec3(0, -0.1, 0),
            new ItemStack(Items.RAW_IRON)
        );
        scene.overlay()
            .showControls(util.vector()
                .of(2.5, 3.5, 2.5), Pointing.DOWN, 20)
            .withItem(new ItemStack(Items.RAW_IRON));
        scene.idle(40);
        BlockPos anvilBlockPos = heaterBlockPos.above(3);
        scene.overlay().showText(40)
            .text("Use an anvil or the Anvil Hammer to strike the cauldron.")
            .pointAt(util.vector().blockSurface(
                anvilBlockPos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.overlay()
            .showControls(new Vec3(
                cauldronBlockPos.getX() + 1,
                cauldronBlockPos.getY() + .5,
                cauldronBlockPos.getZ()
            ), Pointing.RIGHT, 25)
            .leftClick()
            .withItem(ModItems.ANVIL_HAMMER.asStack());
        scene.world().setBlock(anvilBlockPos, Blocks.ANVIL.defaultBlockState(), true);
        ElementLink<WorldSectionElement> anvilLink = scene.world().showIndependentSection(
            util.select().position(anvilBlockPos), Direction.UP
        );
        scene.idle(30);
        scene.world().moveSection(anvilLink, new Vec3(0, -1, 0), 2);
        scene.idle(2);
        scene.world().modifyEntity(inputItemEntityLink, entity -> entity.setPos(2, -100, 2));
        scene.world().createItemEntity(
            util.vector().centerOf(cauldronBlockPos),
            new Vec3(0, -0.1, 0),
            new ItemStack(Items.IRON_INGOT, 2)
        );
        scene.idle(4);
        scene.world().moveSection(anvilLink, new Vec3(0, 1, 0), 3);
        scene.overlay().showText(40)
            .text("It will execute the high-heat recipe")
            .pointAt(util.vector().blockSurface(cauldronBlockPos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.overlay()
            .showControls(util.vector()
                .of(3.5, 2.25, 2.5), Pointing.RIGHT, 30)
            .withItem(new ItemStack(Items.IRON_INGOT, 2));
        scene.idle(30);
        scene.markAsFinished();
    }

    private static void placeTransmissionPole(
        WorldInstructions world,
        @NotNull SceneBuildingUtil util,
        BlockPos bottomPos
    ) {
        BlockState baseState =  ModBlocks.TRANSMISSION_POLE.getDefaultState();
        Vertical3PartHalf[] parts = {
            Vertical3PartHalf.BOTTOM,
            Vertical3PartHalf.MID,
            Vertical3PartHalf.TOP
        };

        for (int i = 0; i < parts.length; i++) {
            BlockPos pos = bottomPos.above(i);
            BlockState state = baseState.trySetValue(TransmissionPoleBlock.HALF, parts[i]);

            if (parts[i] == Vertical3PartHalf.TOP) {
                state = state.trySetValue(TransmissionPoleBlock.OVERLOAD, true);
            }

            world.setBlock(pos, state, false);
            world.showIndependentSection(util.select().position(pos), Direction.NORTH);
        }
    }

}
