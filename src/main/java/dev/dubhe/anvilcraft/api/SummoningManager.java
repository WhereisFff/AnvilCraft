package dev.dubhe.anvilcraft.api;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.InductionLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = AnvilCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SummoningManager {
    private static final Map<Level, SummoningManager> INSTANCES = new HashMap<>();

    private final Set<BlockPos> lightBlocks = Collections.synchronizedSet(new HashSet<>());

    /**
     * 获取当前维度实体生成实例
     */
    public static SummoningManager getInstance(Level level) {
        if (!INSTANCES.containsKey(level)) {
            INSTANCES.put(level, new SummoningManager());
        }
        return INSTANCES.get(level);
    }

    public SummoningManager() {
    }

    /**
     *
     */
    public static void addLightBlock(BlockPos pos, Level level) {
        SummoningManager inst = SummoningManager.getInstance(level);
        inst.lightBlocks.add(pos);
    }

    @SubscribeEvent
    private static void blockEntitySummon(@NotNull EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        SummoningManager instance = getInstance(level);

        Iterator<BlockPos> it = instance.lightBlocks.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            BlockState lightBlockState = level.getBlockState(pos);
            if (
                lightBlockState.getBlock() instanceof InductionLightBlock
                && InductionLightBlock.isLit(lightBlockState)
                && (InductionLightBlock.canBlockMobSummoning(lightBlockState)
                    || InductionLightBlock.canBlockAnimalSummoning(lightBlockState))
            ) {
                AABB blockedAABB = centerSectionTo3x3x3(SectionPos.of(pos));

                if (blockedAABB.contains(entity.position())
                    && ((InductionLightBlock.canBlockMobSummoning(lightBlockState) && entity instanceof Monster)
                        || (InductionLightBlock.canBlockAnimalSummoning(lightBlockState) && entity instanceof Animal))
                ) {
                    event.setCanceled(true);
                }
            } else {
                it.remove();
            }
        }
    }

    private static AABB centerSectionTo3x3x3(SectionPos center) {
        return new AABB(
            center.minBlockX() - 16,
            center.minBlockY() - 16,
            center.minBlockZ() - 16,
            center.maxBlockX() + 16 + 1,
            center.maxBlockY() + 16 + 1,
            center.maxBlockZ() + 16 + 1
        );
    }
}
