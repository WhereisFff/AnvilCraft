package dev.dubhe.anvilcraft.item;

import com.mojang.datafixers.util.Unit;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.TieredItem;

public class EmberMetalHeavyHalberdItem extends TieredItem {
    public EmberMetalHeavyHalberdItem(Properties properties) {
        super(
            ModTiers.EMBER_METAL,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.EMBER_METAL, 13, -2.4f))
                .component(ModComponents.MERCILESS, Unit.INSTANCE)
        );
    }
}
