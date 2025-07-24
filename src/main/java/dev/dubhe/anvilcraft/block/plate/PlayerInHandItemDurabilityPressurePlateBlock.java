package dev.dubhe.anvilcraft.block.plate;

import com.google.common.collect.ImmutableSet;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.AABB;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerInHandItemDurabilityPressurePlateBlock extends PowerLevelPressurePlateBlock {
    public PlayerInHandItemDurabilityPressurePlateBlock(Properties properties) {
        super(BlockSetType.IRON, properties);
    }

    @Override
    protected Set<Class<? extends Entity>> getEntityClasses() {
        return ImmutableSet.of(Player.class);
    }

    @Override
    protected int getSignalStrength(Level level, AABB box, Set<Class<? extends Entity>> entityClasses) {
        return (int) Math.clamp(getRemainDurability(level, box) * 15, 0, 15);
    }

    protected static float getRemainDurability(Level level, AABB box) {
        float result = Float.MIN_VALUE;

        for (Player player : level.getEntitiesOfClass(
            Player.class, box,
            EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers())
        )) {
            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemStack.isEmpty()) {
                result = 0;
                continue;
            }
            Integer maxDamage = itemStack.get(DataComponents.MAX_DAMAGE);
            Integer damage = itemStack.get(DataComponents.DAMAGE);
            if (maxDamage != null) {
                if (damage == null) {
                    damage = maxDamage;
                }
                int remainDamage = maxDamage - damage;
                result = (float) remainDamage / maxDamage;
            } else {
                result = Float.MAX_VALUE;
            }
        }

        return result;
    }
}
