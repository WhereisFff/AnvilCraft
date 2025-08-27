package dev.dubhe.anvilcraft.integration.ponder.scene.recipe;

import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.integration.ponder.api.AnvilCraftSceneBuilder;
import dev.dubhe.anvilcraft.integration.ponder.api.instruction.Interpolation;
import dev.dubhe.anvilcraft.util.CauldronUtil;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockRecipeScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<Item> helper = registrationHelper.withKeyFunction(BuiltInRegistries.ITEM::getKey);
        helper.forComponents(
                Items.ANVIL,
                Items.CHIPPED_ANVIL,
                Items.DAMAGED_ANVIL
            )
            .addStoryBoard(
                "platform/555",
                BlockRecipeScene::crafting
            )
            .addStoryBoard(
                "platform/555",
                BlockRecipeScene::processing
            );
    }

    private static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("block_recipe", "The Anvil Hit The Block");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        BlockPos anvilPos = util.grid().at(2, 3, 2);
        BlockPos downPos = util.grid().at(2, 1, 2);
        BlockPos upPos = util.grid().at(2, 2, 2);
        ElementLink<EntityElement> itemLink;
        scene.world().showSection(util.select().position(upPos), Direction.NORTH);

        // 方块粉碎
        scene.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilLink = scene.world().showIndependentSection(util.select().position(anvilPos), Direction.DOWN);

        scene.world().setBlock(downPos, Blocks.COBBLESTONE.defaultBlockState(), false);
        scene.world().showSection(util.select().position(downPos), Direction.NORTH);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(downPos, Blocks.GRAVEL.defaultBlockState(), true);
        scene.idle(10);

        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(10);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(downPos, Blocks.SAND.defaultBlockState(), true);
        scene.idle(10);

        scene.overlay().showText(40)
            .text("When the anvil hits a specific block, the block is crushed.")
            .pointAt(downPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(60);
        // 复位
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.world().setBlock(downPos, Blocks.AIR.defaultBlockState(), false);
        scene.idle(10);

        // 物品压入方块
        scene.world().setBlock(downPos, Blocks.SHULKER_BOX.defaultBlockState(), false);
        itemLink = scene.world().createItemEntity(upPos.getCenter(), Vec3.ZERO, Items.SHULKER_BOX.getDefaultInstance());
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(downPos, ModBlocks.NESTING_SHULKER_BOX.getDefaultState(), true);
        scene.world().modifyEntity(itemLink, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(10);
        scene.overlay().showText(60)
            .text("When the anvil hits the block with an item on it, press the item into the block.")
            .pointAt(downPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);
        // 复位
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.world().setBlock(downPos, Blocks.AIR.defaultBlockState(), false);
        scene.idle(10);

        // 方块破坏
        scene.world().setBlock(downPos, Blocks.STONECUTTER.defaultBlockState(), false);
        scene.world().setBlock(upPos, Blocks.STONE.defaultBlockState(), false);
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(upPos, Blocks.AIR.defaultBlockState(), true);
        itemLink = scene.world().createItemEntity(upPos.getCenter(), Vec3.ZERO, Items.COBBLESTONE.getDefaultInstance());
        scene.idle(10);
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(10);
        scene.overlay().showText(60)
            .text("When the anvil hit the block on the stone cutter, the block was destroyed.")
            .pointAt(upPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);
        scene.world().modifyEntity(itemLink, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        // 皇家铁砧: 精准采集
        scene.world().setBlock(anvilPos, ModBlocks.ROYAL_ANVIL.getDefaultState(), false);
        scene.world().setBlock(upPos, Blocks.STONE.defaultBlockState(), false);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(upPos, Blocks.AIR.defaultBlockState(), true);
        itemLink = scene.world().createItemEntity(upPos.getCenter(), Vec3.ZERO, Items.STONE.getDefaultInstance());
        scene.idle(10);
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(10);
        scene.overlay().showText(40)
            .text("The Royal Anvil can precisely destroy blocks.")
            .pointAt(upPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);
        scene.world().modifyEntity(itemLink, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        // 余烬铁砧：熔炼
        scene.world().setBlock(anvilPos, ModBlocks.EMBER_ANVIL.getDefaultState(), false);
        scene.world().setBlock(upPos, Blocks.IRON_ORE.defaultBlockState(), false);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(upPos, Blocks.AIR.defaultBlockState(), true);
        itemLink = scene.world().createItemEntity(upPos.getCenter(), Vec3.ZERO, Items.IRON_INGOT.getDefaultInstance());
        scene.idle(10);
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(10);
        scene.overlay().showText(40)
            .text("The Ember Anvil can melt blocks.")
            .pointAt(upPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);
        scene.world().modifyEntity(itemLink, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        // 复位
        scene.world().setBlock(downPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        scene.idle(10);

        // 方块压合
        scene.world().setBlock(downPos, Blocks.ICE.defaultBlockState(), false);
        scene.world().setBlock(upPos, Blocks.ICE.defaultBlockState(), false);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(upPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(downPos, Blocks.PACKED_ICE.defaultBlockState(), true);
        scene.idle(3);
        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(10);
        scene.overlay().showText(40)
            .text("The anvil can compress blocks.")
            .pointAt(downPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);
        // 复位
        scene.world().moveSection(anvilLink, util.vector().of(0, 2, 0), 5);
        scene.world().setBlock(downPos, Blocks.AIR.defaultBlockState(), false);
        scene.idle(10);

        // 方块涂抹
        scene.world().setBlock(downPos, Blocks.COBBLESTONE.defaultBlockState(), false);
        scene.world().setBlock(upPos, Blocks.MOSS_BLOCK.defaultBlockState(), false);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(downPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), false);
        scene.idle(10);
        scene.overlay().showText(40)
            .text("The anvil can smear blocks.")
            .pointAt(downPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);
        // 复位
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.world().setBlock(downPos, Blocks.AIR.defaultBlockState(), false);
        scene.idle(10);

        // 方块压榨
        scene.world().setBlock(downPos, Blocks.CAULDRON.defaultBlockState(), false);
        scene.world().setBlock(upPos, Blocks.SNOW_BLOCK.defaultBlockState(), false);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(upPos, Blocks.ICE.defaultBlockState(), false);
        scene.world().setBlock(downPos, CauldronUtil.getStateFromContentAndLevel(Blocks.POWDER_SNOW_CAULDRON, 1), false);
        scene.idle(10);
        scene.overlay().showText(40)
            .text("The anvil can squeeze blocks.")
            .pointAt(downPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);
        // 复位
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.world().setBlock(downPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().setBlock(upPos, Blocks.AIR.defaultBlockState(), false);
        scene.idle(10);
    }

    private static void processing(SceneBuilder scene, SceneBuildingUtil util) {
        AnvilCraftSceneBuilder builder = new AnvilCraftSceneBuilder(scene);
        scene.title("block_process", "Use anvil to processing");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        BlockPos anvilPos = util.grid().at(2, 3, 2);
        BlockPos blockPos = util.grid().at(2, 1, 2);

        // 强制刷怪
        scene.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilLink = scene.world().showIndependentSection(util.select().position(anvilPos), Direction.DOWN);

        scene.world().setBlock(blockPos, Blocks.SPAWNER.defaultBlockState(), false);
        scene.world().showSection(util.select().position(blockPos), Direction.NORTH);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(13);
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(10);
        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);

        List<ElementLink<EntityElement>> pigs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            BlockPos pigPos = blockPos.east(new Random().nextInt(5) - 2).north(new Random().nextInt(5) - 2);
            pigs.add(
                scene.world().createEntity(world -> {
                    Pig pig = EntityType.PIG.create(world);
                    if (pig != null) {
                        pig.moveTo(pigPos.getBottomCenter());
                    }
                    return pig;
                })
            );
            scene.effects().indicateSuccess(pigPos);
        }
        scene.idle(10);

        scene.overlay().showText(100)
            .text(
                "When the anvil hits the spawner, it will be forced to work. But there are still constraints, such as light, number of mob.")
            .pointAt(blockPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(110);
        for (ElementLink<EntityElement> pig : pigs) {
            scene.world().modifyEntity(pig, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
        pigs.clear();
        scene.idle(10);
        // 高度越高，成功概率越大
        scene.world().moveSection(anvilLink, util.vector().of(0, 4, 0), 10);
        scene.idle(20);
        builder.world().moveSectionInterpolation(anvilLink, util.vector().of(0, -4, 0), Interpolation.acceleration(0.05));
        scene.idle(4);

        for (int i = 0; i < 4; i++) {
            BlockPos pigPos = blockPos.east(new Random().nextInt(5) - 2).north(new Random().nextInt(5) - 2);
            pigs.add(
                scene.world().createEntity(world -> {
                    Pig pig = EntityType.PIG.create(world);
                    if (pig != null) {
                        pig.moveTo(pigPos.getBottomCenter());
                    }
                    return pig;
                })
            );
            scene.effects().indicateSuccess(pigPos);
        }
        scene.idle(10);

        scene.overlay().showText(60)
            .text("The higher the height of the anvil, the higher the probability of success.")
            .pointAt(blockPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);
        for (ElementLink<EntityElement> pig : pigs) {
            scene.world().modifyEntity(pig, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
        // 复位
        pigs.clear();
        scene.world().setBlock(blockPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().moveSection(anvilLink, util.vector().of(0, 1, 0), 5);
        scene.idle(10);

        // 红石EMP
        final BlockPos[] redstonePos = {
            new BlockPos(0, 1, 1),
            new BlockPos(1, 1, 1),
            new BlockPos(0, 1, 2),
            new BlockPos(1, 1, 2),
            new BlockPos(0, 1, 3),
            new BlockPos(1, 1, 3)
        };

        // 在每个位置放置红石火把
        for (BlockPos pos : redstonePos) {
            scene.world().setBlock(pos, Blocks.REDSTONE_TORCH.defaultBlockState(), false);
            scene.world().showSection(util.select().position(pos), Direction.NORTH);
        }
        scene.world().setBlock(blockPos, Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
        scene.idle(10);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        for (BlockPos pos : redstonePos) {
            scene.world().modifyBlock(pos, state -> state.setValue(RedstoneTorchBlock.LIT, false), false);
        }
        scene.idle(2);
        for (BlockPos pos : redstonePos) {
            scene.world().modifyBlock(pos, state -> state.setValue(RedstoneTorchBlock.LIT, true), false);
        }
        scene.idle(10);

        scene.overlay().showText(100)
            .text("When the anvil strikes the red stone, a red stone EMP occurs, extinguishing the nearby red stone torches for an instant.")
            .pointAt(blockPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(110);
        scene.overlay().showText(60)
            .text("The higher the anvil falls, the larger the range.")
            .pointAt(anvilPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(70);
        // 复位
        for (BlockPos pos : redstonePos) {
            scene.world().setBlock(pos, Blocks.AIR.defaultBlockState(), false);
        }
        scene.world().setBlock(blockPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().moveSection(anvilLink, util.vector().of(0, 2, 0), 5);
        scene.idle(10);

        // 宝库重置
        scene.world().setBlock(blockPos, Blocks.VAULT.defaultBlockState(), false);
        BlockPos leadPos = util.grid().at(2, 2, 2);
        scene.world().setBlock(leadPos, ModBlocks.LEAD_BLOCK.getDefaultState(), false);
        scene.world().showSection(util.select().position(leadPos), Direction.NORTH);
        scene.idle(20);

        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.world().setBlock(leadPos, Blocks.AIR.defaultBlockState(), false);
        scene.world().modifyBlock(blockPos, state -> state.setValue(VaultBlock.STATE, VaultState.ACTIVE), false);
        scene.idle(3);
        scene.world().moveSection(anvilLink, util.vector().of(0, -1, 0), 3);
        scene.idle(3);
        scene.overlay().showText(40)
            .text("Press the lead into the vault to reset it.")
            .pointAt(blockPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        scene.markAsFinished();
    }
}

