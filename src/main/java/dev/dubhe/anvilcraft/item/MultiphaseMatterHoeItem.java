package dev.dubhe.anvilcraft.item;

import com.mojang.datafixers.util.Unit;
import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseMatterHoeItem extends HoeItem {
    public MultiphaseMatterHoeItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(HoeItem.createAttributes(ModTiers.MULTIPHASE, 1, 0))
                .component(ModComponents.FIRE_REFORGING, Unit.INSTANCE)
                .component(ModComponents.TOUGH, Unit.INSTANCE)
                .component(ModComponents.MULTIPHASE, IToolProperties.Multiphase.make(
                    Component.translatable("item.anvilcraft.multiphase_matter_hoe"), null
                ))
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        IToolProperties.Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        if (multiPhase != null) {
            stack.set(DataComponents.CUSTOM_NAME, multiPhase.getCustomName());
            stack.set(DataComponents.ENCHANTMENTS, multiPhase.getEnchantments());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        IToolProperties.Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        return multiPhase != null ? multiPhase.getCustomName() : super.getName(stack);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        IToolProperties.Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        return multiPhase != null ? multiPhase.getEnchantments() : super.getAllEnchantments(stack, lookup);
    }
}
