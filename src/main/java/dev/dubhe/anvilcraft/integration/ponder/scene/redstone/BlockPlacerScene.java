package dev.dubhe.anvilcraft.integration.ponder.scene.redstone;


import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.block.BlockPlacerBlock;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderTags;
import dev.dubhe.anvilcraft.integration.ponder.api.AnvilCraftSceneBuilder;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class BlockPlacerScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registrationHelper.withKeyFunction(RegistryEntry::getId);
        helper.forComponents(ModBlocks.BLOCK_PLACER)
            .addStoryBoard("platform/5x", BlockPlacerScene::run, AnvilCraftPonderTags.REDSTONE_COMPONENTS)
            .addStoryBoard("platform/7x", BlockPlacerScene::anvilRun, AnvilCraftPonderTags.POWER_COMPONENTS);
    }

    private static void run(SceneBuilder scene, SceneBuildingUtil util) {
        AnvilCraftSceneBuilder builder = new AnvilCraftSceneBuilder(scene);
        builder.title("block_placer", "Block Placer");
        builder.configureBasePlate(0, 0, 5);
        builder.showBasePlate();
        builder.idle(20);

        // 初始化
        BlockPos placerPos = util.grid().at(2, 1, 2);
        builder.world().setBlock(placerPos, ModBlocks.BLOCK_PLACER.getDefaultState(), false);
        Selection placerSelection = util.select().position(placerPos);
        builder.world().showSection(placerSelection, Direction.DOWN);
        builder.idle(20);

        BlockPos leverPos = placerPos.west(1);
        BlockPos frontPos = placerPos.north(1);
        BlockPos backPos = placerPos.south(1);

        // 添加红石控制
        builder.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);

        Selection leverSelection = util.select().position(leverPos);
        Selection frontSelection = util.select().position(frontPos);
        Selection backSelection = util.select().position(backPos);

        builder.world().showSection(leverSelection, Direction.DOWN);
        builder.world().showSection(frontSelection, Direction.DOWN);
        builder.world().showSection(backSelection, Direction.DOWN);
        builder.idle(20);

        // 将身后的掉落物作为方块放置在前方
        ElementLink<EntityElement> itemLink = builder.world()
            .createItemEntity(backPos.above().getCenter(), Vec3.ZERO, new ItemStack(Items.GRASS_BLOCK));
        builder.idle(20);

        // 激活杠杆
        builder.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        builder.effects().indicateRedstone(leverPos);

        builder.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, true), false);
        builder.world().setBlock(frontPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        builder.world().removeEntity(itemLink);
        // 绿色粒子
        builder.effects().indicateSuccess(frontPos);
        builder.idle(10);
        builder.overlay()
            .showText(40)
            .text("Block Placer can place blocks in front of it when powered by redstone.")
            .pointAt(util.vector().blockSurface(placerPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        builder.idle(50);

        // 将身后容器中的方块放置在前方
        builder.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        builder.world().hideSection(frontSelection, Direction.UP);
        builder.idle(10);
        builder.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, false), false);
        builder.idle(10);

        builder.world().setBlock(backPos, Blocks.CHEST.defaultBlockState(), false);
        builder.idle(20);

        builder.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        builder.effects().indicateRedstone(leverPos);
        builder.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, true), false);
        builder.world().setBlock(frontPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);

        ElementLink<WorldSectionElement> frontLink = builder.world().showIndependentSectionImmediately(frontSelection);

        builder.overlay()
            .showText(40)
            .text("It can also read the items in the container.")
            .pointAt(util.vector().blockSurface(frontPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        builder.idle(50);

        // 被生物堵塞
        builder.world().hideIndependentSection(frontLink, Direction.UP);
        builder.idle(10);
        builder.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, false), false);
        builder.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, false), false);
        builder.idle(10);

        // 创建猪实体
        builder.world().createEntity(world -> {
            Pig pig = EntityType.PIG.create(world);
            if (pig != null) {
                pig.moveTo(frontPos.getBottomCenter());
            }
            return pig;
        });
        builder.idle(30);

        builder.world().modifyBlock(leverPos, state -> state.setValue(BlockStateProperties.POWERED, true), false);
        builder.effects().indicateRedstone(leverPos);
        builder.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, true), false);
        builder.idle(10);

        builder.overlay()
            .showText(60)
            .text("If the block placer is blocked by a mob, it will not place the block.")
            .pointAt(util.vector().blockSurface(frontPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        builder.idle(70);

        builder.markAsFinished();
    }

    private static void anvilRun(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("block_placer_anvil", "Place with anvil");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.scaleSceneView(0.8f);
        scene.idle(20);

        // 初始化
        BlockPos placerAPos = util.grid().at(3, 1, 3);
        BlockPos placerBPos = util.grid().at(3, 1, 4);
        BlockPos backPos = util.grid().at(3, 1, 5);
        BlockPos anvilAPos = util.grid().at(3, 3, 3);
        BlockPos anvilBPos = util.grid().at(3, 3, 4);
        BlockPos frontAPos = util.grid().at(3, 1, 1);
        BlockPos frontBPos = util.grid().at(3, 1, 2);

        // 使用普通Section替代IndependentSection以保持一致的动画效果
        scene.world().setBlock(placerBPos, ModBlocks.BLOCK_PLACER.getDefaultState(), false);
        Selection placerBSelection = util.select().position(placerBPos);
        scene.world().showSection(placerBSelection, Direction.DOWN);

        scene.world().setBlock(anvilBPos, Blocks.ANVIL.defaultBlockState(), false);
        Selection anvilBSelection = util.select().position(anvilBPos);
        ElementLink<WorldSectionElement> anvilBLink = scene.world().showIndependentSection(anvilBSelection, Direction.DOWN);

        Selection frontASelection = util.select().position(frontAPos);
        Selection frontBSelection = util.select().position(frontBPos);
        scene.world().showSection(frontASelection, Direction.DOWN);
        scene.world().showSection(frontBSelection, Direction.DOWN);
        scene.idle(20);

        // 铁砧敲击放置
        scene.world().setBlock(backPos, Blocks.CHEST.defaultBlockState(), false);
        Selection backSelection = util.select().position(backPos);
        scene.world().showSection(backSelection, Direction.DOWN);
        scene.idle(10);

        scene.world().moveSection(anvilBLink, new Vec3(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(frontBPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.overlay()
            .showText(40)
            .text("Block Placer can place blocks with anvil.")
            .pointAt(util.vector().centerOf(placerBPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 从越高的地方砸下来，放置方块越远
        scene.world().setBlock(frontBPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().moveSection(anvilBLink, new Vec3(0, 2, 0), 10);
        scene.idle(20);

        scene.world().moveSection(anvilBLink, new Vec3(0, -2, 0), 5);
        scene.idle(5);
        scene.world().setBlock(frontAPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.overlay()
            .showText(60)
            .text("The higher the anvil falls, the farther the blocks are placed. The farthest is 5 grids.")
            .pointAt(util.vector().centerOf(frontAPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);

        // 可以连锁的从身后的容器或物品堆中获取物品
        scene.world().setBlock(frontAPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().moveSection(anvilBLink, new Vec3(0, 1, 0), 10);

        scene.world().setBlock(placerAPos, ModBlocks.BLOCK_PLACER.getDefaultState(), false);
        Selection placerASelection = util.select().position(placerAPos);
        scene.world().showSection(placerASelection, Direction.DOWN);

        scene.world().setBlock(anvilAPos, Blocks.ANVIL.defaultBlockState(), false);
        Selection anvilASelection = util.select().position(anvilAPos);
        ElementLink<WorldSectionElement> anvilALink = scene.world().showIndependentSection(anvilASelection, Direction.DOWN);
        scene.idle(30);

        scene.world().moveSection(anvilALink, new Vec3(0, -1, 0), 3);
        scene.world().moveSection(anvilBLink, new Vec3(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(frontAPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.world().setBlock(frontBPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.overlay()
            .showText(60)
            .text("When the placer is followed by another placer, they can share containers.")
            .pointAt(util.vector().centerOf(placerAPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }
}