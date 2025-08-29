package dev.dubhe.anvilcraft.integration.ponder.scene.recipe;

import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderTags;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
                IronTrapdoorScene::crafting,
                AnvilCraftPonderTags.PROCESSING_COMPONENTS
            );
    }

    private static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("iron_trapdoor", "Use Iron Trapdoor to trigger unpack Recipe");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        // 创建铁活板门
        BlockPos ironTrapdoorPos = new BlockPos(2, 1, 2);
        scene.world().setBlock(ironTrapdoorPos, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP), false);
        scene.world().showIndependentSection(util.select().position(ironTrapdoorPos), Direction.NORTH);
        // 创建铁砧
        BlockPos anvilPos = new BlockPos(2, 3, 2);
        scene.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilLink = scene.world()
            .showIndependentSection(util.select().position(anvilPos), Direction.NORTH);
        scene.idle(20);

        // 执行操作
        BlockPos itemPos = util.grid().at(2, 2, 2);
        ItemStack itemStack = new ItemStack(Blocks.QUARTZ_BLOCK, 1);
        ElementLink<EntityElement> quartzBlock = scene.world().createItemEntity(itemPos.getCenter(), Vec3.ZERO, itemStack);
        scene.idle(20);

        // 铁砧下落
        scene.world().moveSection(anvilLink, new Vec3(0, -1, 0), 3);
        scene.idle(3);

        // 拆解石英块
        ItemStack outputItem = new ItemStack(Items.QUARTZ.asItem(), 4);
        scene.world().modifyEntity(quartzBlock, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.world().createItemEntity(ironTrapdoorPos.getCenter(), Vec3.ZERO, outputItem);
        scene.idle(10);

        scene.world().moveSection(anvilLink, new Vec3(0, 1, 0), 3);
        scene.idle(3);

        // 生成文本
        scene.overlay().showText(50)
            .text("You can use the iron trapdoor to disassemble composite items into their components.")
            .pointAt(util.vector().blockSurface(ironTrapdoorPos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();

        scene.idle(20);

        scene.markAsFinished();
    }
}
