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
        );
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack defaultInstance = super.getDefaultInstance();

        defaultInstance.set(ModComponents.FIRE_REFORGING, new ToolAttributes.FireReforging());
        defaultInstance.set(ModComponents.TOUGH, new ToolAttributes.Tough());
        defaultInstance.set(ModComponents.MORPH, new ToolAttributes.Morph());

        return defaultInstance;
    }
}
