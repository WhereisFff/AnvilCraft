package dev.dubhe.anvilcraft.item.amulet;

import dev.dubhe.anvilcraft.init.ModDataAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DogAmuletItem extends AbstractAmuletItem {
    public DogAmuletItem(Properties properties) {
        super(properties);
    }

    @Override
    void updateAccessory(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player) {
            CompoundTag root = player.getData(ModDataAttachments.SCARE_ENTITIES);
            root.putBoolean("skeletons", true);
        }
    }
}
