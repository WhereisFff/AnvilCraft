package dev.dubhe.anvilcraft.recipe.neo;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InWorldRecipeContext implements RecipeInput {
    @Getter
    private final ServerLevel level;
    @Getter
    private final Vec3 pos;
    @Getter
    private final Entity entity;
    private final Map<ResourceLocation, Object> data = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Consumer<InWorldRecipeContext>> acceptors = new ConcurrentHashMap<>();
    @Getter
    private final List<IRecipePredicate<?>> stack = Collections.synchronizedList(new LinkedList<>());

    public InWorldRecipeContext(ServerLevel level, Vec3 pos, Entity entity) {
        this.level = level;
        this.pos = pos;
        this.entity = entity;
    }

    public void push(@NotNull IRecipePredicate<?> predicate) {
        this.stack.add(predicate);
        predicate.snapshot(this);
    }

    public void pop(@NotNull IRecipePredicate<?> predicate) {
        predicate.rollback(this);
        this.stack.removeLast();
    }

    public <T> void put(@NotNull InWorldRecipeData<T> key, T value) {
        this.data.put(key.location(), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull InWorldRecipeData<T> key) {
        T value = (T) this.data.get(key.location());
        return value != null ? value : key.supplier().apply(this, key);
    }

    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(@NotNull InWorldRecipeData<T> key) {
        return (T) this.data.computeIfAbsent(key.location(), k -> key.supplier().apply(this, key));
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    public void putAcceptor(ResourceLocation key, @NotNull Consumer<InWorldRecipeContext> acceptor) {
        this.acceptors.put(key, acceptor);
    }

    public void accept() {
        this.acceptors.values().forEach(acceptor -> acceptor.accept(this));
    }

    public @NotNull LootContext emptyLootContext() {
        return new LootContext.Builder(new LootParams(this.level, Map.of(), Map.of(), 0)).create(Optional.empty());
    }

    public float getFloat(@NotNull NumberProvider provider, float min, float max) {
        return Math.clamp(provider.getFloat(this.emptyLootContext()), min, max);
    }

    public float getFloat(@NotNull NumberProvider provider, float max) {
        return Math.min(provider.getFloat(this.emptyLootContext()), max);
    }

    public float getFloat(@NotNull NumberProvider provider) {
        return provider.getFloat(this.emptyLootContext());
    }

    public int getInt(@NotNull NumberProvider provider, int min, int max) {
        return Math.clamp(provider.getInt(this.emptyLootContext()), min, max);
    }

    public int getInt(@NotNull NumberProvider provider, int max) {
        return Math.min(provider.getInt(this.emptyLootContext()), max);
    }

    public int getInt(@NotNull NumberProvider provider) {
        return provider.getInt(this.emptyLootContext());
    }
}
