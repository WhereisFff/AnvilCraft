package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.item.DiskItem;
import dev.dubhe.anvilcraft.mixin.accessor.ShapedRecipePatternAccessor;
import dev.dubhe.anvilcraft.recipe.JewelCraftingRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DiscFragmentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.OminousBottleItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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

        for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.holders().toList()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (itemHolder.value() instanceof BannerItem) {
                JewelCraftingRecipe.builder()
                    .requires(Items.PAPER)
                    .requires(Items.INK_SAC)
                    .result(new ItemStack(itemHolder))
                    .save(provider);
            } else if (itemHolder.is(Tags.Items.MUSIC_DISCS)) {
                JewelCraftingRecipe.builder()
                    .requires(ModItems.HARDEND_RESIN, 4)
                    .requires(Items.PAPER)
                    .result(new ItemStack(itemHolder))
                    .save(provider);
            } else if (
                DecoratedPotPatterns.getPatternFromItem(itemHolder.value()) != null
                && !itemHolder.value().equals(Items.BRICK)
            ) {
                JewelCraftingRecipe.builder()
                    .requires(Items.BRICK, 2)
                    .result(new ItemStack(itemHolder))
                    .save(provider);
            } else if (itemHolder.is(ItemTags.TRIM_TEMPLATES) && server != null) {
                JewelCraftingRecipe.Builder builder = JewelCraftingRecipe.builder()
                    .requires(Items.DIAMOND)
                    .result(new ItemStack(itemHolder));

                for (RecipeHolder<?> recipeHolder : server.getRecipeManager().getRecipes()) {
                    Recipe<?> recipe = recipeHolder.value();
                    if (!(recipe instanceof ShapedRecipe shaped)) return;
                    if (!shaped.getResultItem(server.registryAccess()).is(itemHolder)) return;
                    ShapedRecipePattern pattern = shaped.pattern;
                    if (pattern.width() != 3 || pattern.height() != 3) return;
                    ShapedRecipePattern.Data data = ((ShapedRecipePatternAccessor)(Object) pattern).accessor$data().orElse(null);
                    if (data == null) return;
                    builder.requires(data.key().get(data.pattern().get(1).charAt(1))).save(provider);
                    break;
                }
            }
        }
    }
}
