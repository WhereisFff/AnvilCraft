package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.entity.ThrownFrostMetalHeavyHalberdEntity;
import dev.dubhe.anvilcraft.entity.ThrownHeavyHalberdEntity;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModEnchantmentTags;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FrostMetalHeavyHalberdItem extends HeavyHalberdItem {
    public FrostMetalHeavyHalberdItem(Properties properties) {
        super(
            ModTiers.FROST_METAL,
            properties
                .attributes(HeavyHalberdItem.createAttributes(ModTiers.FROST_METAL, 13, -2.4f))
                .component(ModComponents.MERCILESS, true)
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        checkTooDamaged(this.getTier(), stack);
    }

    public static void checkTooDamaged(Tier tier, ItemStack stack) {
        if (isTooDamagedToUse(stack)) {
            stack.set(ModComponents.MERCILESS, false);
            if (stack.has(DataComponents.ENCHANTMENTS)) {
                ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments enchantmentsStored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments.Mutable enchantmentsMutable = new ItemEnchantments.Mutable(enchantments);
                ItemEnchantments.Mutable storedMutable = new ItemEnchantments.Mutable(enchantmentsStored);
                for (Object2IntMap.Entry<Holder<Enchantment>> enchantment : enchantments.entrySet()) {
                    Holder<Enchantment> enchantmentHolder = enchantment.getKey();
                    if (enchantmentHolder.is(ModEnchantmentTags.FROST_PASSED)) continue;
                    int enchantmentLevel = enchantment.getIntValue();
                    int enchantmentStoredLevel = enchantmentsStored.getLevel(enchantmentHolder);
                    if (enchantmentLevel == enchantmentStoredLevel) {
                        storedMutable.set(enchantmentHolder, enchantmentLevel + 1);
                    } else if (enchantmentLevel > enchantmentStoredLevel) {
                        storedMutable.set(enchantmentHolder, enchantmentLevel);
                    }
                    enchantmentsMutable.removeIf(holder -> holder.equals(enchantmentHolder));
                }
                stack.set(DataComponents.STORED_ENCHANTMENTS, storedMutable.toImmutable());
                stack.set(DataComponents.ENCHANTMENTS, enchantmentsMutable.toImmutable());
            }
            if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
                for (ItemAttributeModifiers.Entry entry : stack.getAttributeModifiers().modifiers()) {
                    if (!entry.matches(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE_ID)) {
                        builder.add(entry.attribute(), entry.modifier(), entry.slot());
                    }
                }
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            }
            if (stack.has(DataComponents.TOOL)) {
                stack.remove(DataComponents.TOOL);
            }
        } else {
            stack.set(ModComponents.MERCILESS, true);
            if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
                ItemEnchantments enchantmentsStored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
                for (Object2IntMap.Entry<Holder<Enchantment>> enchantmentStored : enchantmentsStored.entrySet()) {
                    Holder<Enchantment> enchantmentStoredHolder = enchantmentStored.getKey();
                    int enchantmentStoredLevel = enchantmentStored.getIntValue();
                    int enchantmentLevel = enchantments.getLevel(enchantmentStoredHolder);
                    if (enchantmentStoredLevel == enchantmentLevel) {
                        mutable.set(enchantmentStoredHolder, enchantmentStoredLevel + 1);
                    } else if (enchantmentStoredLevel > enchantmentLevel) {
                        mutable.set(enchantmentStoredHolder, enchantmentStoredLevel);
                    }
                }
                stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                stack.remove(DataComponents.STORED_ENCHANTMENTS);
            }
            if (stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                ItemAttributeModifiers modifiers = stack.getAttributeModifiers()
                    .withModifierAdded(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                            BASE_ATTACK_DAMAGE_ID,
                            13 + tier.getAttackDamageBonus(),
                            AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                    );
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
            }
            if (!stack.has(DataComponents.TOOL)) {
                stack.set(DataComponents.TOOL, createToolProperties(tier));
            }
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        int willDamage = super.damageItem(stack, amount, entity, onBroken);
        return (stack.getMaxDamage() - 1 - stack.getDamageValue() - willDamage) < 0 ? 0 : willDamage;
    }

    @Override
    public ThrownHeavyHalberdEntity createThrown(Level level, LivingEntity shooter, ItemStack pickupItemStack) {
        return new ThrownFrostMetalHeavyHalberdEntity(level, shooter, pickupItemStack);
    }

    @Override
    public ThrownHeavyHalberdEntity createThrown(Level level, double x, double y, double z, ItemStack pickupItemStack) {
        return new ThrownFrostMetalHeavyHalberdEntity(level, x, y, z, pickupItemStack);
    }
}
