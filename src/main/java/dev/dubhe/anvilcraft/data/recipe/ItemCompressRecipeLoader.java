package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.recipe.anvil.ItemCompressRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemCompressRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        ItemCompressRecipe.builder()
            .requires(Items.BONE, 3)
            .result(new ItemStack(Items.BONE_BLOCK))
            .save(provider);

        ItemCompressRecipe.builder()
            .requires(ModItems.CREAM, 4)
            .requires(Items.SUGAR)
            .result(new ItemStack(ModBlocks.CREAM_BLOCK))
            .save(provider);

        ItemCompressRecipe.builder()
            .requires(ModItems.CREAM, 4)
            .requires(Items.SUGAR)
            .requires(Items.SWEET_BERRIES)
            .result(new ItemStack(ModBlocks.BERRY_CREAM_BLOCK))
            .save(provider);

        ItemCompressRecipe.builder()
            .requires(ModItems.CREAM, 4)
            .requires(Items.SUGAR)
            .requires(ModItems.CHOCOLATE)
            .result(new ItemStack(ModBlocks.CHOCOLATE_CREAM_BLOCK))
            .save(provider);

        ItemCompressRecipe.builder()
            .requires(
                Ingredient.of(
                    BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof SmithingTemplateItem)
                        .map(Item::getDefaultInstance)
                ), 2)
            .result(ModItems.TWO_TO_ONE_SMITHING_TEMPLATE.asStack())
            .save(provider);

        ItemCompressRecipe.builder()
            .requires(
                Ingredient.of(
                    BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof SmithingTemplateItem)
                        .map(Item::getDefaultInstance)
                ), 4)
            .result(ModItems.FOUR_TO_ONE_SMITHING_TEMPLATE.asStack())
            .save(provider);

        ItemCompressRecipe.builder()
            .requires(
                Ingredient.of(
                    BuiltInRegistries.ITEM.stream()
                        .filter(item -> item instanceof SmithingTemplateItem)
                        .map(Item::getDefaultInstance)
                ), 8)
            .result(ModItems.EIGHT_TO_ONE_SMITHING_TEMPLATE.asStack())
            .save(provider);
    }
}
