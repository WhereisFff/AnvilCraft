package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ShovelItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseShovelItem extends ShovelItem {
    public MultiphaseShovelItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(ShovelItem.createAttributes(ModTiers.MULTIPHASE, 6.5f, -3f))
                .component(ModComponents.FIRE_REFORGING, IToolProperties.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolProperties.Tough.INSTANCE)
                .component(ModComponents.MULTIPHASE, IToolProperties.Multiphase.make(
                    Component.translatable("item.anvilcraft.multiphase_shovel"), null
                ))
        );
    }
}
