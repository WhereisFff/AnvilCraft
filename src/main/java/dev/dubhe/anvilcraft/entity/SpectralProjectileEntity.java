package dev.dubhe.anvilcraft.entity;

import dev.dubhe.anvilcraft.init.entity.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpectralProjectileEntity extends AbstractArrow {

    private static final EntityDataAccessor<ItemStack> AS_ITEM_STACK = SynchedEntityData.defineId(
        SpectralProjectileEntity.class, EntityDataSerializers.ITEM_STACK);

    public SpectralProjectileEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(AS_ITEM_STACK, Items.ARROW.getDefaultInstance());
    }

    public static SpectralProjectileEntity of(Level level, ItemStack stack) {
        SpectralProjectileEntity sp = new SpectralProjectileEntity(ModEntities.SPECTRAL_PROJECTILE.get(), level);
        sp.entityData.set(AS_ITEM_STACK, stack);
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
