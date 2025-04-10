package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EmberMetalSwordItem extends SwordItem {
    public EmberMetalSwordItem(Properties properties) {
        super(
            ModTiers.EMBER_METAL,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.EMBER_METAL, 8, -2.4f))
                .component(ModComponents.FIRE_REFORGING, IToolProperties.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolProperties.Tough.INSTANCE)
        );
    }
}
