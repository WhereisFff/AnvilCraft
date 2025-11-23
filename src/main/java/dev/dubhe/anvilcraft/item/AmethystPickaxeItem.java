package dev.dubhe.anvilcraft.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AmethystPickaxeItem extends PickaxeItem implements IntrinsicEnchantedItem {
    public AmethystPickaxeItem(Properties properties) {
        super(ModTiers.AMETHYST, properties.attributes(DiggerItem.createAttributes(ModTiers.AMETHYST, 1, -2.8f)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.anvilcraft.amethyst_pickaxe.tooltip")
            .withStyle(ChatFormatting.GRAY));
        IntrinsicEnchantedItem.appendTooltip(stack, context, lines, flag);
    }

    @ApiStatus.OverrideOnly
    @Override
    public @UnmodifiableView Object2IntMap<ResourceKey<Enchantment>> intrinsicEnchantments(ItemStack stack) {
        return Object2IntMaps.singleton(Enchantments.FORTUNE, 3);
    }
}