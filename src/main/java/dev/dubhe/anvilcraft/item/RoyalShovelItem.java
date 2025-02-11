package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RoyalShovelItem extends ShovelItem {
    public RoyalShovelItem(Properties properties) {
        super(Tiers.DIAMOND, properties.attributes(AxeItem.createAttributes(Tiers.DIAMOND, 1.5f, -3.0f)));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack defaultInstance = super.getDefaultInstance();

        defaultInstance.set(ModComponents.TOUGH, new ToolAttributes.Tough());

        return defaultInstance;
    }
}
