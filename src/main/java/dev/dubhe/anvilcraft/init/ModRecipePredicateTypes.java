package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.neo.predicate.block.HasBlockIngredient;
import dev.dubhe.anvilcraft.recipe.neo.predicate.item.HasItem;
import dev.dubhe.anvilcraft.recipe.neo.predicate.item.HasItemIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipePredicateTypes {
    public static final DeferredRegister<IRecipePredicate.Type<?>> PREDICATE_TYPE = DeferredRegister
        .create(ModRegistries.PREDICATE_TYPE_REGISTRY, AnvilCraft.MOD_ID);

    public static final DeferredHolder<IRecipePredicate.Type<?>, HasItem.Type> HAS_ITEM = PREDICATE_TYPE.register(
        "has_item",
        HasItem.Type::new
    );

    public static final DeferredHolder<IRecipePredicate.Type<?>, HasItemIngredient.Type> HAS_ITEM_INGREDIENT = PREDICATE_TYPE.register(
        "has_item_ingredient",
        HasItemIngredient.Type::new
    );

    public static final DeferredHolder<IRecipePredicate.Type<?>, HasBlock.Type> HAS_BLOCK = PREDICATE_TYPE.register(
        "has_block",
        HasBlock.Type::new
    );

    public static final DeferredHolder<IRecipePredicate.Type<?>, HasBlockIngredient.Type> HAS_BLOCK_INGREDIENT = PREDICATE_TYPE.register(
        "has_block_ingredient",
        HasBlockIngredient.Type::new
    );
}
