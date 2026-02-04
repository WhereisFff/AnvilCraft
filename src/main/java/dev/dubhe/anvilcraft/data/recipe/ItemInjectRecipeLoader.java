package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.anvilcraft.lib.recipe.component.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItemSubPredicates;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.item.property.predicate.ItemEnchantmentCountPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.ItemInjectRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class ItemInjectRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        ItemInjectRecipe.builder()
            .requires(Items.GOLD_INGOT, 2)
            .inputBlock(Blocks.NETHERRACK)
            .resultBlock(Blocks.NETHER_GOLD_ORE)
            .save(provider);

        ItemInjectRecipe.builder()
            .requires(Items.GOLD_INGOT)
            .inputBlock(Blocks.BLACKSTONE)
            .resultBlock(Blocks.GILDED_BLACKSTONE)
            .save(provider);

        ItemInjectRecipe.builder()
            .requires(Items.SHULKER_BOX)
            .inputBlock(Blocks.SHULKER_BOX)
            .resultBlock(ModBlocks.NESTING_SHULKER_BOX)
            .save(provider);

        ItemInjectRecipe.builder()
            .requires(ModBlocks.NESTING_SHULKER_BOX)
            .inputBlock(Blocks.SHULKER_BOX)
            .resultBlock(ModBlocks.OVER_NESTING_SHULKER_BOX)
            .save(provider);

        ItemInjectRecipe.builder()
            .requires(ModBlocks.OVER_NESTING_SHULKER_BOX)
            .inputBlock(Blocks.SHULKER_BOX)
            .resultBlock(ModBlocks.SUPERCRITICAL_NESTING_SHULKER_BOX)
            .save(provider);
    }
}
