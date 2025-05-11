package dev.dubhe.anvilcraft.recipe.neo.util;

import lombok.Getter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Predicate;

public class ItemCache extends HashSet<ItemCache.ItemCacheElement> {
    @Getter
    private final Level level;
    private Vec3 min;
    private Vec3 max;

    public ItemCache(Level level, @NotNull Vec3 start, @NotNull Vec3 end) {
        this.level = level;
        this.setRange(start, end);
    }

    public void setRange(@NotNull Vec3 start, @NotNull Vec3 end) {
        this.min = new Vec3(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z));
        this.max = new Vec3(Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z));
    }

    public boolean isInRange(@NotNull Vec3 start, @NotNull Vec3 end) {
        Vec3 min = new Vec3(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z));
        Vec3 max = new Vec3(Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z));
        return min.x >= this.min.x && min.y >= this.min.y && min.z >= this.min.z && max.x <= this.max.x && max.y <= this.max.y && max.z <= this.max.z;
    }

    public @NotNull ItemCacheElement get(ItemEntity entity) {
        for (ItemCacheElement element : this) {
            if (element.test(entity)) {
                return element;
            }
        }
        ItemCacheElement element = new ItemCacheElement(this, entity);
        this.add(element);
        return element;
    }

    public ItemCacheElement get(ItemStack entity) {
        for (ItemCacheElement element : this) {
            if (element.test(entity)) {
                return element;
            }
        }
        return null;
    }

    public ItemCacheElement get(Predicate<ItemStack> entity) {
        for (ItemCacheElement element : this) {
            if (element.test(entity)) {
                return element;
            }
        }
        return null;
    }

    @Getter
    public static class ItemCacheElement {
        private final Vec3 pos;
        private final ItemCache cache;
        private final ItemStack simulate;
        private final Map<ItemEntity, ItemStack> entities;

        public ItemCacheElement(ItemCache cache, @NotNull ItemEntity entity, ItemEntity @NotNull ... entities) {
            this.pos = entity.position();
            this.cache = cache;
            this.simulate = entity.getItem().copy();
            this.entities = new HashMap<>();
            this.entities.put(entity, entity.getItem());
            this.add(entities);
        }

        public boolean test(@NotNull ItemEntity entity) {
            return this.test(entity.getItem());
        }

        public boolean test(@NotNull ItemStack stack) {
            return stack == this.simulate || ItemStack.isSameItemSameComponents(this.simulate, stack);
        }

        public boolean test(@NotNull Predicate<ItemStack> predicate) {
            return predicate.test(this.simulate);
        }

        public void grow(int count) {
            this.simulate.grow(count);
            int balance = count;
            for (Map.Entry<ItemEntity, ItemStack> entry : this.entities.entrySet()) {
                if (balance == 0) break;
                ItemEntity entity = entry.getKey();
                ItemStack stack = entry.getValue();
                int maxStackSize = stack.getMaxStackSize();
                int stackCount = stack.getCount();
                int newCount = Math.min(maxStackSize, Math.max(stackCount + balance, 0));
                int difference = newCount - stackCount;
                balance -= difference;
                stack.grow(difference);
                if (newCount <= 0) {
                    entity.discard();
                } else {
                    entity.setItem(stack);
                }
            }
            if (balance < 0) throw new IllegalStateException();
            if (balance > 0) {
                int maxStackSize = this.simulate.getMaxStackSize();
                while (balance > 0) {
                    ItemStack stack = this.simulate.copy();
                    int newCount = Math.min(maxStackSize, balance);
                    stack.setCount(newCount);
                    balance -= newCount;
                    ItemEntity entity = new ItemEntity(this.cache.level, this.pos.x, this.pos.y, this.pos.z, stack);
                    this.cache.level.addFreshEntity(entity);
                }
            }
        }

        public void shrink(int count) {
            this.grow(-count);
        }

        public void add(ItemEntity @NotNull ... entities) {
            for (ItemEntity entity : entities) {
                if (this.entities.containsKey(entity)) continue;
                ItemStack item = entity.getItem();
                if (!ItemStack.isSameItemSameComponents(this.simulate, item)) continue;
                this.simulate.grow(item.getCount());
                this.entities.put(entity, item);
            }
        }
    }
}
