package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseAxeItem extends AxeItem {
    public MultiphaseAxeItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.MULTIPHASE, 10, -3f))
                .component(ModComponents.FIRE_REFORGING, IToolProperties.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolProperties.Tough.INSTANCE)
                .component(ModComponents.MULTIPHASE, IToolProperties.Multiphase.make(
                    Component.translatable("item.anvilcraft.multiphase_axe"), null
                ))
        );
    }
}
