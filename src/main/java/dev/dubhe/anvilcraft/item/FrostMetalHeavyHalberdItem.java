package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.entity.ThrownFrostMetalHeavyHalberdEntity;
import dev.dubhe.anvilcraft.entity.ThrownHeavyHalberdEntity;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    protected double getBaseAttackDamage() {
        return 13;
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
