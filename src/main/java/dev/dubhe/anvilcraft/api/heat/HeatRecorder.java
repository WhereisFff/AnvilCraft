package dev.dubhe.anvilcraft.api.heat;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.heatable.GlowingBlock;
import dev.dubhe.anvilcraft.block.heatable.HeatedBlock;
import dev.dubhe.anvilcraft.block.heatable.IncandescentBlock;
import dev.dubhe.anvilcraft.block.heatable.RedhotBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public class HeatRecorder {
    public static final Map<Block, ResourceLocation> BLOCK_TO_ID = new HashMap<>();
    private static final Map<ResourceLocation, NavigableMap<HeatTier, Block>> HEATABLE_BLOCKS = new HashMap<>();
    static final Set<HeatProducerInfo<?>> PRODUCER_INFOS = new HashSet<>();

    public static RegisterHelper registerHeatables(ResourceLocation id) {
        return new RegisterHelper(id);
    }

    public static <T> HeatProducerInfo<T> registerProducerInfo(HeatProducerInfo<T> info) {
        PRODUCER_INFOS.add(info);
        return info;
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class RegisterHelper {
        private final ResourceLocation id;

        public RegisterHelper(ResourceLocation id) {
            this.id = id;
        }

        public RegisterHelper normal(Block normal) {
            return this.customTier(HeatTier.NORMAL, normal);
        }

        public RegisterHelper normal(Supplier<? extends Block> normal) {
            return this.customTier(HeatTier.NORMAL, normal.get());
        }

        public RegisterHelper heated(HeatedBlock heated) {
            return this.customTier(HeatTier.HEATED, heated);
        }

        public RegisterHelper heated(Supplier<? extends HeatedBlock> heated) {
            return this.customTier(HeatTier.HEATED, heated.get());
        }

        public RegisterHelper redhot(RedhotBlock redhot) {
            return this.customTier(HeatTier.REDHOT, redhot);
        }

        public RegisterHelper redhot(Supplier<? extends RedhotBlock> redhot) {
            return this.customTier(HeatTier.REDHOT, redhot.get());
        }

        public RegisterHelper glowing(GlowingBlock glowing) {
            return this.customTier(HeatTier.GLOWING, glowing);
        }

        public RegisterHelper glowing(Supplier<? extends GlowingBlock> glowing) {
            return this.customTier(HeatTier.GLOWING, glowing.get());
        }

        public RegisterHelper incandescent(IncandescentBlock incandescent) {
            return this.customTier(HeatTier.INCANDESCENT, incandescent);
        }

        public RegisterHelper incandescent(Supplier<? extends IncandescentBlock> incandescent) {
            return this.customTier(HeatTier.INCANDESCENT, incandescent.get());
        }

        public RegisterHelper customTier(HeatTier tier, Block heatable) {
            BLOCK_TO_ID.put(heatable, this.id);

            if (HEATABLE_BLOCKS.containsKey(this.id)) {
                HEATABLE_BLOCKS.get(this.id).put(tier, heatable);
            } else {
                NavigableMap<HeatTier, Block> tierBlockMap = Collections.synchronizedNavigableMap(new TreeMap<>());
                tierBlockMap.put(tier, heatable);
                HEATABLE_BLOCKS.put(this.id, tierBlockMap);
            }

            return this;
        }
    }

    public static Optional<Block> getHeatableBlock(ResourceLocation id, HeatTier tier) {
        return Optional.ofNullable(HEATABLE_BLOCKS.get(id).get(tier));
    }

    public static Optional<Block> getHeatableBlock(Block prevTier, HeatTier tier) {
        ResourceLocation id = BLOCK_TO_ID.get(prevTier);
        if (id == null) return Optional.empty();
        for (Map.Entry<HeatTier, Block> entry : HEATABLE_BLOCKS.get(id).entrySet()) {
            if (entry.getKey().equals(tier)) return Optional.empty();
            if (entry.getValue().equals(prevTier)) return Optional.ofNullable(HEATABLE_BLOCKS.get(id).get(tier));
        }
        return Optional.empty();
    }

    public static Optional<Block> getHeatableBlockPrevTier(Block value) {
        ResourceLocation id = BLOCK_TO_ID.get(value);
        if (id == null) return Optional.empty();
        Block prevTier = null;
        for (Map.Entry<HeatTier, Block> entry : HEATABLE_BLOCKS.get(id).entrySet()) {
            if (entry.getValue().equals(value)) return Optional.ofNullable(prevTier);
            prevTier = entry.getValue();
        }
        return Optional.empty();
    }

    public static Optional<HeatTier> getPrevTier(Block value) {
        ResourceLocation id = BLOCK_TO_ID.get(value);
        if (id == null) return Optional.empty();
        HeatTier prevTier = null;
        for (Map.Entry<HeatTier, Block> entry : HEATABLE_BLOCKS.get(id).entrySet()) {
            if (entry.getValue().equals(value)) return Optional.ofNullable(prevTier);
            prevTier = entry.getKey();
        }
        return Optional.empty();
    }

    public static Optional<HeatTier> getCurrentTier(Block value) {
        ResourceLocation id = BLOCK_TO_ID.get(value);
        if (id == null) return Optional.empty();
        for (Map.Entry<HeatTier, Block> entry : HEATABLE_BLOCKS.get(id).entrySet()) {
            if (entry.getValue().equals(value)) return Optional.of(entry.getKey());
        }
        return Optional.empty();
    }

    public static Optional<Block> getHeatableBlockNextTier(Block value) {
        ResourceLocation id = BLOCK_TO_ID.get(value);
        if (id == null) return Optional.empty();
        boolean isNextReturn = false;
        for (Map.Entry<HeatTier, Block> entry : HEATABLE_BLOCKS.get(id).entrySet()) {
            if (entry.getValue().equals(value)) {
                isNextReturn = true;
                continue;
            }
            if (isNextReturn) return Optional.of(entry.getValue());
        }
        return Optional.empty();
    }

    static {
        registerHeatables(AnvilCraft.of("netherite"))
            .normal(Blocks.NETHERITE_BLOCK)
            .heated(ModBlocks.HEATED_NETHERITE)
            .redhot(ModBlocks.REDHOT_NETHERITE)
            .glowing(ModBlocks.GLOWING_NETHERITE)
            .incandescent(ModBlocks.INCANDESCENT_NETHERITE);
        registerHeatables(AnvilCraft.of("tungsten"))
            .normal(ModBlocks.TUNGSTEN_BLOCK)
            .heated(ModBlocks.HEATED_TUNGSTEN)
            .redhot(ModBlocks.REDHOT_TUNGSTEN)
            .glowing(ModBlocks.GLOWING_TUNGSTEN)
            .incandescent(ModBlocks.INCANDESCENT_TUNGSTEN);
    }
}
