package dev.dubhe.anvilcraft.entity;

import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

public class EternalItemEntity extends ItemEntity {
    public EternalItemEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    public EternalItemEntity(Level level, double posX, double posY, double posZ, ItemStack stack) {
        super(level, posX, posY, posZ, stack);
    }

    public EternalItemEntity(
        Level level, double posX, double posY, double posZ, ItemStack stack, double deltaX, double deltaY, double deltaZ
    ) {
        super(level, posX, posY, posZ, stack, deltaX, deltaY, deltaZ);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getItem().has(ModComponents.ETERNAL)
            && (source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.CACTUS))
        ) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        if (getItem().onEntityItemUpdate(this)) return;
        if (this.getItem().isEmpty()) {
            this.discard();
            return;
        }

        super.tick();
        if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            this.pickupDelay--;
        }

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 vec3 = this.getDeltaMovement();
        FluidType fluidType = this.getMaxHeightFluidType();
        if (!fluidType.isAir() && !fluidType.isVanilla() && this.getFluidTypeHeight(fluidType) > 0.1F) {
            fluidType.setItemMovement(this);
        } else if (this.isInWater() && this.getFluidTypeHeight(NeoForgeMod.WATER_TYPE.value()) > 0.1F) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidTypeHeight(NeoForgeMod.LAVA_TYPE.value()) > 0.1F) {
            this.setUnderLavaMovement();
        } else if (this.getY() < this.level().getMinBuildHeight() + 5) {
            this.setDeltaMovement(this.getDeltaMovement().with(Direction.Axis.Y, 0.1F));
        } else {
            this.applyGravity();
        }

        if (this.level().isClientSide) {
            this.noPhysics = false;
        } else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f = 0.98F;
            if (this.onGround()) {
                BlockPos groundPos = getBlockPosBelowThatAffectsMyMovement();
                f = this.level().getBlockState(groundPos).getFriction(level(), groundPos, this) * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.98, f));
            if (this.onGround()) {
                Vec3 vec31 = this.getDeltaMovement();
                if (vec31.y < 0.0) {
                    this.setDeltaMovement(vec31.multiply(1.0, -0.5, 1.0));
                }
            }
        }

        boolean flag = Mth.floor(this.xo) != Mth.floor(this.getX())
                       || Mth.floor(this.yo) != Mth.floor(this.getY())
                       || Mth.floor(this.zo) != Mth.floor(this.getZ());
        int i = flag ? 2 : 40;
        if (this.tickCount % i == 0 && !this.level().isClientSide && this.isMergable()) {
            this.mergeWithNeighbours();
        }

        this.hasImpulse = this.hasImpulse | this.updateInWaterStateAndDoFluidPushing();
        if (!this.level().isClientSide) {
            double d0 = this.getDeltaMovement().subtract(vec3).lengthSqr();
            if (d0 > 0.01) {
                this.hasImpulse = true;
            }
        }

        if (this.getItem().isEmpty() && !this.isRemoved()) {
            this.discard();
        }
    }
}
