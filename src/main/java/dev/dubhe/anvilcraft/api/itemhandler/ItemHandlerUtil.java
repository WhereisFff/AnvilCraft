package dev.dubhe.anvilcraft.api.itemhandler;

import dev.dubhe.anvilcraft.AnvilCraft;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static dev.dubhe.anvilcraft.block.BlockPlacerBlock.ORIENTATION;

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

    @Nullable
    public static IItemHandler getSourceItemHandlerRecursive(Block source, BlockPos inputBlockPos, Direction context, Level level) {
        int i = 0;
        do {
            if (level == null) return null;
            if (level.getBlockState(inputBlockPos).is(source)
                && level.getBlockState(inputBlockPos).getValue(ORIENTATION).getDirection() == context
            ) {
                i++;
                inputBlockPos = inputBlockPos.relative(context.getOpposite());
            } else {
                return dev.dubhe.anvilcraft.util.ItemHandlerUtil.getSourceItemHandler(inputBlockPos, context, level);
            }
        } while (i < AnvilCraft.config.blockPlacerRecursiveRetrievalDistanceMax);
        return null;
    }

    public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty()) {
            return stack;
        }

        if (dest instanceof PollableFilteredItemStackHandler pollable) {
            for (int i = 0; i < dest.getSlots(); i++) {
                stack = pollable.insertItemNoPolling(i, stack, simulate);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }

            return stack;
        } else {
            return ItemHandlerHelper.insertItem(dest, stack, simulate);
        }
    }
}
