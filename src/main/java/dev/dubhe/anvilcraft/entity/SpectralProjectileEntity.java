package dev.dubhe.anvilcraft.entity;

import dev.dubhe.anvilcraft.init.entity.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SpectralProjectileEntity extends AbstractArrow {

    //TODO：实现这东西的行为
    private static final EntityDataAccessor<ItemStack> AS_ITEM_STACK = SynchedEntityData.defineId(
        SpectralProjectileEntity.class, EntityDataSerializers.ITEM_STACK);

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
}
