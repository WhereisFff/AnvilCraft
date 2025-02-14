package dev.dubhe.anvilcraft.recipe.neo;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InWorldRecipeContext implements RecipeInput {
    @Getter
    private final MinecraftServer server;
    @Getter
    private final ServerLevel level;
    @Getter
    private final Entity triggerEntity;
    @Getter
    private final Vec3 triggerPos;
    private final Map<ResourceLocation, Object> data = new HashMap<>();
    @Getter
    private final List<RecipePredicate<?>> passed = new ArrayList<>();

    private InWorldRecipeContext(MinecraftServer server, ServerLevel level, Entity triggerEntity, Vec3 triggerPos) {
        this.server = server;
        this.level = level;
        this.triggerEntity = triggerEntity;
        this.triggerPos = triggerPos;
    }

    public static @NotNull InWorldRecipeContext of(MinecraftServer server, ServerLevel level, Entity triggerEntity, Vec3 triggerPos) {
        return new InWorldRecipeContext(server, level, triggerEntity, triggerPos);
    }

    public static @NotNull InWorldRecipeContext of(MinecraftServer server, ServerLevel level, Entity triggerEntity) {
        return InWorldRecipeContext.of(server, level, triggerEntity, triggerEntity.position());
    }

    public <T> void setData(ResourceLocation location, T data) {
        this.data.put(location, data);
    }

    public <T> T getData(ResourceLocation location, @SuppressWarnings("unused") Class<T> typeOfT) {
        //noinspection unchecked
        return (T) this.data.get(location);
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return ItemStack.EMPTY;
    }

    public void pass(RecipePredicate<?> predicate) {
        this.passed.add(predicate);
        predicate.push(this);
    }

    public void pop() {
        this.passed.removeLast().pop(this);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    public void reset() {
    }
}
