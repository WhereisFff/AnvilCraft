package dev.dubhe.anvilcraft.recipe.anvil.cache;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModBlockEntityTags;
import dev.dubhe.anvilcraft.init.ModEntityTags;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipeData;
import dev.dubhe.anvilcraft.recipe.anvil.util.Range;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 物品缓存
 * 缓存物品，用于物品匹配/输出
 */
public class ItemCache {
    public static final InWorldRecipeData<ItemCache> ITEM_CACHE = InWorldRecipeData.of(AnvilCraft.of("item_cache"), ItemCache::of);
    public static final Consumer<InWorldRecipeContext> DEFAULT_ACCEPTOR = (ctx) -> ctx.get(ItemCache.ITEM_CACHE).endCache();
    @Getter
    private final Level level;
    private final Set<ICacheElement> inputs = new HashSet<>();
    private final Set<ICacheElement> outputs = new HashSet<>();
    private final Range range = new Range(Vec3.ZERO, Vec3.ZERO);
    private final List<SpawnOperation> spawnList = new ArrayList<>();
    private final Set<ICacheInputOutputImpl> inputCache = new HashSet<>();
    private final Set<ICacheInputOutputImpl> outputCache = new HashSet<>();

    public ItemCache(Level level) {
        this.level = level;
    }

    private static @NotNull ItemCache of(@NotNull InWorldRecipeContext level, InWorldRecipeData<ItemCache> key) {
        return new ItemCache(level.getLevel());
    }

    public boolean inRange(@NotNull Vec3 pos, @NotNull Vec3 range) {
        return this.range.contains(pos, range);
    }


    private static void toElement(
        ItemCache itemCache,
        @NotNull IItemHandlerCache cache,
        Set<ICacheElement> input,
        Set<ICacheElement> output,
        Vec3 elementPos,
        Vec3 elementRange
    ) {
        IItemHandler inputHandler = cache.getInput();
        for (int i = 0; i < inputHandler.getSlots(); i++) {
            ItemHandlerCacheElement element = new ItemHandlerCacheElement(
                itemCache,
                inputHandler,
                i,
                elementPos,
                elementRange
            );
            input.add(element);
        }
        IItemHandler outputHandler = cache.getOutput();
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemHandlerCacheElement element = new ItemHandlerCacheElement(
                itemCache,
                outputHandler,
                i,
                elementPos,
                elementRange
            );
            output.add(element);
        }
    }


    private static void toElement(
        ItemCache itemCache,
        @NotNull IItemHandler handler,
        Set<ICacheElement> input,
        Set<ICacheElement> output,
        Vec3 elementPos,
        Vec3 elementRange
    ) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemHandlerCacheElement element = new ItemHandlerCacheElement(
                itemCache,
                handler,
                i,
                elementPos,
                elementRange
            );
            input.add(element);
            output.add(element);
        }
    }

    private static @NotNull Map.Entry<Set<ICacheElement>, Set<ICacheElement>> toElement(
        @NotNull ItemCache itemCache,
        @NotNull Entity entity
    ) {
        Set<ICacheElement> input = new HashSet<>();
        Set<ICacheElement> output = new HashSet<>();
        if (entity instanceof ItemEntity itemEntity) {
            ItemEntityCacheElement element = new ItemEntityCacheElement(itemCache, itemEntity);
            input.add(element);
            output.add(element);
            return Map.entry(input, output);
        }
        double xRange = Math.abs(entity.getBoundingBox().maxX - entity.getBoundingBox().minX);
        double yRange = Math.abs(entity.getBoundingBox().maxY - entity.getBoundingBox().minY);
        double zRange = Math.abs(entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);
        double minRange = Math.min(xRange, Math.min(yRange, zRange));
        Vec3 elementPos = entity.position().add(0.0, yRange / 2.0, 0.0);
        Vec3 elementRange = new Vec3(minRange, minRange, minRange);
        if (entity instanceof IItemHandlerCache cache) {
            ItemCache.toElement(itemCache, cache, input, output, elementPos, elementRange);
        } else if (entity instanceof IItemHandler handler && entity.getType().is(ModEntityTags.ITEM_CACHE)) {
            ItemCache.toElement(itemCache, handler, input, output, elementPos, elementRange);
        }
        return Map.entry(input, output);
    }

    private static @NotNull Map.Entry<Set<ICacheElement>, Set<ICacheElement>> toElement(
        @NotNull ItemCache itemCache,
        @NotNull BlockEntity entity
    ) {
        Set<ICacheElement> input = new HashSet<>();
        Set<ICacheElement> output = new HashSet<>();
        Vec3 elementPos = entity.getBlockPos().getCenter();
        Vec3 elementRange = new Vec3(0.5, 0.5, 0.5);
        Predicate<BlockEntity> inTag = blockEntity -> false;
        Optional<HolderSet.Named<BlockEntityType<?>>> holders = BuiltInRegistries.BLOCK_ENTITY_TYPE.getTag(ModBlockEntityTags.ITEM_CACHE);
        if (holders.isPresent()) {
            HolderSet.Named<BlockEntityType<?>> named = holders.get();
            List<ResourceKey<BlockEntityType<?>>> keys = new ArrayList<>();
            for (Holder<BlockEntityType<?>> holder : named) {
                ResourceKey<BlockEntityType<?>> key = holder.getKey();
                keys.add(key);
            }
            inTag = blockEntity -> {
                Optional<ResourceKey<BlockEntityType<?>>> key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(blockEntity.getType());
                return key.filter(keys::contains).isPresent();
            };
        }
        if (entity instanceof IItemHandlerCache cache) {
            ItemCache.toElement(itemCache, cache, input, output, elementPos, elementRange);
        } else if (entity instanceof IItemHandler handler && inTag.test(entity)) {
            ItemCache.toElement(itemCache, handler, input, output, elementPos, elementRange);
        }
        return Map.entry(input, output);
    }

    public void grow(Vec3 pos, Vec3 range) {
        Range newRange = Range.of(pos, range);
        if (this.range.contains(pos, range)) return;
        this.range.grow(newRange);
        List<Entity> entities = this.level.getEntities(EntityTypeTest.forClass(Entity.class), newRange.toAABB(), (entity) -> true);
        for (Entity entity : entities) {
            Map.Entry<Set<ICacheElement>, Set<ICacheElement>> entry = ItemCache.toElement(this, entity);
            this.inputs.addAll(entry.getKey());
            this.outputs.addAll(entry.getValue());
        }
        for (BlockPos blockPos : newRange) {
            BlockEntity entity = level.getBlockEntity(blockPos);
            if (entity == null) continue;
            Map.Entry<Set<ICacheElement>, Set<ICacheElement>> entry = ItemCache.toElement(this, entity);
            this.inputs.addAll(entry.getKey());
            this.outputs.addAll(entry.getValue());
        }
    }

    public ICacheInput getInput(ItemLike itemLike, Vec3 pos) {
        return this.getInput(stack1 -> stack1.is(itemLike.asItem()), pos);
    }

    public ICacheInput getInput(ItemLike itemLike, Vec3 pos, Vec3 range) {
        return this.getInput(stack1 -> stack1.is(itemLike.asItem()), pos, range);
    }

    public ICacheInput getInput(ItemStack stack, Vec3 pos) {
        return this.getInput(stack1 -> ItemStack.isSameItemSameComponents(stack, stack1), pos);
    }

    public ICacheInput getInput(ItemStack stack, Vec3 pos, Vec3 range) {
        return this.getInput(stack1 -> ItemStack.isSameItemSameComponents(stack, stack1), pos, range);
    }

    public ICacheInput getInput(Predicate<ItemStack> predicate, Vec3 pos) {
        return this.getInput(predicate, pos, new Vec3(0.25, 0.25, 0.25));
    }

    public ICacheInput getInput(Predicate<ItemStack> predicate, Vec3 pos, Vec3 range) {
        Range range1 = Range.of(pos, range);
        for (ICacheInputOutputImpl element : this.inputCache) {
            if (!element.equals(predicate, range1)) continue;
            return element;
        }
        this.grow(pos, range);
        Set<ICacheElement> inputs = new HashSet<>();
        for (ICacheElement input : this.inputs) {
            Range inputRange = Range.of(input.getPos(), input.getRange());
            if (!inputRange.cross(Range.of(pos, range))) continue;
            if (!input.is(predicate)) continue;
            inputs.add(input);
        }
        ICacheInputOutputImpl input = new ICacheInputOutputImpl(predicate, this, pos, range1, inputs);
        this.inputCache.add(input);
        return input;
    }

    public ICacheOutput getOutput(ItemStack stack, Vec3 pos) {
        return this.getOutput(stack, pos, new Vec3(0.05, 0.05, 0.05));
    }

    public ICacheOutput getOutput(ItemStack stack, Vec3 pos, Vec3 range) {
        Range range1 = Range.of(pos, range);
        for (ICacheInputOutputImpl element : this.outputCache) {
            if (!element.equals(stack, range1)) continue;
            return element;
        }
        this.grow(pos, range);
        Set<ICacheElement> outputs = new HashSet<>();
        for (ICacheElement output : this.outputs) {
            Range outputRange = Range.of(output.getPos(), output.getRange());
            if (!outputRange.contains(range1)) continue;
            if (!output.is(stack)) continue;
            outputs.add(output);
        }
        if (outputs.isEmpty()) {
            ItemEntityCacheElement output = ItemEntityCacheElement.create(this, stack, pos);
            this.outputs.add(output);
            outputs.add(output);
        }
        ICacheInputOutputImpl output = new ICacheInputOutputImpl(stack, this, pos, range1, outputs);
        this.outputCache.add(output);
        return output;
    }

    public void pushSpawnList(Collection<SpawnOperation> spawnOperations) {
        this.spawnList.addAll(spawnOperations);
    }

    public void endCache() {
        for (ICacheElement input : this.inputs) {
            input.sync();
        }
        for (ICacheElement output : this.outputs) {
            output.sync();
        }
        Map<Map.Entry<ItemStack, Vec3>, Integer> map = new HashMap<>();
        for (SpawnOperation spawnOperation : this.spawnList) {
            ItemStack stack = spawnOperation.stack();
            if (stack.isEmpty()) continue;
            int count = spawnOperation.count();
            if (count <= 0) continue;
            stack.setCount(1);
            Vec3 spawnPos = spawnOperation.pos();
            Map.Entry<ItemStack, Vec3> key = Map.entry(stack, spawnPos);
            for (Map.Entry<ItemStack, Vec3> mapKey : map.keySet()) {
                ItemStack stack1 = mapKey.getKey();
                Vec3 pos = mapKey.getValue();
                if (!ItemStack.isSameItemSameComponents(stack, stack1)) continue;
                if (!pos.closerThan(spawnPos, 0.25)) continue;
                key = mapKey;
            }
            map.put(key, map.getOrDefault(key, 0) + count);
        }
        for (Map.Entry<ItemStack, Vec3> stackEntry : map.keySet()) {
            int count = map.get(stackEntry);
            ItemStack stack = stackEntry.getKey();
            Vec3 pos = stackEntry.getValue();
            int maxStackSize = stack.getMaxStackSize();
            while (count > 0) {
                ItemStack stack1 = stack.copy();
                int newCount = Math.min(maxStackSize, count);
                stack1.setCount(newCount);
                ItemEntity entity = new ItemEntity(this.level, pos.x, pos.y, pos.z, stack1);
                this.level.addFreshEntity(entity);
                count -= newCount;
            }
        }
    }

    public record CacheOperation(int amount) {
    }

    public interface ICacheInput {
        int shrink(int count);

        int rollbackShrink();

        void sync();

        int getCount();
    }

    public interface ICacheOutput {
        ItemStack grow(ItemStack stack, boolean spawn);

        ItemStack rollbackGrow();

        void sync();
    }

    public interface ICacheElement extends ICacheInput, ICacheOutput {
        int getCapacity(@NotNull ItemStack stack);

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean is(ItemStack stack);

        boolean is(Predicate<ItemStack> stack);

        Vec3 getPos();

        default Vec3 getRange() {
            return new Vec3(0.125, 0.125, 0.125);
        }
    }

    public abstract static class AbstractCacheElement implements ICacheElement {
        protected final ItemCache cache;
        protected final ItemStack type;
        protected ItemStack simulate;
        protected final Deque<CacheOperation> growSimulateStack = new ArrayDeque<>();
        protected final Deque<CacheOperation> shrinkSimulateStack = new ArrayDeque<>();

        protected AbstractCacheElement(ItemCache cache, @NotNull ItemStack simulate) {
            this.cache = cache;
            this.simulate = simulate;
            this.type = simulate.copy();
            this.type.setCount(1);
        }

        @Override
        public int shrink(int count) {
            int shrink = Math.min(this.simulate.getCount(), count);
            this.simulate.shrink(shrink);
            this.shrinkSimulateStack.add(new CacheOperation(shrink));
            return count - shrink;
        }

        @Override
        public ItemStack grow(@NotNull ItemStack stack, boolean spawn) {
            ItemStack copy = stack.copy();
            if (!this.is(stack)) return copy;
            int growCount = copy.getCount();
            int simulateCount = this.simulate.getCount();
            int grownSimulateCount = Math.min(this.getCapacity(stack), simulateCount + growCount);
            int grownCount = grownSimulateCount - simulateCount;
            int remainingCount = growCount - grownCount;
            if (remainingCount > 0) copy.setCount(remainingCount);
            else copy = ItemStack.EMPTY;
            if (!this.simulate.isEmpty()) {
                this.simulate.grow(grownCount);
            } else {
                this.simulate = this.type.copy();
                this.simulate.setCount(grownCount);
            }
            this.growSimulateStack.push(new CacheOperation(grownCount));
            return copy;
        }

        @Override
        public ItemStack rollbackGrow() {
            CacheOperation operation = this.growSimulateStack.pop();
            ItemStack copy = this.simulate.copy();
            this.simulate.shrink(operation.amount);
            copy.setCount(operation.amount);
            return copy;
        }

        @Override
        public int rollbackShrink() {
            CacheOperation operation = this.shrinkSimulateStack.pop();
            this.simulate.grow(operation.amount);
            return operation.amount;
        }

        public boolean is(@NotNull Predicate<ItemStack> stack) {
            return stack.test(this.type);
        }

        @Override
        public boolean is(ItemStack stack) {
            if (stack == null) return false;
            return ItemStack.isSameItemSameComponents(stack, this.type);
        }

        @Override
        public int getCount() {
            return this.simulate.getCount();
        }
    }

    @EqualsAndHashCode(callSuper = false)
    public static class ItemEntityCacheElement extends AbstractCacheElement implements ICacheElement {
        private final ItemEntity entity;
        private boolean isInLevel;
        @Getter
        private final Vec3 pos;

        public ItemEntityCacheElement(ItemCache cache, @NotNull ItemEntity entity) {
            super(cache, entity.getItem().copy());
            this.pos = entity.position().add(0.0, 0.125, 0.0);
            this.entity = entity;
            this.isInLevel = true;
        }

        public static @NotNull ItemEntityCacheElement create(@NotNull ItemCache cache, ItemStack stack, @NotNull Vec3 pos) {
            ItemEntity itemEntity = new ItemEntity(cache.level, pos.x, pos.y, pos.z, stack, 0.0d, 0.0d, 0.0d);
            ItemEntityCacheElement element = new ItemEntityCacheElement(cache, itemEntity);
            element.isInLevel = false;
            element.simulate.setCount(0);
            return element;
        }

        @Override
        public void sync() {
            this.growSimulateStack.clear();
            this.shrinkSimulateStack.clear();
            this.entity.setItem(this.simulate);
            if (this.isInLevel) return;
            this.cache.level.addFreshEntity(this.entity);
        }

        @Override
        public int getCapacity(@NotNull ItemStack stack) {
            return this.simulate.isEmpty() ? stack.getMaxStackSize() : this.simulate.getMaxStackSize();
        }
    }

    @EqualsAndHashCode(callSuper = false)
    public static class ItemHandlerCacheElement extends AbstractCacheElement implements ICacheElement {
        private final IItemHandler iItemHandler;
        private final int slot;
        @Getter
        private final Vec3 pos;
        @Getter
        private final Vec3 range;

        public ItemHandlerCacheElement(ItemCache cache, @NotNull IItemHandler iItemHandler, int slot, Vec3 pos, Vec3 range) {
            super(cache, iItemHandler.getStackInSlot(slot).copy());
            this.iItemHandler = iItemHandler;
            this.slot = slot;
            this.pos = pos;
            this.range = range;
        }

        @Override
        public int getCapacity(@NotNull ItemStack stack) {
            return this.iItemHandler.getSlotLimit(this.slot);
        }

        @Override
        public boolean is(ItemStack stack) {
            return this.iItemHandler.isItemValid(this.slot, stack);
        }

        @Override
        public void sync() {
            this.growSimulateStack.clear();
            this.shrinkSimulateStack.clear();
            ItemStack stack = this.iItemHandler.getStackInSlot(this.slot);
            if (stack.isEmpty()) {
                this.iItemHandler.insertItem(this.slot, this.simulate, false);
            } else {
                stack.setCount(this.simulate.getCount());
            }
        }
    }

    public record InputOutputOperation(Set<ICacheElement> elements) {
    }

    public record SpawnOperation(ItemStack stack, int count, Vec3 pos) {
    }

    public static class ICacheInputOutputImpl implements ICacheInput, ICacheOutput {
        private final ItemCache cache;
        private final Set<ICacheElement> elements = new HashSet<>();
        private final Deque<InputOutputOperation> growSimulateStack = new ArrayDeque<>();
        private final Deque<InputOutputOperation> shrinkSimulateStack = new ArrayDeque<>();
        private final Deque<SpawnOperation> spawnSimulateStack = new ArrayDeque<>();
        private final Vec3 pos;
        private final Object key;
        private final Range range;

        public ICacheInputOutputImpl(Object key, ItemCache cache, Vec3 pos, Range range, Collection<ICacheElement> elements) {
            this.key = key;
            this.cache = cache;
            this.pos = pos;
            this.range = range;
            this.elements.addAll(elements);
        }

        @Override
        public int shrink(int count) {
            Set<ICacheElement> elements = new HashSet<>();
            for (ICacheElement element : this.elements) {
                count = element.shrink(count);
                elements.add(element);
                if (count <= 0) break;
            }
            this.shrinkSimulateStack.push(new InputOutputOperation(elements));
            return count;
        }

        @Override
        public int rollbackShrink() {
            InputOutputOperation pop = this.shrinkSimulateStack.pop();
            int count = 0;
            for (ICacheElement element : pop.elements) {
                count += element.rollbackShrink();
            }
            return count;
        }

        @Override
        public ItemStack grow(ItemStack stack, boolean spawn) {
            Set<ICacheElement> elements = new HashSet<>();
            for (ICacheElement element : this.elements) {
                stack = element.grow(stack, false);
                elements.add(element);
                if (stack.isEmpty()) break;
            }
            this.growSimulateStack.push(new InputOutputOperation(elements));
            if (spawn) {
                this.spawnSimulateStack.push(new SpawnOperation(stack.copy(), stack.getCount(), this.pos));
                return ItemStack.EMPTY;
            } else {
                this.spawnSimulateStack.push(new SpawnOperation(ItemStack.EMPTY, 0, this.pos));
                return stack;
            }
        }

        @Override
        public ItemStack rollbackGrow() {
            InputOutputOperation operation = this.growSimulateStack.pop();
            ItemStack stack = ItemStack.EMPTY;
            for (ICacheElement element : operation.elements) {
                ItemStack stack1 = element.rollbackGrow();
                if (stack.isEmpty()) stack = stack1;
                else stack.grow(stack1.getCount());
            }
            SpawnOperation spawnOperation = this.spawnSimulateStack.pop();
            stack.grow(spawnOperation.count);
            return stack;
        }

        @Override
        public void sync() {
            this.growSimulateStack.clear();
            this.shrinkSimulateStack.clear();
            this.cache.pushSpawnList(this.spawnSimulateStack);
            this.spawnSimulateStack.clear();
            this.elements.forEach(ICacheElement::sync);
        }

        @Override
        public int getCount() {
            return this.elements.stream().mapToInt(ICacheElement::getCount).sum();
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean equals(Object key, Range range) {
            if (key == null) return false;
            if (key.getClass() != this.key.getClass()) return false;
            if (!this.range.equals(range)) return false;
            if (key instanceof ItemStack stack && this.key instanceof ItemStack stack1) {
                return ItemStack.isSameItemSameComponents(stack, stack1);
            }
            return this.key.equals(key);
        }
    }
}
