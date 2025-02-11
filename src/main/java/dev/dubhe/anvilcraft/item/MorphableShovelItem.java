package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MorphableShovelItem extends ShovelItem {
    public MorphableShovelItem(Properties properties) {
        super(
            ModTiers.MORPHABLE,
            properties.fireResistant()
                .attributes(ShovelItem.createAttributes(ModTiers.MORPHABLE, 6.5f, -3f))
                .component(ModComponents.FIRE_REFORGING, ToolAttributes.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, ToolAttributes.Tough.INSTANCE)
                .component(ModComponents.MORPH, ToolAttributes.Morph.INSTANCE)
        );
    }
}
