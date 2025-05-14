package dev.dubhe.anvilcraft.item.amulet;

import dev.dubhe.anvilcraft.api.amulet.AmuletManager;
import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import dev.dubhe.anvilcraft.init.ModDataAttachments;
import dev.dubhe.anvilcraft.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static dev.dubhe.anvilcraft.init.ModDataAttachments.AMULET_COUNT;
import static dev.dubhe.anvilcraft.init.ModDataAttachments.AMULET_MAX;
import static dev.dubhe.anvilcraft.init.ModDataAttachments.DISCOUNT_RATE;
import static dev.dubhe.anvilcraft.init.ModDataAttachments.SCARE_ENTITIES;

public abstract class AbstractAmuletItem extends Item {

    public AbstractAmuletItem(Properties properties) {
        super(properties);
    }

    abstract void updateAccessory(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected);

    public abstract Holder<AmuletType> getType();

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(stack.getItem() instanceof AbstractAmuletItem)) return;
        if (!(entity instanceof Player)) return;
        if (entity.getData(AMULET_COUNT) < entity.getData(AMULET_MAX)) {
            updateAccessory(stack, level, entity, slotId, isSelected);
            entity.setData(AMULET_COUNT, entity.getData(AMULET_COUNT) + 1);
        }
    }

    public static void resetWorkingAmuletData(@NotNull LivingEntity entity) {
        if (entity.hasData(AMULET_COUNT)) {
            entity.setData(AMULET_COUNT, 0);
        }
        if (entity instanceof Player player) {
            if (!AmuletManager.INSTANCE.hasAmuletInInventory(player, ModItems.EMERALD_AMULET) && entity.hasData(DISCOUNT_RATE)) {
                entity.setData(DISCOUNT_RATE, 0f);
            }
            if (entity.hasData(SCARE_ENTITIES)) {
                CompoundTag root = entity.getData(ModDataAttachments.SCARE_ENTITIES);
                if (!AmuletManager.INSTANCE.hasAmuletInInventory(player, ModItems.DOG_AMULET)) {
                    root.putBoolean("skeletons", false);
                } else if (!AmuletManager.INSTANCE.hasAmuletInInventory(player, ModItems.CAT_AMULET)) {
                    root.putBoolean("creepers", false);
                    root.putBoolean("phantoms", false);
                }
            }
        }
    }
}
