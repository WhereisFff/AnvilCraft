package dev.dubhe.anvilcraft.item.amulet;

import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import dev.dubhe.anvilcraft.init.ModAmuletTypes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AnvilAmuletItem extends AbstractAmuletItem {
    public AnvilAmuletItem(Properties properties) {
        super(properties);
    }

    @Override
    void updateAccessory(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
    }

    @Override
    public Holder<AmuletType> getType() {
        return ModAmuletTypes.ANVIL.getDelegate();
    }
}
