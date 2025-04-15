package dev.dubhe.anvilcraft.api.itemhandler;

import dev.dubhe.anvilcraft.util.AnvilUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ItemHandlerUtil {
    public static boolean exportToTarget(
        IItemHandler source,
        int maxAmount,
        Predicate<ItemStack> predicate,
        IItemHandler target
    ) {
        boolean success = false;
        Item filterItem = null;
        for (int srcIndex = 0; srcIndex < source.getSlots(); srcIndex++) {
            ItemStack sourceStack = source.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !predicate.test(sourceStack)) {
                continue;
            }
            if (filterItem == null) {
                filterItem = sourceStack.getItem();
                maxAmount = maxAmount / 64 * sourceStack.getMaxStackSize(); //根据最大堆叠设置maxAmount 默认情况完全等于最大堆叠
            }
            if (sourceStack.getItem() != filterItem) continue;

            ItemStack remainder = ItemHandlerHelper.insertItem(target, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = source.extractItem(srcIndex, Math.min(maxAmount, amountToInsert), false);
                ItemHandlerHelper.insertItem(target, sourceStack, false);
                success = true;
                maxAmount -= amountToInsert;
                if (maxAmount <= 0) break;
            }
        }
        return success;
    }

    public static boolean importFromTarget(
        IItemHandler target,
        int maxAmount,
        Predicate<ItemStack> predicate,
        IItemHandler source
    ) {
        int amount = 64;
        boolean success = false;
        Item filterItem = null;
        for (int srcIndex = 0; srcIndex < source.getSlots(); srcIndex++) {
            ItemStack sourceStack = source.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !predicate.test(sourceStack)) {
                continue;
            }
            if (filterItem == null) {
                filterItem = sourceStack.getItem();
                amount = maxAmount / 64 * sourceStack.getMaxStackSize(); //根据最大堆叠设置maxAmount 默认情况完全等于最大堆叠
            }
            if (sourceStack.getItem() != filterItem) continue;
            ItemStack remainder = ItemHandlerHelper.insertItem(target, sourceStack, true);
            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert == 0) {
                filterItem = null;
                continue;
            }
            if (amountToInsert > 0) {
                sourceStack = source.extractItem(srcIndex, Math.min(amount, amountToInsert), false);
                ItemHandlerHelper.insertItem(target, sourceStack, false);
                success = true;
                amount -= amountToInsert;
                if (amount <= 0) break;
            }
        }
        return success;
    }

    public static void exportAllToTarget(IItemHandler source, Predicate<ItemStack> predicate, IItemHandler target) {
        for (int srcIndex = 0; srcIndex < source.getSlots(); srcIndex++) {
            ItemStack sourceStack = source.extractItem(srcIndex, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !predicate.test(sourceStack)) continue;

            ItemStack remainder = ItemHandlerHelper.insertItem(target, sourceStack, true);

            int amountToInsert = sourceStack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                sourceStack = source.extractItem(srcIndex, amountToInsert, false);
                ItemHandlerHelper.insertItem(target, sourceStack, false);
            }
        }
    }

    public static void exportContentsToItemHandlers(IItemHandler source, @Nullable List<IItemHandler> itemHandlerList) {
        if (itemHandlerList == null) return;
        for (IItemHandler target : itemHandlerList) {
            exportAllToTarget(source, stack -> true, target);
        }
    }

    public static void exportItemsToItemHandlers(List<ItemStack> items, @Nullable List<IItemHandler> itemHandlerList) {
        if (itemHandlerList == null) return;
        for (var stack : items) {
            for (IItemHandler target : itemHandlerList) {
                stack = ItemHandlerHelper.insertItem(target, stack, false);
            }
        }
    }

    public static void dropAllToPos(@NotNull IItemHandler source, Level level, Vec3 pos) {
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack stack = source.extractItem(slot, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) items.add(stack);
        }
        AnvilUtil.dropItems(items, level, pos);
    }

    public static IItemHandler getSourceItemHandlerList(BlockPos inputBlockPos, Direction context, Level level) {
        if (level == null) return null;
        IItemHandler input = level.getCapability(
            Capabilities.ItemHandler.BLOCK,
            inputBlockPos,
            context
        );
        if (input != null) {
            return input;
        }
        AABB aabb = new AABB(inputBlockPos);
        List<ContainerEntity> entities = level.getEntitiesOfClass(
                Entity.class, aabb, e -> e instanceof ContainerEntity && !((ContainerEntity) e).isEmpty())
            .stream()
            .map(it -> (ContainerEntity) it)
            .toList();
        if (!entities.isEmpty()) {
            input = ((Entity) entities.getFirst()).getCapability(
                Capabilities.ItemHandler.ENTITY,
                null
            );
        }
        return input;
    }

    public static List<IItemHandler> getTargetItemHandlerList(BlockPos inputBlockPos, Direction context, Level level) {
        if (level == null) return null;
        List<IItemHandler> list = new ArrayList<>();
        IItemHandler input = level.getCapability(
            Capabilities.ItemHandler.BLOCK,
            inputBlockPos,
            context
        );
        if (input != null) {
            list.add(input);
            return list;
        }
        AABB aabb = new AABB(inputBlockPos);
        list = level.getEntitiesOfClass(
                Entity.class, aabb, e -> e instanceof ContainerEntity)
            .stream()
            .map(e -> e.getCapability(
                Capabilities.ItemHandler.ENTITY,
                null
            ))
            .toList();
        return list;
    }

    public static int countItemsInHandler(IItemHandler handler) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            count += handler.getStackInSlot(i).getCount();
        }
        return count;
    }

    public static Object2IntMap<Item> mergeHandlerItems(IItemHandler handler) {
        Object2IntMap<Item> items = new Object2IntOpenHashMap<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            items.mergeInt(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return items;
    }
}
