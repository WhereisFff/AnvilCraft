package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MorphableAxeItem extends AxeItem {
    public MorphableAxeItem(Properties properties) {
        super(
            ModTiers.MORPHABLE,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.MORPHABLE, 10, -3f))
                .component(ModComponents.FIRE_REFORGING, ToolAttributes.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, ToolAttributes.Tough.INSTANCE)
                .component(ModComponents.MORPH, ToolAttributes.Morph.INSTANCE)
        );
    }
}
