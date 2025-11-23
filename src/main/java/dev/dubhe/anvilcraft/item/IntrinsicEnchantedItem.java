package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.util.EnchantmentUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface IntrinsicEnchantedItem extends IItemExtension {
    static void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        var registries = context.registries();
        if (registries != null && stack.getItem() instanceof IntrinsicEnchantedItem item) {
            added(
                EnchantmentUtil.builderOf(),
                item.intrinsicEnchantments(stack),
                registries.lookupOrThrow(Registries.ENCHANTMENT)
            ).toImmutable().addToTooltip(context, lines::add, flag);
        }
    }

    @Contract(value = "_, _, _ -> param1", mutates = "param1")
    private static ItemEnchantments.Mutable added(
        ItemEnchantments.Mutable builder,
        @UnmodifiableView Object2IntMap<ResourceKey<Enchantment>> intrinsicEnchantments,
        HolderLookup.RegistryLookup<Enchantment> registry
    ) {
        for (var entry : intrinsicEnchantments.object2IntEntrySet()) {
            builder.upgrade(registry.getOrThrow(entry.getKey()), entry.getIntValue());
        }
        return builder;
    }

    @ApiStatus.OverrideOnly
    @Override
    default int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchHolder) {
        int nbtLevel = IItemExtension.super.getEnchantmentLevel(stack, enchHolder);
        int intrinsicLevel = intrinsicEnchantments(stack).getInt(enchHolder.getKey());
        return Math.max(nbtLevel, intrinsicLevel);
    }

    @ApiStatus.OverrideOnly
    @Override
    default ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> registry) {
        return added(
            new ItemEnchantments.Mutable(IItemExtension.super.getAllEnchantments(stack, registry)),
            intrinsicEnchantments(stack),
            registry
        ).toImmutable();
    }

    @ApiStatus.OverrideOnly
    default @UnmodifiableView Object2IntMap<ResourceKey<Enchantment>> intrinsicEnchantments(ItemStack stack) {
        return Object2IntMaps.emptyMap();
    }
}