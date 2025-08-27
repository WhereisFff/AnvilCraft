package dev.dubhe.anvilcraft.integration.ponder.scene.logistics;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.block.MagneticChuteBlock;
import dev.dubhe.anvilcraft.block.entity.MagneticChuteBlockEntity;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class SlidingRailScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registrationHelper.withKeyFunction(RegistryEntry::getId);
        helper.forComponents(ModBlocks.SLIDING_RAIL)
            .addStoryBoard(
                "platform/999",
                SlidingRailScene::itemSliding
            )
            .addStoryBoard(
                "platform/999",
                SlidingRailScene::blockSliding
            )
            .addStoryBoard(
                "platform/999",
                SlidingRailScene::multiBlockSliding
            );
    }

    // 演示物品在滑轨上滑行
    private static void itemSliding(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sliding_rail_items", "Items on sliding rails");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(20);

        // 创建一条长滑轨
        Selection railsSection = util.select().fromTo(1, 1, 4, 6, 1, 4);
        scene.world().setBlocks(railsSection, ModBlocks.SLIDING_RAIL.getDefaultState(), false);
        scene.world().showSection(railsSection, Direction.DOWN);
        scene.idle(20);

        // 添加文字说明
        scene.overlay().showText(40)
            .text("Sliding rails have extremely smooth surfaces that allow items to slide without friction")
            .pointAt(util.vector().topOf(util.grid().at(1, 1, 4)))
            .attachKeyFrame()
            .placeNearTarget();
        scene.rotateCameraY(-45);
        scene.idle(50);

        // 放置磁性溜槽在滑轨旁边
        BlockPos chutePos = util.grid().at(7, 1, 4);
        scene.world().setBlock(chutePos, ModBlocks.MAGNETIC_CHUTE.getDefaultState().setValue(MagneticChuteBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().position(chutePos), Direction.DOWN);
        scene.idle(20);
        scene.addKeyframe();
        // 向磁性溜槽添加物品
        ItemStack ironIngots = new ItemStack(Items.IRON_INGOT, 64);
        Vec3 chuteInputPos = util.vector().topOf(chutePos).add(1, 1, 0); // 从后面放置物品
        ElementLink<EntityElement> chuteItems = scene.world().createItemEntity(chuteInputPos, Vec3.ZERO, ironIngots);
        scene.idle(8);
        scene.world().modifyEntity(chuteItems, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(16);

        // 物品从磁性溜槽落到滑轨上并开始滑动
        Vec3 railItemPos = util.vector().centerOf(chutePos).add(-1, 0, 0);
        scene.world().createItemEntity(railItemPos, MagneticChuteBlockEntity.getOutputSpeed(Direction.WEST), ironIngots);

        scene.overlay().showText(40)
            .text("Items can slide infinitely far until they reach the end of the rail or are collected")
            .pointAt(util.vector().topOf(util.grid().at(6, 1, 4)))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        scene.markAsFinished();
    }

    // 演示方块在滑轨上滑行
    private static void blockSliding(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sliding_rail_blocks", "Blocks on sliding rails");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(20);

        Selection railsSection = util.select().fromTo(1, 1, 4, 7, 1, 4);
        BlockPos pistonPos = util.grid().at(8, 2, 4);
        BlockPos leverPos = pistonPos.below();
        BlockPos headPos = pistonPos.west();
        BlockPos stonePos = headPos.west();

        // 创建一条长滑轨
        scene.world().setBlocks(railsSection, ModBlocks.SLIDING_RAIL.getDefaultState(), false);
        scene.world().showSection(railsSection, Direction.DOWN);
        scene.idle(10);

        // 在滑轨一端放置活塞
        scene.world().setBlock(pistonPos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().position(pistonPos), Direction.DOWN);

        //在活塞下放置拉杆
        scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
        scene.world().showSection(util.select().position(leverPos), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(40)
            .text("Blocks can also slide on sliding rails when pushed by pistons")
            .pointAt(pistonPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 在滑轨上放置石头
        scene.world().setBlock(headPos, Blocks.STONE.defaultBlockState(), false);
        scene.world().showSection(util.select().position(headPos), Direction.DOWN);
        scene.idle(30);
        scene.world().hideSection(util.select().position(headPos), Direction.WEST);
        // 激活杠杆
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);

        // 模拟方块推动
        scene.world().setBlock(stonePos, Blocks.STONE.defaultBlockState(), false);
        ElementLink<WorldSectionElement> stoneLink2 = scene.world().showIndependentSection(util.select().position(stonePos), Direction.WEST);
        scene.world().moveSection(stoneLink2, new Vec3(-6, 0, 0), 30);

        // 激活活塞
        scene.world().modifyBlock(pistonPos, state -> state.setValue(BlockStateProperties.EXTENDED, true), false);
        ElementLink<WorldSectionElement> headLink = scene.world().showIndependentSection(util.select().position(headPos), Direction.WEST);
        scene.world().setBlock(headPos, Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        scene.idle(40);

        // 重置拉杆
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().hideIndependentSection(headLink, Direction.EAST);
        scene.idle(15);

        // 重置活塞
        scene.world().modifyBlock(pistonPos, state -> state.setValue(BlockStateProperties.EXTENDED, false), false);
        scene.idle(20);
        scene.world().hideIndependentSection(stoneLink2, Direction.UP);
        scene.idle(10);

        // 放置铁砧
        scene.world().setBlock(headPos, Blocks.ANVIL.defaultBlockState(), false);
        scene.world().showIndependentSection(util.select().position(headPos), Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(40)
            .text("However, blocks that cannot be pushed by pistons also cannot slide on rails")
            .pointAt(util.vector().centerOf(headPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 激活拉杆但活塞和铁砧不动
        scene.world().modifyBlock(leverPos, state -> state.setValue(LeverBlock.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);
        scene.idle(20);

        scene.markAsFinished();
    }

    // 演示多方块结构在滑轨上滑行
    private static void multiBlockSliding(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sliding_rail_multi_blocks", "Multiple blocks on sliding rails");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(20);

        BlockPos pistonPos = util.grid().at(8, 2, 4);
        BlockPos leverPos = pistonPos.below();
        BlockPos slimePos = pistonPos.west();
        BlockPos stone = slimePos.above();
        BlockPos headPos = pistonPos.west();

        // 创建一条长滑轨
        Selection railsSection = util.select().fromTo(new BlockPos(1, 1, 4), new BlockPos(7, 1, 4));
        scene.world().setBlocks(railsSection, ModBlocks.SLIDING_RAIL.getDefaultState(), false);
        scene.world().showSection(railsSection, Direction.DOWN);
        scene.idle(20);

        // 放置活塞和拉杆
        scene.world().setBlock(pistonPos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
        scene.world().showSection(util.select().position(pistonPos), Direction.DOWN);
        scene.world().showSection(util.select().position(leverPos), Direction.DOWN);

        scene.overlay().showText(40)
            .text("Sliding rails can handle multiple blocks")
            .pointAt(railsSection.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 放置结构
        Selection multiBlock1 = util.select().position(slimePos).add(util.select().position(stone));
        scene.world().setBlock(slimePos, Blocks.SLIME_BLOCK.defaultBlockState(), false);
        scene.world().setBlock(stone, Blocks.STONE.defaultBlockState(), false);
        ElementLink<WorldSectionElement> multiLink1 = scene.world().showIndependentSection(multiBlock1, Direction.DOWN);

        scene.overlay().showText(40)
            .text("Slime blocks can stick multiple blocks together to form a sliding structure")
            .pointAt(util.vector().centerOf(slimePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 激活拉杆
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);

        // 移动结构
        scene.world().moveSection(multiLink1, new Vec3(-1, 0, 0), 5);
        scene.idle(5);
        scene.world().setBlocks(multiBlock1, Blocks.AIR.defaultBlockState(), false);
        scene.world().moveSection(multiLink1, new Vec3(1, 0, 0), 0);

        Selection multiBlock2 = util.select().position(slimePos.west()).add(util.select().position(stone.west()));
        scene.world().setBlock(slimePos.west(), Blocks.SLIME_BLOCK.defaultBlockState(), false);
        scene.world().setBlock(stone.west(), Blocks.STONE.defaultBlockState(), false);
        ElementLink<WorldSectionElement> multiLink2 = scene.world().showIndependentSection(multiBlock2, Direction.WEST);

        scene.world().moveSection(multiLink2, new Vec3(-6, 0, 0), 30);

        // 激活活塞
        scene.world().modifyBlock(pistonPos, state -> state.setValue(BlockStateProperties.EXTENDED, true), false);
        scene.world().setBlock(headPos, Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        scene.idle(50);

        scene.overlay().showText(40)
            .text("The entire multi-block structure slides together along the rails")
            .pointAt(pistonPos.west(8).getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        scene.markAsFinished();
    }
}
