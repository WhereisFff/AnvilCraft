package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.recipe.transform.MobTransformWithItemRecipe;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFItems;

public class MobTransformWithItemRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        MobTransformWithItemRecipe.from(EntityType.ZOMBIE, Items.ANVIL, EntityType.GIANT, ModBlocks.GIANT_ANVIL.asStack())
            .setItemChancePercentagePerItem(5)
            .save(provider);

        MobTransformWithItemRecipe.from(EntityType.ZOMBIE, Items.STONE_PICKAXE, EntityType.GIANT, TFItems.GIANT_PICKAXE.toStack())
            .setItemChancePercentagePerItem(50)
            .save(provider.withConditions(new ModLoadedCondition(TwilightForestMod.ID)),
                AnvilCraft.of("mob_transform_with_item/zombie_to_giant_pickaxe"));

        MobTransformWithItemRecipe.from(EntityType.ZOMBIE, Items.STONE_SWORD, EntityType.GIANT, TFItems.GIANT_SWORD.toStack())
            .setItemChancePercentagePerItem(50)
            .save(provider.withConditions(new ModLoadedCondition(TwilightForestMod.ID)),
                AnvilCraft.of("mob_transform_with_item/zombie_to_giant_sword"));
    }
}
