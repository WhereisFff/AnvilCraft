package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.HoeItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EmberMetalHoeItem extends HoeItem {
    public EmberMetalHoeItem(Properties properties) {
        super(
            ModTiers.EMBER_METAL,
            properties.fireResistant()
                .attributes(HoeItem.createAttributes(ModTiers.EMBER_METAL, 1, 0))
                .component(ModComponents.FIRE_REFORGING, ToolAttributes.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, ToolAttributes.Tough.INSTANCE)
                .component(ModComponents.MORPH, ToolAttributes.Morph.INSTANCE)
        );
    }
}
