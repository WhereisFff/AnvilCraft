package dev.dubhe.anvilcraft.item.amulet;

import dev.dubhe.anvilcraft.init.ModDataAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CogwheelAmuletItem extends AbstractAmuletItem {
    public CogwheelAmuletItem(Properties properties) {
        super(properties);
    }

    @Override
    void UpdateAccessory(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player) {
            player.setData(ModDataAttachments.CREATE_MASTER, true);
        }
    }
}
