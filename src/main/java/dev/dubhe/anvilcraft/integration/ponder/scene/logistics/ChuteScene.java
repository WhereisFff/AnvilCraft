package dev.dubhe.anvilcraft.integration.ponder.scene.logistics;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.block.ChuteBlock;
import dev.dubhe.anvilcraft.block.SimpleChuteBlock;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
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
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

public class ChuteScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registrationHelper.withKeyFunction(RegistryEntry::getId);
        helper.forComponents(ModBlocks.CHUTE)
            .addStoryBoard("platform/5x", ChuteScene::basicOperation)
            .addStoryBoard("platform/5x", ChuteScene::simpleChute)
            .addStoryBoard("platform/5x", ChuteScene::filtering);
    }

    // 基本操作展示：对比漏斗和溜槽，演示物品阻塞
    private static void basicOperation(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("chute_basics", "The basics of chute");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(20);

        BlockPos hopperPos = util.grid().at(1, 2, 2);
        BlockPos chutePos = hopperPos.east(2);
        BlockPos chuteTargetPos = chutePos.below();

        Vec3 hopperItemVec = util.vector().topOf(hopperPos).add(0, 1, 0);
        Vec3 chuteItemVec = util.vector().topOf(chutePos).add(0, 1, 0);
        Vec3 targetItemVec = util.vector().topOf(chuteTargetPos);
        Vec3 itemEntityPos = util.vector().centerOf(chuteTargetPos);

        ItemStack ironIngot = new ItemStack(Items.IRON_INGOT, 1);
        ItemStack ironIngots = new ItemStack(Items.IRON_INGOT, 64);
        ItemStack goldIngot = new ItemStack(Items.GOLD_INGOT, 64);

        Selection hopper = util.select().position(hopperPos);
        Selection chute = util.select().position(chutePos);

        // 放置漏斗
        scene.world().setBlock(hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, Direction.DOWN), false);
        scene.world().showIndependentSection(hopper, Direction.DOWN);

        scene.overlay().showText(40)
            .text("A normal funnel can only transmit one item at a time") // 普通的漏斗一次只能传输一个物品
            .pointAt(util.vector().centerOf(hopperPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 放置溜槽
        scene.world().setBlock(chutePos, ModBlocks.CHUTE.getDefaultState().setValue(ChuteBlock.FACING, Direction.DOWN), false);
        scene.world().showIndependentSection(chute, Direction.DOWN);

        scene.overlay().showText(40)
            .text("Whereas chutes can transfer a set of items at once") // 而溜槽可以一次性传输一组物品
            .pointAt(util.vector().centerOf(chutePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 向漏斗添加几个物品
        for (int i = 0; i < 3; i++) {
            ElementLink<EntityElement> hopperItems = scene.world().createItemEntity(hopperItemVec, Vec3.ZERO, ironIngot);
            scene.idle(8);
            // 漏斗吸收物品
            scene.world().modifyEntity(hopperItems, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
        scene.idle(20);

        // 向溜槽添加一组物品
        ElementLink<EntityElement> chuteItem1 = scene.world().createItemEntity(chuteItemVec, Vec3.ZERO, ironIngots);
        scene.idle(8);

        // 溜槽吸收物品
        scene.world().modifyEntity(chuteItem1, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(8);

        // 溜槽一次性输出一组物品
        ElementLink<EntityElement> chuteItem2 = scene.world().createItemEntity(targetItemVec, Vec3.ZERO, ironIngots);
        scene.idle(8);

        scene.overlay().showText(40)
            .text("and can be thrown as drops") // 并且可以将物品作为掉落物投掷出来
            .pointAt(util.vector().centerOf(chuteTargetPos).add(0, -0.5, 0))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 在溜槽下方添加一个方块演示阻塞，同时移除此处的物品
        scene.world().setBlock(chuteTargetPos, Blocks.GLASS.defaultBlockState(), false);
        scene.world().modifyEntity(chuteItem2, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.world().showSection(util.select().position(chuteTargetPos), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(40)
            .text("When the chute output position is blocked by a block, the item will stop outputting") // 当溜槽输出位置被方块阻挡时，物品会停止输出
            .pointAt(util.vector().centerOf(chuteTargetPos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 添加物品到溜槽，但因为被阻塞所以不会输出
        ElementLink<EntityElement> goldItem = scene.world().createItemEntity(chuteItemVec, Vec3.ZERO, goldIngot);
        scene.idle(8);
        scene.world().modifyEntity(goldItem, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(20);

        // 移除方块，金锭被输出
        scene.world().destroyBlock(chuteTargetPos);
        scene.idle(8);
        scene.world().createItemEntity(itemEntityPos, Vec3.ZERO, goldIngot);

        scene.idle(10);

        scene.overlay().showText(40)
            .text("When there is an identical set of drops under the chute, the item will not continue to be thrown") // 当溜槽输出位置被方块阻挡时，物品会停止输出
            .pointAt(itemEntityPos)
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(30);

        // 添加物品到溜槽，但因为下方有相同物品所以不会输出
        ElementLink<EntityElement> chuteItem3 = scene.world().createItemEntity(chuteItemVec, Vec3.ZERO, goldIngot);
        scene.idle(8);
        scene.world().modifyEntity(chuteItem3, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(20);

        scene.markAsFinished();
    }

    // 简易溜槽演示：演示溜槽连接变为简易溜槽，以及红石信号对两种溜槽的不同影响
    private static void simpleChute(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("simple_chute", "Simple chute and normal chute");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(20);

        BlockPos chutePos = util.grid().at(1, 2, 2);
        BlockPos leverPos = util.grid().at(1, 1, 2);
        BlockPos topChutePos = util.grid().at(2, 3, 2);
        BlockPos simplePos = util.grid().at(2, 2, 2);

        Selection simple = util.select().position(simplePos);
        Selection topChute = util.select().position(topChutePos);
        Selection lever = util.select().position(leverPos);

        Vec3 topItemVec = util.vector().topOf(topChutePos).add(0, 1, 0);
        Vec3 targetItemVec = util.vector().topOf(new BlockPos(2, 1, 2));
        Vec3 leftItemVec = util.vector().topOf(chutePos).add(0, 1, 0);

        ItemStack diamonds = new ItemStack(Items.DIAMOND, 16);
        ItemStack emeralds = new ItemStack(Items.EMERALD, 32);
        ItemStack redstoneItems = new ItemStack(Items.REDSTONE, 16);
        ItemStack moreItems = new ItemStack(Items.DIAMOND, 16);

        // 放置一个溜槽
        scene.world().setBlock(chutePos, ModBlocks.CHUTE.getDefaultState().setValue(ChuteBlock.FACING, Direction.EAST), false);
        Selection chute = util.select().position(chutePos);
        scene.world().showIndependentSection(chute, Direction.DOWN);
        scene.idle(20);

        // 放置第二个溜槽（连接到第一个的输出）
        scene.world().setBlock(simplePos, ModBlocks.CHUTE.getDefaultState().setValue(ChuteBlock.FACING, Direction.DOWN), false);
        scene.world().showIndependentSection(simple, Direction.DOWN);
        scene.idle(20);

        // 第二个溜槽变为简易溜槽
        scene.world().modifyBlock(simplePos, state ->
            ModBlocks.SIMPLE_CHUTE.getDefaultState().setValue(SimpleChuteBlock.FACING, Direction.DOWN), false
        );

        scene.overlay().showText(40)
            .text("When one chute is connected to the output direction of another chute, it becomes a simple chute") // 当一个溜槽连接到另一个溜槽的输出方向，它会变成简易溜槽
            .pointAt(util.vector().centerOf(simplePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 在简易溜槽上方放置另一个溜槽
        scene.world().setBlock(topChutePos, ModBlocks.CHUTE.getDefaultState().setValue(ChuteBlock.FACING, Direction.DOWN), false);
        // 同时更新简易溜槽为TALL状态
        scene.world().modifyBlock(simplePos, state ->
            ModBlocks.SIMPLE_CHUTE.getDefaultState().setValue(SimpleChuteBlock.TALL, true), false
        );
        scene.world().showIndependentSection(topChute, Direction.DOWN);
        scene.idle(20);

        // 向上方溜槽添加物品
        ElementLink<EntityElement> topItem = scene.world().createItemEntity(topItemVec, Vec3.ZERO, diamonds);
        scene.idle(8);

        // 上方溜槽吸收物品
        scene.world().modifyEntity(topItem, entity -> entity.remove(Entity.RemovalReason.DISCARDED));

        // 简易溜槽输出物品
        scene.idle(16);
        scene.world().createItemEntity(targetItemVec, Vec3.ZERO, diamonds);

        scene.overlay().showText(40)
            .text("The simple chute can receive items from other chutes from multiple directions") // 简易溜槽可以从多个方向接收其他溜槽的物品
            .pointAt(util.vector().centerOf(simplePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 演示左侧溜槽输送物品到简易溜槽
        ElementLink<EntityElement> leftItem = scene.world().createItemEntity(leftItemVec, Vec3.ZERO, emeralds);
        scene.idle(8);

        // 左侧溜槽吸收物品
        scene.world().modifyEntity(leftItem, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(16);

        // 简易溜槽输出物品
        scene.world().createItemEntity(targetItemVec, Vec3.ZERO, emeralds);
        scene.idle(20);

        // 添加红石控制
        scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
        scene.world().showIndependentSection(lever, Direction.DOWN);
        scene.idle(20);

        // 激活杠杆
        scene.world().toggleRedstonePower(lever);
        scene.effects().indicateRedstone(leverPos);

        // 锁定普通溜槽
        scene.world().modifyBlock(chutePos, state -> state.setValue(ChuteBlock.ENABLED, false), false);

        scene.overlay().showText(40)
            .text("Normal chutes are locked by redstone signals") // 普通溜槽会被红石信号锁定
            .pointAt(util.vector().centerOf(chutePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 添加物品到锁定的溜槽
        scene.world().createItemEntity(leftItemVec, Vec3.ZERO, redstoneItems);
        scene.idle(40);

        // 向上方溜槽添加物品，展示简易溜槽不被锁定
        ElementLink<EntityElement> moreTopItem = scene.world().createItemEntity(topItemVec, Vec3.ZERO, moreItems);
        scene.idle(8);

        // 上方溜槽吸收物品
        scene.world().modifyEntity(moreTopItem, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(16);

        // 简易溜槽输出物品
        scene.world().createItemEntity(targetItemVec, Vec3.ZERO, moreItems);

        scene.overlay().showText(40)
            .text("The simple chute will not be locked by the redstone signal") // 而简易溜槽不会被红石信号锁定
            .pointAt(util.vector().centerOf(simplePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        scene.markAsFinished();
    }

    // 过滤功能演示：演示溜槽的物品过滤功能
    private static void filtering(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("chute_filtering", "Chute item filtration");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(20);

        BlockPos chutePos = util.grid().at(2, 2, 2);

        Vec3 itemDropPos = util.vector().topOf(chutePos).add(0, 1, 0);
        Vec3 targetItemPos = util.vector().topOf(util.grid().at(2, 1, 2));

        ItemStack diamond = new ItemStack(Items.DIAMOND);
        ItemStack iron = new ItemStack(Items.IRON_INGOT);

        // 放置溜槽
        scene.world().setBlock(chutePos, ModBlocks.CHUTE.getDefaultState().setValue(ChuteBlock.FACING, Direction.DOWN), false);
        Selection chute = util.select().position(chutePos);
        scene.world().showIndependentSection(chute, Direction.DOWN);
        scene.idle(20);

        // 演示右键打开UI
        scene.overlay().showControls(
            util.vector().blockSurface(chutePos, Direction.WEST),
            Pointing.RIGHT, 20
        ).rightClick().withItem(Items.AIR.getDefaultInstance());

        scene.overlay().showText(40)
            .text("Right-click the chute to open the filter settings interface") // 右键溜槽可以打开过滤设置界面
            .pointAt(util.vector().blockSurface(chutePos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 设置过滤器
        scene.overlay().showControls(
            util.vector().blockSurface(chutePos, Direction.WEST),
            Pointing.DOWN, 20
        ).withItem(Items.DIAMOND.getDefaultInstance());

        scene.overlay().showText(40)
            .text("Set it allow only diamonds to pass through") // 设置其只允许钻石通过
            .pointAt(util.vector().blockSurface(chutePos, Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        // 钻石（应该通过）
        ElementLink<EntityElement> diamondItem = scene.world().createItemEntity(itemDropPos, Vec3.ZERO, diamond);
        scene.idle(8);

        // 溜槽吸收钻石
        scene.world().modifyEntity(diamondItem, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        scene.idle(8);

        // 钻石从溜槽输出
        scene.world().createItemEntity(targetItemPos, Vec3.ZERO, diamond);
        scene.idle(20);

        // 铁锭（应该被阻挡）
        scene.world().createItemEntity(
            itemDropPos,
            Vec3.ZERO,
            iron
        );
        scene.idle(20);

        scene.overlay().showText(40)
            .text("The chute will only let the set item pass through and will not absorb items outside of the filter condition") // 溜槽只会让设定的物品通过，不会吸收过滤条件以外的物品
            .pointAt(util.vector().centerOf(chutePos))
            .attachKeyFrame()
            .placeNearTarget();
        scene.idle(50);

        scene.markAsFinished();
    }
}
