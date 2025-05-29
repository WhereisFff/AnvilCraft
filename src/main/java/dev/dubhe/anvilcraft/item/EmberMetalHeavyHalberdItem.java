package dev.dubhe.anvilcraft.item;

import com.mojang.datafixers.util.Unit;
import dev.dubhe.anvilcraft.entity.ThrownEmberMetalHeavyHalberdEntity;
import dev.dubhe.anvilcraft.entity.ThrownHeavyHalberdEntity;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EmberMetalHeavyHalberdItem extends HeavyHalberdItem {
    public EmberMetalHeavyHalberdItem(Properties properties) {
        super(
            ModTiers.EMBER_METAL,
            properties.fireResistant()
                .attributes(HeavyHalberdItem.createAttributes(ModTiers.EMBER_METAL, 10, -2.4f))
                .component(ModComponents.FIRE_REFORGING, Unit.INSTANCE)
        );
    }

    @Override
    protected double getAttackDamage() {
        return 10;
    }

    @Override
    public ThrownHeavyHalberdEntity createThrown(Level level, LivingEntity shooter, ItemStack pickupItemStack) {
        return new ThrownEmberMetalHeavyHalberdEntity(level, shooter, pickupItemStack);
    }

    @Override
    public ThrownHeavyHalberdEntity createThrown(Level level, double x, double y, double z, ItemStack pickupItemStack) {
        return new ThrownEmberMetalHeavyHalberdEntity(level, x, y, z, pickupItemStack);
    }
}
