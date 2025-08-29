package dev.dubhe.anvilcraft.integration.ponder.scene.recipe;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderTags;
import dev.dubhe.anvilcraft.integration.ponder.api.AnvilCraftSceneBuilder;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class SpaceOvercompressorScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> helper = registrationHelper.withKeyFunction(RegistryEntry::getId);
        helper.forComponents(
                ModBlocks.SPACE_OVERCOMPRESSOR
            )
            .addStoryBoard(
                "platform/555",
                SpaceOvercompressorScene::crafting,
                AnvilCraftPonderTags.PROCESSING_COMPONENTS
            );
    }

    private static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        AnvilCraftSceneBuilder builder = new AnvilCraftSceneBuilder(scene);
        builder.title("space_overcompressor", "Use the Space Overcompressor to create the Neutronium Ingot");
        builder.configureBasePlate(0, 0, 5);
        builder.showBasePlate();
        // 创建空间超压器
        BlockPos spaceOvercompressorPos = new BlockPos(2, 2, 2);
        builder.world().setBlock(spaceOvercompressorPos, ModBlocks.SPACE_OVERCOMPRESSOR.getDefaultState(), false);
        builder.world().showIndependentSection(util.select().position(spaceOvercompressorPos), Direction.NORTH);
        // 创建铁砧
        BlockPos anvilPos = new BlockPos(2, 4, 2);
        builder.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilLink =
            builder.world().showIndependentSection(util.select().position(anvilPos), Direction.NORTH);
        builder.idle(10);
        // 在 y=0 层，从 (1,0,1) 到 (3,0,3) 的 3x3 区域内放置飘浮粉块
        builder.world().setBlocks(util.select().fromTo(1, 0, 1, 3, 0, 3), ModBlocks.END_DUST.getDefaultState(), true);
        builder.idle(10);

        Vec3 ironBlockPos = new Vec3(2.5, 3.3, 2.5);
        ItemStack ironBlockItem = new ItemStack(ModBlocks.HEAVY_IRON_BLOCK, 64);
        // 循环3次，每次砸入物品
        builder.overlay().showText(40)
            .text("Press a large amount of metal items into the Space Overcompressor to accumulate mass.")
            .pointAt(util.vector().blockSurface(util.grid().at(2, 3, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        for (int i = 0; i < 3; i++) {
            // 添加铁块
            ElementLink<EntityElement> ironBockItemLink = builder.world().createItemEntity(ironBlockPos, Vec3.ZERO, ironBlockItem);
            builder.idle(5);
            // 铁砧压入
            builder.world().dropSection(anvilLink);
            builder.world().modifyEntity(ironBockItemLink, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
            builder.idle(6);
            // 铁砧上移
            builder.world().liftSection(anvilLink);
        }
        // 从空间超压器下方掉出中子锭
        builder.overlay().showText(100)
            .text(
                "When the Space Overcompressor has built up enough mass, a neutron ingot will form. "
                + "It can pass through most blocks, so you'll need something like end dust to stop it."
            )
            .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.WEST))
            .attachKeyFrame()
            .placeNearTarget();
        builder.world().createItemEntity(spaceOvercompressorPos.getBottomCenter(), Vec3.ZERO, ModItems.NEUTRONIUM_INGOT.asStack());

        builder.idle(20); // 等待一会展示结果

        builder.markAsFinished();
    }
}
