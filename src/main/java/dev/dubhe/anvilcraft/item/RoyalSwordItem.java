package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RoyalSwordItem extends SwordItem {
    public RoyalSwordItem(Properties properties) {
        super(Tiers.DIAMOND, properties.attributes(AxeItem.createAttributes(Tiers.DIAMOND, 3, -2.4f)));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack defaultInstance = super.getDefaultInstance();

        defaultInstance.set(ModComponents.TOUGH, new ToolAttributes.Tough());

        return defaultInstance;
    }
}
