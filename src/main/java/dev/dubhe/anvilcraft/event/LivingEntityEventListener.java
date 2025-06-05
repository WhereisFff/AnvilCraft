package dev.dubhe.anvilcraft.event;

import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import static dev.dubhe.anvilcraft.init.ModDataAttachments.SCARE_ENTITIES;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID)
public class LivingEntityEventListener {

    @SubscribeEvent
    public static void onSkeletonChangeTarget(LivingChangeTargetEvent event) {
        if (
            event.getEntity() instanceof AbstractSkeleton
                && event.getNewAboutToBeSetTarget() instanceof Player player && player.getData(SCARE_ENTITIES).getBoolean("skeletons")
        ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSkeletonTick(EntityTickEvent.Post event) {
        if (
            event.getEntity() instanceof AbstractSkeleton skeleton
                && skeleton.getTarget() instanceof Player player && player.getData(SCARE_ENTITIES).getBoolean("skeletons")
        ) {
            skeleton.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Warden
            && event.getSource().typeHolder().is(DamageTypes.SONIC_BOOM)
        ) {
            LivingEntity entity = event.getEntity();
            Level level = entity.level();
            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), Items.ECHO_SHARD.getDefaultInstance()));
        }
    }
}
