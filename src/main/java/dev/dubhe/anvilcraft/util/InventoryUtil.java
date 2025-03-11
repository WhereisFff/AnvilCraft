package dev.dubhe.anvilcraft.util;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class InventoryUtil {
    public static BiConsumer<LinkedList<ItemStack>, LivingEntity> compatConsumer = NonNullBiConsumer.noop();

    public static ItemStack getFirstItem(Inventory inventory, Item item) {
        for (ItemStack stack : inventory.items) {
            if (stack.getItem().equals(item)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack getFirstItem(Inventory inventory, ItemEntry<? extends Item> item) {
        for (ItemStack stack : getItems(inventory)) {
            if (item.isIn(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack getFirstItem(Inventory inventory, Predicate<ItemStack> filter) {
        for (ItemStack stack : getItems(inventory)) {
            if (filter.test(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static LinkedList<ItemStack> getItems(Inventory inventory) {
        LinkedList<ItemStack> items = new LinkedList<>();

        compatConsumer.accept(items, inventory.player);
        items.addAll(inventory.items);
        items.addAll(inventory.armor);
        items.addAll(inventory.offhand);

        return items;
    }

    public static LinkedList<ItemStack> getItems(Inventory inventory, Item item) {
        LinkedList<ItemStack> items = getItems(inventory);
        items.removeIf(stack -> stack.getItem().equals(item));
        return items;
    }

    public static boolean hasItem(Inventory inventory, Item item) {
        return !getFirstItem(inventory, item).equals(ItemStack.EMPTY);
    }

    public static boolean hasItem(Inventory inventory, ItemEntry<? extends Item> item) {
        return !getFirstItem(inventory, item).equals(ItemStack.EMPTY);
    }

    public static void addToInventory(Inventory inventory, ItemStack stack) {
        if (inventory.getFreeSlot() != -1) {
            inventory.add(stack);
        } else {
            inventory.player.drop(stack, false, true);
        }
    }
}
