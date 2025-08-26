package dev.dubhe.anvilcraft.integration.ponder.scene.redstone;


import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.block.BlockPlacerBlock;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderTags;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class BlockPlacerScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registrationHelper.withKeyFunction(RegistryEntry::getId);
        helper.forComponents(ModBlocks.BLOCK_PLACER)
            .addStoryBoard(
                "platform/555",
                BlockPlacerScene::run,
                AnvilCraftPonderTags.REDSTONE_COMPONENTS
            )
            .addStoryBoard(
                "platform/777",
                BlockPlacerScene::anvilRun,
                AnvilCraftPonderTags.POWER_COMPONENTS
            );
    }

    private static void run(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("block_placer", "Block Placer");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        // 初始化
        BlockPos placerPos = util.grid().at(2, 1, 2);
        scene.world().setBlock(placerPos, ModBlocks.BLOCK_PLACER.getDefaultState(), false);
        scene.world().showIndependentSection(util.select().position(placerPos), Direction.DOWN);

        BlockPos signPos = util.grid().at(1, 1, 2);
        BlockPos frontPos = util.grid().at(2, 1, 1);
        BlockPos backPos = util.grid().at(2, 1, 3);

        scene.world().showSection(util.select().position(signPos), Direction.DOWN);
        scene.world().showSection(util.select().position(frontPos), Direction.DOWN);
        scene.world().showSection(util.select().position(backPos), Direction.DOWN);
        scene.idle(10);

        // 将身后的掉落物作为方块放置在前方
        ElementLink<EntityElement> itemLink = scene.world()
            .createItemEntity(backPos.above().getCenter(), Vec3.ZERO, new ItemStack(Items.GRASS_BLOCK));
        scene.idle(30);
        scene.world().setBlock(signPos, Blocks.REDSTONE_TORCH.defaultBlockState(), false);
        scene.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, true), false);
        scene.world().setBlock(frontPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.world().modifyEntity(itemLink, item -> item.remove(Entity.RemovalReason.DISCARDED));
        scene.effects().indicateSuccess(frontPos);  // 绿色粒子
        scene.idle(10);
        scene.overlay().showText(50)
            .text("Block Placer can place blocks in front of it when powered by redstone.")
            .pointAt(util.vector().blockSurface(placerPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(60);

        // 将身后容器中的方块放置
        scene.world().setBlock(signPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(frontPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, false), false);
        scene.idle(10);

        scene.world().setBlock(backPos, Blocks.CHEST.defaultBlockState(), false);
        scene.idle(20);
        scene.world().setBlock(signPos, Blocks.REDSTONE_TORCH.defaultBlockState(), false);
        scene.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, true), false);
        scene.world().setBlock(frontPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.overlay().showText(40)
            .text("It can also read the items in the container.")
            .pointAt(util.vector().blockSurface(frontPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 被生物堵塞
        scene.world().setBlock(signPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(frontPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, false), false);
        scene.idle(10);

        scene.world().createEntity(world -> {
            Pig pig = EntityType.PIG.create(world);
            if (pig != null) {
                pig.moveTo(frontPos.getBottomCenter());
            }
            return pig;
        });
        scene.idle(30);
        scene.world().setBlock(signPos, Blocks.REDSTONE_TORCH.defaultBlockState(), false);
        scene.world().modifyBlock(placerPos, blockState -> blockState.setValue(BlockPlacerBlock.TRIGGERED, true), false);
        scene.effects().indicateRedstone(frontPos);  // 红色粒子
        scene.idle(10);
        scene.overlay().showText(60)
            .text("If the block placer is blocked by a mob, it will not place the block.")
            .pointAt(util.vector().blockSurface(frontPos, Direction.DOWN))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    private static void anvilRun(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("block_placer_anvil", "Place with anvil");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.scaleSceneView(0.8f);

        // 初始化
        BlockPos placerAPos = util.grid().at(3, 1, 3);
        BlockPos placerBPos = util.grid().at(3, 1, 4);
        BlockPos backPos = util.grid().at(3, 1, 5);
        BlockPos anvilAPos = util.grid().at(3, 3, 3);
        BlockPos anvilBPos = util.grid().at(3, 3, 4);
        BlockPos frontAPos = util.grid().at(3, 1, 1);
        BlockPos frontBPos = util.grid().at(3, 1, 2);

        scene.world().setBlock(placerBPos, ModBlocks.BLOCK_PLACER.getDefaultState(), false);
        scene.world().showIndependentSection(util.select().position(placerBPos), Direction.DOWN);

        scene.world().setBlock(anvilBPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilBLink =
            scene.world().showIndependentSection(util.select().position(anvilBPos), Direction.DOWN);

        scene.world().showSection(util.select().position(frontAPos), Direction.DOWN);
        scene.world().showSection(util.select().position(frontBPos), Direction.DOWN);
        scene.idle(20);

        // 铁砧敲击放置
        scene.world().setBlock(backPos, Blocks.CHEST.defaultBlockState(), false);
        scene.world().showIndependentSection(util.select().position(backPos), Direction.DOWN);
        scene.idle(10);

        scene.world().moveSection(anvilBLink, new Vec3(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(frontBPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.overlay().showText(40)
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
        scene.overlay().showText(60)
            .text("The higher the anvil falls, the farther the blocks are placed. The farthest is 5 grids.")
            .pointAt(util.vector().centerOf(frontAPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);

        // 可以连锁的从身后的容器或物品堆中获取物品
        scene.world().setBlock(frontAPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().moveSection(anvilBLink, new Vec3(0, 1, 0), 10);

        scene.world().setBlock(placerAPos, ModBlocks.BLOCK_PLACER.getDefaultState(), false);
        scene.world().showIndependentSection(util.select().position(placerAPos), Direction.DOWN);

        scene.world().setBlock(anvilAPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilALink =
            scene.world().showIndependentSection(util.select().position(anvilAPos), Direction.DOWN);
        scene.idle(30);

        scene.world().moveSection(anvilALink, new Vec3(0, -1, 0), 3);
        scene.world().moveSection(anvilBLink, new Vec3(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(frontAPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.world().setBlock(frontBPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
        scene.overlay().showText(60)
            .text("When the placer is followed by another placer, they can share containers.")
            .pointAt(util.vector().centerOf(placerAPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);

    }
}