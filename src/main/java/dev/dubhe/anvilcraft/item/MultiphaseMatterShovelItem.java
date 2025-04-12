package dev.dubhe.anvilcraft.item;

import com.mojang.datafixers.util.Unit;
import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.util.EnchantmentUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiphaseMatterShovelItem extends ShovelItem {
    public static final Multiphase DEFAULT_MULTIPHASE = Multiphase.make(
        Component.translatable("item.anvilcraft.multiphase_matter_shovel")
    );

    public MultiphaseMatterShovelItem(Properties properties) {
        super(
            ModTiers.MULTIPHASE,
            properties.fireResistant()
                .attributes(ShovelItem.createAttributes(ModTiers.MULTIPHASE, 6.5f, -3f))
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
        Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        if (multiPhase != null) {
            if (multiPhase.equalsLoose(DEFAULT_MULTIPHASE)) {
                stack.set(DataComponents.ITEM_NAME, multiPhase.getCustomName());
            } else {
                stack.set(DataComponents.CUSTOM_NAME, multiPhase.getCustomName());
            }
            stack.set(DataComponents.ENCHANTMENTS, multiPhase.getEnchantments());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        return multiPhase != null && !multiPhase.equalsLoose(DEFAULT_MULTIPHASE)
               ? multiPhase.getCustomName() : super.getName(stack);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        Multiphase multiPhase = stack.get(ModComponents.MULTIPHASE);
        return multiPhase != null && !multiPhase.equalsLoose(DEFAULT_MULTIPHASE)
               ? multiPhase.getEnchantments() : super.getAllEnchantments(stack, lookup);
    }

    @Override
    public ItemStack applyEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        if (stack.has(ModComponents.MULTIPHASE)) {
            EnchantmentUtil.updateEnchantmentsForMultiphase(
                stack, mutable -> {
                    for (EnchantmentInstance inst : enchantments) {
                        mutable.set(inst.enchantment, inst.level);
                    }
                });
            return stack;
        }
        return super.applyEnchantments(stack, enchantments);
    }
}
