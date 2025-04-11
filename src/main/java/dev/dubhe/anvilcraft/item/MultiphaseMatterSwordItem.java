package dev.dubhe.anvilcraft.item;

import com.mojang.datafixers.util.Unit;
import dev.dubhe.anvilcraft.api.item.IToolProperties;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseMatterSwordItem extends SwordItem {
    public static final IToolProperties.Multiphase DEFAULT_MULTIPHASE = IToolProperties.Multiphase.make(
        Component.translatable("item.anvilcraft.multiphase_matter_sword"), null
    );

    public MultiphaseMatterSwordItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(AxeItem.createAttributes(ModTiers.MULTIPHASE, 8, -2.4f))
                .component(ModComponents.FIRE_REFORGING, Unit.INSTANCE)
                .component(ModComponents.MULTIPHASE, DEFAULT_MULTIPHASE)
        );
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ModComponents.MULTIPHASE, DEFAULT_MULTIPHASE);
        stack.set(DataComponents.ITEM_NAME, DEFAULT_MULTIPHASE.getCustomName());
        stack.set(DataComponents.ENCHANTMENTS, DEFAULT_MULTIPHASE.getEnchantments());
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        IToolProperties.Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        if (multiPhase != null) {
            stack.set(DataComponents.ITEM_NAME, multiPhase.getCustomName());
            stack.set(DataComponents.ENCHANTMENTS, multiPhase.getEnchantments());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        IToolProperties.Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        return multiPhase != null && !multiPhase.equals(DEFAULT_MULTIPHASE)
               ? multiPhase.getCustomName() : super.getName(stack);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        IToolProperties.Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        return multiPhase != null && !multiPhase.equals(DEFAULT_MULTIPHASE)
               ? multiPhase.getEnchantments() : super.getAllEnchantments(stack, lookup);
    }
}
