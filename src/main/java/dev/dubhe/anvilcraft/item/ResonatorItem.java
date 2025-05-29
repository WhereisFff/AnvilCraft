package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IMultipleResult;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModEnchantmentTags;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.recipe.multiple.MultipleToOneSmithingRecipeInput;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

public class ResonatorItem extends TieredItem implements IMultipleResult {
    public static final int AUTO_MODE = 0;
    public static final int AXE_MODE = 1;
    public static final int SHOVEL_MODE = 2;
    public static final int HOE_MODE = 3;
    public static final int PICKAXE_MODE = 4;

    public ResonatorItem(Tier tier, Properties properties) {
        super(
            tier,
            properties
                .component(DataComponents.TOOL, createToolProperties(tier))
                .fireResistant()
                .rarity(Rarity.EPIC)
        );
    }

    public static ItemAttributeModifiers createAttributes(Tier tier, float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    BASE_ATTACK_DAMAGE_ID, attackDamage + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .build();
    }

    public static Tool createToolProperties(Tier tier) {
        List<Tool.Rule> rules = new ArrayList<>();
        rules.addAll(SwordItem.createToolProperties().rules());
        rules.addAll(ShearsItem.createToolProperties().rules());
        rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, tier.getSpeed()));
        rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, tier.getSpeed()));
        rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, tier.getSpeed()));
        rules.add(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tier.getSpeed()));
        return new Tool(List.copyOf(rules), 1.0F, 1);
    }

    public static Tool createToolProperties(@Range(from = 0, to = 4) int mode, Tier tier) {
        return switch (mode) {
            case AUTO_MODE -> createToolProperties(tier);
            case AXE_MODE -> new Tool(List.of(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, tier.getSpeed())), 1.0F, 1);
            case SHOVEL_MODE -> new Tool(List.of(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tier.getSpeed())), 1.0F, 1);
            case HOE_MODE -> new Tool(List.of(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, tier.getSpeed())), 1.0F, 1);
            case PICKAXE_MODE -> new Tool(List.of(Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, tier.getSpeed())), 1.0F, 1);
            default -> throw new IllegalStateException("Unexpected mode: " + mode);
        };
    }

    public static void checkTooDamaged(Tier tier, ItemStack stack) {
        if (isTooDamagedToUse(stack)) {
            if (stack.has(ModComponents.MERCILESS)) {
                stack.set(ModComponents.MERCILESS, false);
            }
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
            if (stack.has(ModComponents.MERCILESS)) {
                stack.set(ModComponents.MERCILESS, true);
            }
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
                stack.set(DataComponents.TOOL, createToolProperties((int) stack.getOrDefault(DataComponents.CUSTOM_MODEL_DATA, 0), tier));
            }
        }
    }

    @Override
    public ItemStack assemble(int id, MultipleToOneSmithingRecipeInput input, HolderLookup.Provider registries) {
        if (id == 0) {
            ItemStack defaultStack = this.getDefaultInstance();

            Object2IntMap<Holder<Enchantment>> enchantments = new Object2IntArrayMap<>();
            for (int i = 0; i < 4; i++) {
                ItemStack inputStack = input.getInputItem(i);
                for (
                    Object2IntMap.Entry<Holder<Enchantment>> entry
                    : inputStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet()
                ) {
                    enchantments.mergeInt(entry.getKey(), entry.getIntValue(), Integer::max);
                }
            }

            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                defaultStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.object2IntEntrySet()) {
                mutable.set(entry.getKey(), entry.getIntValue());
            }
            defaultStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

            return defaultStack;
        }
        return ItemStack.EMPTY;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Tool tool = stack.get(DataComponents.TOOL);
        return tool != null ? tool.getMiningSpeed(state) : this.getTier().getSpeed();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
    }

    protected static boolean isTooDamagedToUse(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return switch ((int) context.getItemInHand().getOrDefault(DataComponents.CUSTOM_MODEL_DATA, 0)) {
            case AXE_MODE -> useOnAsAxe(this, context);
            case SHOVEL_MODE -> useOnAsShovel(this, context);
            case HOE_MODE -> useOnAsHoe(this, context);
            case PICKAXE_MODE -> useOnAsPickaxe(this, context);
            default -> super.useOn(context);
        };
    }

    public static InteractionResult useOnAsAxe(ResonatorItem resonator, UseOnContext ctx) {
        return new AxeItem(resonator.getTier(), new Properties()).useOn(ctx);
    }

    public static InteractionResult useOnAsShovel(ResonatorItem resonator, UseOnContext ctx) {
        return new ShovelItem(resonator.getTier(), new Properties()).useOn(ctx);
    }

    public static InteractionResult useOnAsHoe(ResonatorItem resonator, UseOnContext ctx) {
        return new HoeItem(resonator.getTier(), new Properties()).useOn(ctx);
    }

    public static InteractionResult useOnAsPickaxe(ResonatorItem resonator, UseOnContext ctx) {
        return new PickaxeItem(resonator.getTier(), new Properties()).useOn(ctx);
    }

    public static void set(ServerPlayer player, InteractionHand hand, @Range(from = 0, to = 4) int mode) {
        ItemStack resonator = player.getItemInHand(hand);
        if (!resonator.is(ModItemTags.RESONATOR)) return;
        Item item = resonator.getItem();
        if (!(item instanceof TieredItem resonatorItem)) return;
        resonator.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(mode));
        resonator.set(DataComponents.TOOL, createToolProperties(mode, resonatorItem.getTier()));
    }
}
