package dev.dubhe.anvilcraft.integration.ponder.scene.logistics;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.block.MagneticChuteBlock;
import dev.dubhe.anvilcraft.block.entity.MagneticChuteBlockEntity;
import dev.dubhe.anvilcraft.entity.SlidingBlockEntity;
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
            .addStoryBoard("platform/999", SlidingRailScene::itemSliding)
            .addStoryBoard("platform/999", SlidingRailScene::blockSliding)
            .addStoryBoard("platform/999", SlidingRailScene::multiBlockSliding);
    }

    // 演示物品在滑轨上滑行
    private static void itemSliding(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sliding_rail_items", "Items on sliding rails");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(20);

        int distance = 5;
        BlockPos railStartPos = util.grid().at(1, 1, 4);
        BlockPos railEndPos = railStartPos.east(distance);
        Selection railsSection = util.select().fromTo(railStartPos, railEndPos);
        BlockPos chutePos = railEndPos.east();
        Vec3 chuteInputPos = util.vector().topOf(chutePos).add(1, 1, 0);
        Vec3 railItemPos = util.vector().centerOf(chutePos.west());
        ItemStack ironIngots = new ItemStack(Items.IRON_INGOT, 64);

        // 创建一条长滑轨
        scene.world().setBlocks(railsSection, ModBlocks.SLIDING_RAIL.getDefaultState(), false);
        scene.world().showSection(railsSection, Direction.DOWN);
        scene.idle(10);

        // 放置磁性溜槽在滑轨旁边
        scene.world().setBlock(chutePos, ModBlocks.MAGNETIC_CHUTE.getDefaultState().setValue(MagneticChuteBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().position(chutePos), Direction.DOWN);
        scene.idle(10);

        // 旋转视角
        scene.rotateCameraY(-45);
        scene.idle(20);

        scene.overlay().showText(40)
            .text("Sliding rails have extremely smooth surfaces that allow items to slide without friction")
            .pointAt(railsSection.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 向磁性溜槽添加物品
        ElementLink<EntityElement> chuteItems = scene.world().createItemEntity(chuteInputPos, Vec3.ZERO, ironIngots);
        scene.idle(8);
        scene.world().modifyEntity(chuteItems, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(16);

        // 输出物品
        scene.world().createItemEntity(railItemPos, MagneticChuteBlockEntity.getOutputSpeed(Direction.WEST), ironIngots);

        scene.overlay().showText(40)
            .text("Items can slide infinitely far until they reach the end of the rail or are collected")
            .pointAt(railItemPos)
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

        int distance = 6;
        BlockPos railStartPos = util.grid().at(1, 1, 4);
        BlockPos railEndPos = railStartPos.east(distance);
        Selection railsSection = util.select().fromTo(railStartPos, railEndPos);
        BlockPos pistonPos = railEndPos.east().above();
        BlockPos pistonHeadPos = pistonPos.above();
        BlockPos leverPos = pistonPos.below();
        BlockPos stonePos = pistonPos.west();

        // 创建一条长滑轨
        scene.world().setBlocks(railsSection, ModBlocks.SLIDING_RAIL.getDefaultState(), false);
        scene.world().showSection(railsSection, Direction.DOWN);
        scene.idle(5);

        // 在滑轨一端放置活塞
        scene.world().setBlock(pistonPos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().position(pistonPos), Direction.DOWN);
        scene.idle(5);

        // 在活塞下放置拉杆
        scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
        scene.world().showSection(util.select().position(leverPos), Direction.DOWN);
        scene.idle(5);

        // 在滑轨上放置石头
        scene.world().setBlock(stonePos, Blocks.STONE.defaultBlockState(), false);
        ElementLink<WorldSectionElement> stone = scene.world().showIndependentSection(util.select().position(stonePos), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(40)
            .text("Blocks can also slide on sliding rails when pushed by pistons")
            .pointAt(stonePos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 激活杠杆和活塞
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);
        scene.world().modifyBlock(pistonPos, state -> state.setValue(PistonBaseBlock.EXTENDED, true), false);

        // 放置活塞头至活塞处
        scene.world().setBlock(pistonHeadPos, Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        ElementLink<WorldSectionElement> pistonHead = scene.world().showIndependentSectionImmediately(util.select().position(pistonHeadPos));
        scene.world().moveSection(pistonHead, new Vec3(0, -1, 0), 0);

        // 推出活塞头和其他方块
        Vec3 offset = new Vec3(-1, 0, 0);
        scene.world().moveSection(pistonHead, offset, 2);
        scene.world().moveSection(stone, offset, 2);
        scene.idle(4);

        // 其他方块继续滑动
        scene.world().moveSection(stone, new Vec3(-distance, 0, 0), (int) (distance / SlidingBlockEntity.DEFAULT_MOVEMENT));
        scene.idle(30);

        // 移除其他方块
        scene.world().hideIndependentSection(stone, Direction.UP);
        scene.idle(20);

        // 恢复拉杆，活塞头，活塞
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        scene.world().moveSection(pistonHead, offset.reverse(), 2);
        scene.idle(2);
        scene.world().modifyBlock(pistonPos, state -> state.setValue(PistonBaseBlock.EXTENDED, false), false);
        scene.idle(20);

        // 放置铁砧
        scene.world().setBlock(stonePos, Blocks.ANVIL.defaultBlockState(), false);
        scene.world().showSection(util.select().position(stonePos), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(40)
            .text("However, blocks that cannot be pushed by pistons also cannot slide on rails")
            .pointAt(util.vector().centerOf(stonePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 仅激活拉杆
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);
        scene.idle(10);

        scene.markAsFinished();
    }


    // 演示多方块结构在滑轨上滑行
    private static void multiBlockSliding(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sliding_rail_multi_blocks", "Multiple blocks on sliding rails");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.idle(20);

        int distance = 6;
        BlockPos railStartPos = util.grid().at(1, 1, 4);
        BlockPos railEndPos = railStartPos.east(distance);
        Selection railsSection = util.select().fromTo(railStartPos, railEndPos);
        BlockPos pistonPos = railEndPos.east().above();
        BlockPos pistonHeadPos = pistonPos.above();
        BlockPos leverPos = pistonPos.below();
        BlockPos slimePos = pistonPos.west();
        BlockPos stonePos = slimePos.above();

        // 创建一条长滑轨
        scene.world().setBlocks(railsSection, ModBlocks.SLIDING_RAIL.getDefaultState(), false);
        scene.world().showSection(railsSection, Direction.DOWN);
        scene.idle(5);

        // 在滑轨一端放置活塞
        scene.world().setBlock(pistonPos, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        scene.world().showSection(util.select().position(pistonPos), Direction.DOWN);
        scene.idle(5);

        // 在活塞下放置拉杆
        scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
        scene.world().showSection(util.select().position(leverPos), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(40)
            .text("Sliding rails can handle multiple blocks")
            .pointAt(railsSection.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 在滑轨上放置结构
        scene.world().setBlock(slimePos, Blocks.SLIME_BLOCK.defaultBlockState(), false);
        scene.world().setBlock(stonePos, Blocks.STONE.defaultBlockState(), false);
        ElementLink<WorldSectionElement> structure = scene.world().showIndependentSection(util.select().fromTo(slimePos, stonePos), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(40)
            .text("Slime blocks can stick multiple blocks together to form a sliding structure")
            .pointAt(util.vector().centerOf(slimePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 激活杠杆和活塞
        scene.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);
        scene.world().modifyBlock(pistonPos, state -> state.setValue(PistonBaseBlock.EXTENDED, true), false);

        // 放置活塞头至活塞处
        scene.world().setBlock(pistonHeadPos, Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), false);
        ElementLink<WorldSectionElement> pistonHead = scene.world().showIndependentSectionImmediately(util.select().position(pistonHeadPos));
        scene.world().moveSection(pistonHead, new Vec3(0, -1, 0), 0);

        // 推出活塞头和结构
        Vec3 offset = new Vec3(-1, 0, 0);
        scene.world().moveSection(pistonHead, offset, 2);
        scene.world().moveSection(structure, offset, 2);
        scene.idle(4);

        // 结构继续滑动
        scene.world().moveSection(structure, new Vec3(-distance, 0, 0), (int) (distance / SlidingBlockEntity.DEFAULT_MOVEMENT));
        scene.idle(30);

        scene.markAsFinished();
    }
}
