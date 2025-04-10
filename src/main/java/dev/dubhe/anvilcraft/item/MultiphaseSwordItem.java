package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseSwordItem extends SwordItem {
    public MultiphaseSwordItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.MULTIPHASE, 8, -2.4f))
                .component(ModComponents.FIRE_REFORGING, IToolProperties.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolProperties.Tough.INSTANCE)
                .component(ModComponents.MULTIPHASE, IToolProperties.Multiphase.make(
                    Component.translatable("item.anvilcraft.multiphase_sword"), null
                ))
        );
    }
}
