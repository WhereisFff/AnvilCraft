package dev.dubhe.anvilcraft.integration.ponder.scene.recipe;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class ItemCompressScene {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> registrationHelper) {
        PonderSceneRegistrationHelper<Item> helper = registrationHelper.withKeyFunction(BuiltInRegistries.ITEM::getKey);
        helper.forComponents(Items.CAULDRON)
            .addStoryBoard(
                "platform/555",
                ItemCompressScene::crafting,
                AnvilCraftPonderTags.PROCESSING_COMPONENTS
            );
    }

    private static void crafting(SceneBuilder scene, SceneBuildingUtil util) {
        AnvilCraftSceneBuilder builder = new AnvilCraftSceneBuilder(scene);
        builder.title("item_compress", "Compressing Items");
        builder.configureBasePlate(0, 0, 5);
        builder.showBasePlate();

        BlockPos anvilPos = util.grid().at(2, 3, 2);
        builder.world().setBlock(anvilPos, Blocks.ANVIL.defaultBlockState(), false);
        ElementLink<WorldSectionElement> anvilLink =
            builder.world().showIndependentSection(util.select().position(anvilPos), Direction.DOWN);

        BlockPos cauldronPos = util.grid().at(2, 1, 2);
        builder.world().setBlock(cauldronPos, Blocks.CAULDRON.defaultBlockState(), false);
        builder.world().showSection(util.select().position(cauldronPos), Direction.NORTH);
        builder.idle(20);

        // 放入物品
        ElementLink<EntityElement> itemLink =
            builder.world().createItemEntity(cauldronPos.above().getCenter(), Vec3.ZERO, new ItemStack(Items.IRON_INGOT, 9));
        builder.idle(10);

        // 给我砸！
        builder.world().dropSection(anvilLink);
        builder.world().modifyEntity(itemLink, entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        builder.world().createItemEntity(cauldronPos.getCenter(), Vec3.ZERO, Items.IRON_BLOCK.getDefaultInstance());
        builder.world().liftSection(anvilLink);
        builder.idle(10);

        builder.overlay().showText(40)
            .text("Using cauldrons to compress items.")
            .pointAt(cauldronPos.getCenter())
            .attachKeyFrame()
            .placeNearTarget();

        builder.markAsFinished();
    }
}
