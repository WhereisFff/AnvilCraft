package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RoyalAxeItem extends AxeItem {
    public RoyalAxeItem(Properties properties) {
        super(Tiers.DIAMOND, properties.attributes(AxeItem.createAttributes(ModTiers.AMETHYST, 5, -3.0f)));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack defaultInstance = super.getDefaultInstance();

        defaultInstance.set(ModComponents.TOUGH, new ToolAttributes.Tough());

        return defaultInstance;
    }
}
