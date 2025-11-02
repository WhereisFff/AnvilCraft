package dev.dubhe.anvilcraft.entity;

import com.google.common.collect.Lists;
import dev.dubhe.anvilcraft.init.entity.ModEntities;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;


import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class SpectralProjectileEntity extends AbstractArrow {

    //TODO：实现这东西的行为
    private static final EntityDataAccessor<ItemStack> AS_ITEM_STACK = SynchedEntityData.defineId(
        SpectralProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    @Nullable
    private IntOpenHashSet piercingIgnoreEntityIds;

    public SpectralProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(AS_ITEM_STACK, Items.ARROW.getDefaultInstance());
    }

    public SpectralProjectileEntity(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.SPECTRAL_PROJECTILE.get(), owner, level, pickupItemStack, firedFromWeapon);
        this.entityData.set(AS_ITEM_STACK, Items.ARROW.getDefaultInstance());
    }

    public static SpectralProjectileEntity of(Level level, LivingEntity owner, ItemStack asStack, @Nullable ItemStack firedFromWeapon) {
        SpectralProjectileEntity sp = new SpectralProjectileEntity(level, owner, ItemStack.EMPTY, firedFromWeapon);
        sp.entityData.set(AS_ITEM_STACK, asStack);
        if (asStack.is(ItemTags.ARROWS)) sp.setBaseDamage(5.0);
        else {
            ItemAttributeModifiers modifiers = asStack.getAttributeModifiers();
            double dmg = modifiers.compute(0, EquipmentSlot.MAINHAND);
            if (dmg > 0) sp.setBaseDamage(dmg * 0.5);
        }
        return sp;
    }

    public ItemStack getAsItemStack() {
        return this.entityData.get(AS_ITEM_STACK);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(AS_ITEM_STACK, Items.ARROW.getDefaultInstance());
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.discard();
        }
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        double d0 = this.getBaseDamage();
        Entity entity1 = this.getOwner();
        DamageSource damagesource = this.damageSources().arrow(this, (Entity) (entity1 != null ? entity1 : this));
        if (this.getWeaponItem() != null) {
            Level var9 = this.level();
            if (var9 instanceof ServerLevel serverlevel) {
                d0 = (double) EnchantmentHelper.modifyDamage(serverlevel, this.getWeaponItem(), entity, damagesource, (float) d0);
            }
        }

        int j = Mth.ceil(Mth.clamp(d0, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long k = this.random.nextInt(j / 2 + 2);
            j = (int) Math.min(k + (long)j, 2147483647L);
        }

        if (entity1 instanceof LivingEntity livingentity1) {
            livingentity1.setLastHurtMob(entity);
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int i = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.igniteForSeconds(5.0F);
        }

        if (entity.hurt(damagesource, (float) j)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity livingentity) {
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                this.doKnockback(livingentity, damagesource);
                Level var13 = this.level();
                if (var13 instanceof ServerLevel serverlevel1) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel1, livingentity, damagesource, this.getWeaponItem());
                }

                this.doPostHurtEffects(livingentity);
                if (livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }
            }

            this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(i);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.getOwner(), false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
