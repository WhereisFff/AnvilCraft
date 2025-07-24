package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.property.Eternal;
import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.api.item.property.Providence;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.recipe.multiple.MultipleToOneSmithingRecipeInput;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Objects;

public class TranscendenceResonatorItem extends ResonatorItem {
    public static final Multiphase DEFAULT_MULTIPHASE = Multiphase.make(
        Component.translatable("item.anvilcraft.transcendence_resonator")
    );

    public TranscendenceResonatorItem(Properties properties) {
        super(
            ModTiers.TRANSCENDIUM,
            properties
                .attributes(ResonatorItem.createAttributes(ModTiers.TRANSCENDIUM, 17, -3f))
                .component(ModComponents.MULTIPHASE, DEFAULT_MULTIPHASE.copy())
                .component(ModComponents.ETERNAL, Eternal.INSTANCE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .component(ModComponents.PROVIDENCE, Providence.INSTANCE)
                .component(DataComponents.ITEM_NAME, Objects.requireNonNull(DEFAULT_MULTIPHASE.alpha().getItemName()))
        );
    }

    @Override
    protected double getBaseAttackDamage() {
        return 17;
    }

    @Override
    public ItemStack assemble(int id, MultipleToOneSmithingRecipeInput input, HolderLookup.Provider registries) {
        if (id == 0) {
            ItemStack firstStack = input.getInputItem(0);
            ItemStack secondStack = input.getInputItem(1);
            Multiphase.PhaseData first = Multiphase.PhaseData.of(
                firstStack.get(DataComponents.CUSTOM_NAME), firstStack.get(DataComponents.ITEM_NAME),
                firstStack.getOrDefault(DataComponents.REPAIR_COST, 0),
                firstStack.getOrDefault(EnchantmentHelper.getComponentType(firstStack), ItemEnchantments.EMPTY));
            Multiphase.PhaseData second = Multiphase.PhaseData.of(
                secondStack.get(DataComponents.CUSTOM_NAME), secondStack.get(DataComponents.ITEM_NAME),
                secondStack.getOrDefault(DataComponents.REPAIR_COST, 0),
                secondStack.getOrDefault(EnchantmentHelper.getComponentType(secondStack), ItemEnchantments.EMPTY));

            Multiphase multiphase = Multiphase.make(this, first, second);
            ItemStack result = this.getDefaultInstance();
            result.set(ModComponents.MULTIPHASE, multiphase);
            multiphase.applyToStack(result);

            return result;
        } else if (id == 1) {
            Multiphase multiphase = input.getInputItem(0).getOrDefault(ModComponents.MULTIPHASE, Multiphase.EMPTY);
            Component customName = null;
            if (!multiphase.isEmpty()) {
                customName = multiphase.alpha().getCustomName();
            }
            if (customName == null) {
                customName = input.getInputItem(0).get(DataComponents.CUSTOM_NAME);
            }
            int repairCost = 0;
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (int i = 0; i < 4; i++) {
                multiphase = input.getInputItem(i).getOrDefault(ModComponents.MULTIPHASE, Multiphase.EMPTY);
                if (!multiphase.isEmpty()) {
                    repairCost += multiphase.alpha().repairCost();
                } else {
                    repairCost += input.getInputItem(i).getOrDefault(DataComponents.REPAIR_COST, 0);
                }
                ItemEnchantments enchantmentsSub;
                if (!multiphase.isEmpty()) {
                    enchantmentsSub = multiphase.alpha().enchantments();
                } else {
                    enchantmentsSub = input.getInputItem(i).getOrDefault(
                        EnchantmentHelper.getComponentType(input.getInputItem(i)), ItemEnchantments.EMPTY);
                }
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantmentsSub.entrySet()) {
                    enchantments.set(entry.getKey(), Math.max(enchantments.getLevel(entry.getKey()), entry.getIntValue()));
                }
            }
            Multiphase.PhaseData first = Multiphase.PhaseData.of(customName, null, repairCost, enchantments.toImmutable());

            multiphase = input.getInputItem(0).getOrDefault(ModComponents.MULTIPHASE, Multiphase.EMPTY);
            customName = null;
            if (!multiphase.isEmpty()) {
                customName = multiphase.beta().getCustomName();
            }
            repairCost = 0;
            enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (int i = 0; i < 4; i++) {
                multiphase = input.getInputItem(i).getOrDefault(ModComponents.MULTIPHASE, Multiphase.EMPTY);
                if (!multiphase.isEmpty()) {
                    repairCost += multiphase.beta().repairCost();
                }
                ItemEnchantments enchantmentsSub = ItemEnchantments.EMPTY;
                if (!multiphase.isEmpty()) {
                    enchantmentsSub = multiphase.beta().enchantments();
                }
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantmentsSub.entrySet()) {
                    enchantments.set(entry.getKey(), Math.max(enchantments.getLevel(entry.getKey()), entry.getIntValue()));
                }
            }
            Multiphase.PhaseData second = Multiphase.PhaseData.of(customName, null, repairCost, enchantments.toImmutable());

            multiphase = Multiphase.make(this, first, second);
            ItemStack result = this.getDefaultInstance();
            result.set(ModComponents.MULTIPHASE, multiphase);
            multiphase.applyToStack(result);

            return result;
        }
        return ItemStack.EMPTY;
    }
}
