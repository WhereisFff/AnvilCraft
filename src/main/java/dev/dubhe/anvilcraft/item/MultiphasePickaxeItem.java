package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IToolAttributes;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.PickaxeItem;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphasePickaxeItem extends PickaxeItem {
    public MultiphasePickaxeItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.MULTIPHASE, 6, -2.8f))
                .component(ModComponents.FIRE_REFORGING, IToolAttributes.FireReforging.INSTANCE)
                .component(ModComponents.TOUGH, IToolAttributes.Tough.INSTANCE)
                .component(ModComponents.MULTIPHASE, IToolAttributes.Multiphase.make(
                    Component.translatable("item.anvilcraft.multiphase_pickaxe"), null
                ))
        );
    }
}
