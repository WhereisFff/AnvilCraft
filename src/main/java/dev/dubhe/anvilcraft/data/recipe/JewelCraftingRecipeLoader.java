package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.mixin.accessor.ShapedRecipePatternAccessor;
import dev.dubhe.anvilcraft.recipe.JewelCraftingRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.neoforged.neoforge.common.Tags;

public class JewelCraftingRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        JewelCraftingRecipe.builder()
            .requires(Items.EXPERIENCE_BOTTLE, 16)
            .requires(Items.GOLD_BLOCK, 8)
            .requires(Items.GOLDEN_APPLE)
            .result(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE))
            .save(provider);

        JewelCraftingRecipe.builder()
            .requires(Tags.Items.STORAGE_BLOCKS_GOLD)
            .requires(Items.EMERALD, 2)
            .requires(ModItems.ROYAL_STEEL_INGOT)
            .result(new ItemStack(Items.TOTEM_OF_UNDYING))
            .save(provider);

        JewelCraftingRecipe.builder()
            .requires(Items.PHANTOM_MEMBRANE, 8)
            .requires(Tags.Items.FEATHERS, 8)
            .requires(Tags.Items.LEATHERS, 2)
            .requires(Items.BAMBOO, 16)
            .result(new ItemStack(Items.ELYTRA))
            .save(provider);

        JewelCraftingRecipe.builder()
            .requires(Items.POLISHED_TUFF)
            .requires(Items.COPPER_INGOT)
            .result(new ItemStack(Items.TRIAL_KEY))
            .save(provider);

        JewelCraftingRecipe.builder()
            .requires(Items.POLISHED_TUFF, 3)
            .requires(Items.OXIDIZED_COPPER)
            .requires(Items.OMINOUS_BOTTLE)
            .result(new ItemStack(Items.OMINOUS_TRIAL_KEY))
            .save(provider);

        JewelCraftingRecipe.builder()
            .requires(Items.EXPERIENCE_BOTTLE, 16)
            .requires(ModItems.CURSED_GOLD_INGOT, 2)
            .requires(Items.GLASS_BOTTLE)
            .result(Items.OMINOUS_BOTTLE.getDefaultInstance())
            .save(provider);

        JewelCraftingRecipe.builder()
            .requires(ModBlocks.HEAVY_IRON_BLOCK, 64)
            .requires(ModBlocks.LEAD_BLOCK, 64)
            .requires(ModBlocks.SPACE_OVERCOMPRESSOR)
            .result(new ItemStack(Items.HEAVY_CORE))
            .save(provider);

        for (Holder<Item> holder : BuiltInRegistries.ITEM.holders().toList()) {
            if (holder.value() instanceof BannerItem) {
                JewelCraftingRecipe.builder()
                    .requires(Items.PAPER)
                    .requires(Items.INK_SAC)
                    .result(new ItemStack(holder))
                    .save(provider);
            } else if (holder.value().getDefaultInstance().has(DataComponents.JUKEBOX_PLAYABLE)) {
                JewelCraftingRecipe.builder()
                    .requires(ModItems.HARDEND_RESIN, 4)
                    .requires(Items.PAPER)
                    .result(new ItemStack(holder))
                    .save(provider);
            } else if (
                DecoratedPotPatterns.getPatternFromItem(holder.value()) != null
                && !holder.value().equals(Items.BRICK)
            ) {
                JewelCraftingRecipe.builder()
                    .requires(Items.BRICK, 2)
                    .result(new ItemStack(holder))
                    .save(provider);
            } else if (TrimPatterns.getFromTemplate(provider.getProvider(), holder.value().getDefaultInstance()).isPresent()) {
                JewelCraftingRecipe.builder()
                    .requires(ModItems.EARTH_CORE_SHARD)
                    .requires(Items.DIAMOND)
                    .result(new ItemStack(holder))
                    .save(provider);
            }
        }
    }
}
