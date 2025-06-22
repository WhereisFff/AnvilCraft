package dev.dubhe.anvilcraft.data.recipe;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.recipe.transform.MobTransformWithItemRecipe;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

public class MobTransformWithItemRecipeLoader {
    public static void init(RegistrateRecipeProvider provider) {
        MobTransformWithItemRecipe.from(EntityType.ZOMBIE, Items.ANVIL, EntityType.GIANT, ModBlocks.GIANT_ANVIL.asStack())
            .setItemChancePercentagePerItem(5)
            .save(provider);
    }
}
