package dev.dubhe.anvilcraft.recipe.neo;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InWorldRecipeContext implements RecipeInput {
    @Getter
    private final Level level;
    @Getter
    private final Vec3 pos;
    @Getter
    private final Entity entity;
    private final Map<ResourceLocation, Object> data = Collections.synchronizedMap(new HashMap<>());
    @Getter
    private final List<IRecipePredicate<?>> stack = Collections.synchronizedList(new LinkedList<>());

    public InWorldRecipeContext(Level level, Vec3 pos, Entity entity) {
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
        return value != null ? value : key.defaultValue();
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
}
