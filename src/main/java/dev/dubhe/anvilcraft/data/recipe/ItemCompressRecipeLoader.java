package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItemSubPredicates;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.recipe.anvil.util.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.ItemCompressRecipe;
import dev.dubhe.anvilcraft.recipe.transform.NumericTagValuePredicate;
import dev.dubhe.anvilcraft.recipe.util.ItemSavedEntityPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

        ItemIngredientPredicate creeperResinPredicate = ItemIngredientPredicate
            .of(ModBlocks.RESIN_BLOCK.asItem())
            .withSubPredicate(
                ModItemSubPredicates.SAVED_ENTITY.get(),
                ItemSavedEntityPredicate.of(EntityType.CREEPER)
            )
            .build();
        ItemCompressRecipe.Builder superCapacitorEmptyRecipeBuilder = ItemCompressRecipe.builder()
            .requires(ModItemTags.IRON_PLATES, 2)
            .requires(creeperResinPredicate)
            .result(ModItems.SUPER_CAPACITOR_EMPTY);
        ItemCompressRecipe superCapacitorEmptyRecipe = superCapacitorEmptyRecipeBuilder.buildRecipe();
        superCapacitorEmptyRecipeBuilder.save(provider);

        ItemIngredientPredicate chargedCreeperResinPredicate = ItemIngredientPredicate
            .of(ModBlocks.RESIN_BLOCK.asItem())
            .withSubPredicate(
                ModItemSubPredicates.SAVED_ENTITY.get(),
                ItemSavedEntityPredicate.of(EntityType.CREEPER)
                    .predicate(b ->
                        b.compare(NumericTagValuePredicate.ValueFunction.GREATER_OR_EQUAL)
                            .lhs("powered")
                            .rhs(1)
                    )
            )
            .build();
        ItemCompressRecipe.builder()
            .requires(ModItemTags.IRON_PLATES, 2)
            .requires(chargedCreeperResinPredicate)
            .result(ModItems.SUPER_CAPACITOR)
            //这里还需要加上.priority(superCapacitorEmptyRecipe.getPriority() + 1)
            .save(provider);


        ItemCompressRecipe.builder()
            .requires(ModItems.NEUTRONIUM_INGOT)
            .requires(ModItems.URANIUM_INGOT)
            .result(ModItems.PLUTONIUM_NUGGET, 6)
            .result(ModItems.LIME_POWDER)
            .result(ModItems.NEUTRONIUM_INGOT)
            .save(provider, AnvilCraft.of("item_compress/plutonium_nugget_from_neutronium_ingot"));

        ItemCompressRecipe.builder()
            .requires(ModItems.STABLE_NEUTRONIUM_INGOT)
            .requires(ModItems.URANIUM_INGOT)
            .result(ModItems.PLUTONIUM_NUGGET, 6)
            .result(ModItems.LIME_POWDER)
            .result(ModItems.STABLE_NEUTRONIUM_INGOT)
            .save(provider, AnvilCraft.of("item_compress/plutonium_nugget_from_stable_neutronium_ingot"));
    }
}
