package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeData;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemCache;
import lombok.Getter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

@Getter
public abstract class HasItemBase<T extends HasItemBase<T, P>, P extends Predicate<ItemStack>> implements IRecipePredicate<T> {
    private static final InWorldRecipeData<ItemCache> ITEM_CACHE = InWorldRecipeData.of(AnvilCraft.of("item_cache"));
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
        return this.getItem(context) != null;
    }

    public ItemCache.ItemCacheElement getItem(@NotNull InWorldRecipeContext context) {
        ItemCache cache = this.getOrCreateItemCache(context);
        return cache.get(this.item);
    }

    public ItemCache getOrCreateItemCache(@NotNull InWorldRecipeContext context) {
        Level level = context.getLevel();
        Vec3 trans = this.getRange().scale(0.5d);
        Vec3 end = context.getPos().add(this.getOffset()).add(trans);
        Vec3 start = context.getPos().add(this.getOffset()).subtract(trans);
        ItemCache itemCache = context.get(ITEM_CACHE);
        if (itemCache != null && itemCache.isInRange(start, end)) return itemCache;
        if (itemCache == null) {
            itemCache = new ItemCache(level, start, end);
            context.put(ITEM_CACHE, itemCache);
        }
        List<ItemEntity> entities = level.getEntities(EntityType.ITEM, new AABB(start, end), entity -> true);
        itemCache.setRange(start, end);
        for (ItemEntity entity : entities) {
            ItemCache.ItemCacheElement element = itemCache.get(entity);
            element.add(entity);
        }
        return itemCache;
    }
}
