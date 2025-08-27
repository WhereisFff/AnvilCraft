package dev.dubhe.anvilcraft.integration.ponder.scene.recipe;

import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.item.Items.IRON_TRAPDOOR;

public class IronTrapdoorScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<Item> helper = registrationHelper.withKeyFunction(BuiltInRegistries.ITEM::getKey);
        helper.forComponents(
                        IRON_TRAPDOOR
                )
                .addStoryBoard(
                        "platform/555",
                        IronTrapdoorScene::crafting
                );
    }

    private static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("iron_trapdoor", "Use iron_trapdoor to trigger unpack Recipe");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        //创建铁活板门
        BlockPos Iron_TrapdoorPos = new BlockPos(2, 1, 2);
        scene.world().setBlock(Iron_TrapdoorPos, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF , Half.TOP),false );
        Selection Iron_Trapdoor = util.select().position(2, 1, 2);
        scene.world().showIndependentSection(Iron_Trapdoor, Direction.NORTH);
        // 创建铁砧
        BlockPos anvilPos = new BlockPos(2, 3, 2);
        scene.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        Selection anvil = util.select().position(2, 3, 2);
        ElementLink<WorldSectionElement> anvilLink = scene.world().showIndependentSection(anvil, Direction.NORTH);
        scene.idle(10);
        //执行操作
        Vec3 ironBlockPos = new Vec3(2.5, 2.3, 2.5);
        Vec3 ironBlockMotion = new Vec3(0, -0.3, 0);
        ItemStack ironBlockItem = new ItemStack(Blocks.QUARTZ_BLOCK, 1);
        ElementLink<EntityElement> Quartz_block = scene.world().createItemEntity(ironBlockPos, ironBlockMotion, ironBlockItem);
        scene.idle(5);
        scene.world().moveSection(anvilLink, new Vec3(0, -1, 0), 2);
        scene.idle(2);
        scene.world().modifyEntity(Quartz_block, entity -> entity.setPos(2.5, -100, 2.5));
        scene.idle(6);
        scene.world().moveSection(anvilLink, new Vec3(0, 1, 0), 3);
        scene.idle(3);
        //生成石英与文本
        scene.overlay().showText(50)
                .text("You can use the iron trapdoor to disassemble composite items into their components.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.WEST))
                .attachKeyFrame()
                .placeNearTarget();
        ItemStack outputItem = new ItemStack(Items.QUARTZ.asItem(), 4);
        Vec3 outputPos = new Vec3(2.5, 1.8, 2.5);
        scene.world().createItemEntity(outputPos, Vec3.ZERO, outputItem);

        scene.idle(20);

        scene.markAsFinished();

    }
}
