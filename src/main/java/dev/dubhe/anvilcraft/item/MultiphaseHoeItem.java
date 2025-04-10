package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolAttributes;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HoeItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseHoeItem extends HoeItem {
    public MultiphaseHoeItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(HoeItem.createAttributes(ModTiers.MULTIPHASE, 1, 0))
                .component(ModComponents.FIRE_REFORGING, IToolAttributes.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolAttributes.Tough.INSTANCE)
                .component(ModComponents.MULTIPHASE, IToolAttributes.Multiphase.make(
                    Component.translatable("item.anvilcraft.multiphase_hoe"), null
                ))
        );
    }
}
