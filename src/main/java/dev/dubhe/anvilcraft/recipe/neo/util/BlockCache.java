package dev.dubhe.anvilcraft.recipe.neo.util;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

public class BlockCache {
    public static final InWorldRecipeData<BlockCache> BLOCK_CACHE = InWorldRecipeData.of(AnvilCraft.of("block_cache"), BlockCache::of);
    public static final Consumer<InWorldRecipeContext> DEFAULT_ACCEPTOR = (ctx) -> ctx.get(BlockCache.BLOCK_CACHE).accept();
    private final HashMap<BlockPos, BlockState> simulated = new HashMap<>();
    private final HashMap<BlockPos, BlockEntity> simulatedEntity = new HashMap<>();
    private final HashMap<BlockPos, BlockState> cache = new HashMap<>();
    private final HashMap<BlockPos, BlockEntity> cacheEntity = new HashMap<>();
    private final LevelAccessor level;

    public BlockCache(LevelAccessor level) {
        this.level = level;
    }

    private static @NotNull BlockCache of(@NotNull InWorldRecipeContext level, InWorldRecipeData<BlockCache> key) {
        return new BlockCache(level.getLevel());
    }

    public BlockState getBlockState(@NotNull BlockPos pos) {
        cache.computeIfAbsent(pos, level::getBlockState);
        cacheEntity.computeIfAbsent(pos, level::getBlockEntity);
        simulatedEntity.computeIfAbsent(pos, level::getBlockEntity);
        return simulated.computeIfAbsent(pos, level::getBlockState);
    }

    public BlockEntity getBlockEntity(BlockPos pos) {
        cache.computeIfAbsent(pos, level::getBlockState);
        cacheEntity.computeIfAbsent(pos, level::getBlockEntity);
        simulated.computeIfAbsent(pos, level::getBlockState);
        return simulatedEntity.computeIfAbsent(pos, level::getBlockEntity);
    }

    public void setBlock(@NotNull BlockPos pos, BlockState state) {
        if (state == null) state = Blocks.AIR.defaultBlockState();
        this.getBlockState(pos);
        simulated.put(pos, state);
        simulatedEntity.put(pos, null);
    }

    public void setBlock(@NotNull BlockPos pos, Block block) {
        if (block == null) block = Blocks.AIR;
        this.setBlock(pos, block.defaultBlockState());
    }

    public void removeBlock(@NotNull BlockPos pos) {
        this.setBlock(pos, Blocks.AIR);
    }

    public void accept() {
        simulated.forEach((pos, state) -> {
            BlockState old = cache.get(pos);
            if (old == null) throw new IllegalStateException("Block at " + pos + " was not found in the cache!");
            if (old.equals(state)) return;
            level.setBlock(pos, state, 3);
            cache.put(pos, state);
        });
    }
}
