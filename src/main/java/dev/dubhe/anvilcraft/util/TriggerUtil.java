package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.init.ModCriterionTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class TriggerUtil {
    public static void placerPlaceBlock(Level level, BlockPos pos, Block block) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.PLACER_PLACE_BLOCK.get().trigger(player, block);
            }
        }
    }

    public static void devourerDevourBlock(Level level, BlockPos pos, Block block) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.DEVOURER_DEVOUR_BLOCK.get().trigger(player, block);
            }
        }
    }

    public static void anvilLooting(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.ANVIL_LOOTING.get().trigger(player);
            }
        }
    }

    public static void anvilLootingIronGolem(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.ANVIL_LOOTING_IRON_GOLEM.get().trigger(player);
            }
        }
    }

    public static void liftingAnvil(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.LIFTING_ANVIL.get().trigger(player);
            }
        }
    }

    public static void anvilOnGround(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.ANVIL_ON_GROUND.get().trigger(player);
            }
        }
    }

    public static void anythingAnvilCrafting(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.ANYTHING_ANVIL_CRAFTING.get().trigger(player);
            }
        }
    }

    public static void blockCrushing(Level level, BlockPos pos, Item input, Item output) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.BLOCK_CRUSHING.get().trigger(player, input, output);
            }
        }
    }

    public static void mesh(Level level, BlockPos pos, Item input, Item output) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.MESH.get().trigger(player, input, output);
            }
        }
    }

    public static void squeezing(Level level, BlockPos pos, Item input, Item output) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.SQUEEZING.get().trigger(player, input, output);
            }
        }
    }

    public static void blockCompressing(Level level, BlockPos pos, Item input, Item output) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 5)) {
                ModCriterionTriggers.BLOCK_COMPRESSING.get().trigger(player, input, output);
            }
        }
    }
}
