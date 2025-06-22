package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeData;
import dev.dubhe.anvilcraft.recipe.neo.util.IItemStackPredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemCache;
import lombok.Getter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class HasItemBase<T extends HasItemBase<T, P>, P extends IItemStackPredicate> implements IRecipePredicate<T> {
    protected static final InWorldRecipeData<ItemCache> ITEM_CACHE = InWorldRecipeData.of(AnvilCraft.of("item_cache"));
    private final Vec3 offset;
    private final Vec3 range;
    protected final P item;

    public HasItemBase(Vec3 offset, Vec3 range, P item) {
        this.offset = offset;
        this.range = range;
        this.item = item;
    }

    @Override
    public boolean test(@NotNull InWorldRecipeContext context) {
        return this.item.testCount(this.getItem(context).getCount());
    }

    public ItemCache.ICacheInput getItem(@NotNull InWorldRecipeContext context) {
        final InWorldRecipeData<ItemCache.ICacheInput> cacheInput = InWorldRecipeData.of(
            AnvilCraft.of("item_cache_input$%s".formatted(this.hashCode()))
        );
        ItemCache.ICacheInput input = context.get(cacheInput);
        if (input == null) {
            Level level = context.getLevel();
            ItemCache itemCache = context.get(ITEM_CACHE);
            if (itemCache == null) {
                itemCache = new ItemCache(level);
                context.put(ITEM_CACHE, itemCache);
            }
            input = itemCache.getInput(this.item.testIgnoreCount(), this.offset, this.range);
            context.put(cacheInput, input);
        }
        return input;
    }
}
