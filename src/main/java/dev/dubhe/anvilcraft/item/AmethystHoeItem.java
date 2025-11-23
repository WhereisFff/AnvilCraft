package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.enchantment.ModEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public class AmethystHoeItem extends HoeItem implements IntrinsicEnchantedItem {
    public AmethystHoeItem(Properties properties) {
        super(ModTiers.AMETHYST, properties.attributes(DiggerItem.createAttributes(ModTiers.AMETHYST, -1, -2.0f)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        IntrinsicEnchantedItem.appendTooltip(stack, context, lines, flag);
    }

    @ApiStatus.OverrideOnly
    @Override
    public @UnmodifiableView Object2IntMap<ResourceKey<Enchantment>> intrinsicEnchantments(ItemStack stack) {
        return Object2IntMaps.singleton(ModEnchantments.HARVEST_KEY, 1);
    }
}