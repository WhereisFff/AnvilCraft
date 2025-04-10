package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolAttributes;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.AxeItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EmberMetalAxeItem extends AxeItem {
    public EmberMetalAxeItem(Properties properties) {
        super(
            ModTiers.EMBER_METAL,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.EMBER_METAL, 10, -3f))
                .component(ModComponents.FIRE_REFORGING, IToolAttributes.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolAttributes.Tough.INSTANCE)
        );
    }
}
