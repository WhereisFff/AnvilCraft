package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MorphableHoeItem extends HoeItem {
    public MorphableHoeItem(Properties properties) {
        super(
            ModTiers.MORPHABLE,
            properties.fireResistant()
                .attributes(HoeItem.createAttributes(ModTiers.MORPHABLE, 1, 0))
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
