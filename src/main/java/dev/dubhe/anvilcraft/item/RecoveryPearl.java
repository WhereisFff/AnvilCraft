package dev.dubhe.anvilcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;

import java.util.Optional;

public class RecoveryPearl extends Item {
    public RecoveryPearl(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemInHand = player.getItemInHand(usedHand);
        Optional<GlobalPos> lastDeathLocation = player.getLastDeathLocation();
        ResourceKey<Level> currentDimension = level.dimension();
        BlockPos currentPlayerPos = player.getOnPos();
        BlockPos spawnPos = player.getSleepingPos().isPresent() ? player.getSleepingPos().get() : level.getSharedSpawnPos();

        if (!level.isClientSide) {
            if (lastDeathLocation.isPresent()) {
                BlockPos lastDeathPos = lastDeathLocation.get().pos();
                ResourceKey<Level> lastDeathDimension = lastDeathLocation.get().dimension();
                if (currentDimension == lastDeathDimension) {
                    if (getDistance(currentPlayerPos, lastDeathPos) < 12) {
                        player.teleportTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                    } else {
                        player.teleportTo(lastDeathPos.getX(), lastDeathPos.getY(), lastDeathPos.getZ());
                    }
                } else {
                    crossDimensionTeleportTo(lastDeathDimension, player, lastDeathPos);
                }
            } else {
                if (currentDimension == Level.OVERWORLD) {
                    player.teleportTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                } else {
                    crossDimensionTeleportTo(Level.OVERWORLD, player, spawnPos);
                }
            }

            player.hurt(level.damageSources().fall(), 4);
        }

        itemInHand.consume(1, player);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }

    private double getDistance(BlockPos pos1, BlockPos pos2) {
        return Math.sqrt(pos1.distToLowCornerSqr(pos2.getX(), pos2.getY(), pos2.getZ()));
    }

    public static void crossDimensionTeleportTo(ResourceKey<Level> dimension, Player player, BlockPos pos) {
        Level level = player.level();
        MinecraftServer server = level.getServer();
        if (server != null) {
            ServerLevel serverLevel = server.getLevel(dimension);
            if (serverLevel != null) {
                player.changeDimension(new DimensionTransition(serverLevel, player, DimensionTransition.PLACE_PORTAL_TICKET));
                player.teleportTo(pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }
}
