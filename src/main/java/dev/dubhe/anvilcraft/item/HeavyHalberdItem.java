package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IMultipleResult;
import dev.dubhe.anvilcraft.entity.ThrownHeavyHalberdEntity;
import dev.dubhe.anvilcraft.recipe.multiple.MultipleToOneSmithingRecipeInput;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;

public abstract class HeavyHalberdItem extends TieredItem implements ProjectileItem, IMultipleResult {
    public HeavyHalberdItem(Tier tier, Properties properties) {
        super(
            tier,
            properties
                .component(DataComponents.TOOL, createToolProperties(tier))
                .durability(tier.getUses())
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
        ArrayList<Tool.Rule> rules = new ArrayList<>();
        rules.addAll(SwordItem.createToolProperties().rules());
        rules.addAll(tier.createToolProperties(BlockTags.MINEABLE_WITH_AXE).rules());
        return new Tool(rules, 1.0F, 2);
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

    /**
     * Returns the action that specifies what animation to play when the item is being used.
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) return;
        int i = this.getUseDuration(stack, entityLiving) - timeLeft;
        if (i < 10) return;
        float spinStrength = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
        if (spinStrength > 0.0F && !player.isInWaterOrRain()) return;
        if (isTooDamagedToUse(stack)) return;
        Holder<SoundEvent> soundEvent = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
            .orElse(SoundEvents.TRIDENT_THROW);
        if (!level.isClientSide) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(entityLiving.getUsedItemHand()));
            if (spinStrength == 0.0F) {
                ThrownHeavyHalberdEntity thrown = this.createThrown(level, player, stack);
                thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                if (player.hasInfiniteMaterials()) {
                    thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }

                level.addFreshEntity(thrown);
                level.playSound(null, thrown, soundEvent.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!player.hasInfiniteMaterials()) {
                    player.getInventory().removeItem(stack);
                }
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (spinStrength <= 0.0F) return;
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        float xDelta = -Mth.sin(yRot * (float) (Math.PI / 180.0)) * Mth.cos(xRot * (float) (Math.PI / 180.0));
        float yDelta = -Mth.sin(xRot * (float) (Math.PI / 180.0));
        float zDelta = Mth.cos(yRot * (float) (Math.PI / 180.0)) * Mth.cos(xRot * (float) (Math.PI / 180.0));
        float fixer = Mth.sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta);
        xDelta *= spinStrength / fixer;
        yDelta *= spinStrength / fixer;
        zDelta *= spinStrength / fixer;
        player.push(xDelta, yDelta, zDelta);
        player.startAutoSpinAttack(20, 8.0F, stack);
        if (player.onGround()) {
            float yFix = 1.1999999F;
            player.move(MoverType.SELF, new Vec3(0.0, yFix, 0.0));
        }

        level.playSound(null, player, soundEvent.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (isTooDamagedToUse(itemstack)) {
            return InteractionResultHolder.fail(itemstack);
        } else if (EnchantmentHelper.getTridentSpinAttackStrength(itemstack, player) > 0.0F && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        }
    }

    protected static boolean isTooDamagedToUse(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player) || !MaceItem.canSmashAttack(player)) return true;
        ServerLevel level = (ServerLevel) attacker.level();
        if (player.isIgnoringFallDamageFromCurrentImpulse() && player.currentImpulseImpactPos != null) {
            if (player.currentImpulseImpactPos.y > player.position().y) {
                player.currentImpulseImpactPos = player.position();
            }
        } else {
            player.currentImpulseImpactPos = player.position();
        }

        player.setIgnoreFallDamageFromCurrentImpulse(true);
        player.setDeltaMovement(player.getDeltaMovement().with(Direction.Axis.Y, 0.01F));
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        if (target.onGround()) {
            player.setSpawnExtraParticlesOnFall(true);
            SoundEvent soundEvent = player.fallDistance > 5.0F ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
            level.playSound(
                null, player.getX(), player.getY(), player.getZ(), soundEvent, player.getSoundSource(), 1.0F, 1.0F
            );
        } else {
            level.playSound(
                null, player.getX(), player.getY(), player.getZ(), SoundEvents.MACE_SMASH_AIR, player.getSoundSource(), 1.0F, 1.0F
            );
        }

        MaceItem.knockback(level, player, target);
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        if (MaceItem.canSmashAttack(attacker)) {
            attacker.resetFallDistance();
        }
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        if (!(source.getDirectEntity() instanceof LivingEntity entity)) return 0.0F;
        if (!MaceItem.canSmashAttack(entity)) return 0.0F;

        float firstMaxHeight = 3.0F;
        float secondMaxHeight = 8.0F;
        float fallDistance = entity.fallDistance;

        float damageBonus;
        if (fallDistance <= firstMaxHeight) {
            damageBonus = 4.0F * fallDistance;
        } else if (fallDistance <= secondMaxHeight) {
            damageBonus = 12.0F + 2.0F * (fallDistance - firstMaxHeight);
        } else {
            damageBonus = 22.0F + fallDistance - secondMaxHeight;
        }

        return entity.level() instanceof ServerLevel level
               ? damageBonus + EnchantmentHelper.modifyFallBasedDamage(level, entity.getWeaponItem(), target, source, 0.0F) * fallDistance
               : damageBonus;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ThrownHeavyHalberdEntity thrown = this.createThrown(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
        thrown.pickup = AbstractArrow.Pickup.ALLOWED;
        return thrown;
    }

    public abstract ThrownHeavyHalberdEntity createThrown(Level level, LivingEntity shooter, ItemStack pickupItemStack);

    public abstract ThrownHeavyHalberdEntity createThrown(Level level, double x, double y, double z, ItemStack pickupItemStack);

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_TRIDENT_ACTIONS.contains(itemAbility);
    }
}
