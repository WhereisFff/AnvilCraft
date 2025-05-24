package dev.dubhe.anvilcraft.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class HeavyHalberdItem extends ProjectileWeaponItem {
    private final Tier tier;

    public HeavyHalberdItem(Tier tier, Properties properties) {
        super(properties.durability(tier.getUses()));
        this.tier = tier;
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return null;
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0;
    }

    @Override
    protected void shootProjectile(
        LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle,
        @Nullable LivingEntity target
    ) {

    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
    }

    protected boolean canUse(ItemStack stack) {
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
        BowItem
    }
}
