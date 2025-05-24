package dev.dubhe.anvilcraft.item;

import com.mojang.datafixers.util.Unit;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;

public class FrostMetalHeavyHalberdItem extends TieredItem {
    public FrostMetalHeavyHalberdItem(Properties properties) {
        super(
            ModTiers.FROST_METAL,
            properties
                .attributes(AxeItem.createAttributes(ModTiers.FROST_METAL, 13, -2.4f))
                .component(ModComponents.MERCILESS, Unit.INSTANCE)
        );
    }
}
