package dev.dubhe.anvilcraft.recipe.neo;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.init.ModRegistries;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.List;

public class InWorldRecipe implements Recipe<InWorldRecipeContext>, Comparable<InWorldRecipe>, IPrioritized {
    private final NonNullList<ItemStack> icon = NonNullList.withSize(1, new ItemStack(Items.ANVIL));
    @Getter
    private final RecipeTrigger trigger;
    @Getter
    @Immutable
    private final List<RecipePredicate<?>> predicates;
    @Getter
    @Immutable
    private final List<RecipeOutcome<?>> outcomes;

    public InWorldRecipe(@Nullable ItemStack icon, @NotNull RecipeTrigger trigger, @Nullable List<RecipePredicate<?>> predicates, @Nullable List<RecipeOutcome<?>> outcomes) {
        if (icon != null) this.icon.set(0, icon);
        this.trigger = trigger;
        ImmutableList.Builder<RecipePredicate<?>> predicateBuilder = ImmutableList.builder();
        if (predicates != null) for (RecipePredicate<?> predicate : predicates) {
            predicateBuilder.add(predicate);
        }
        ImmutableList.Builder<RecipeOutcome<?>> outcomeBuilder = ImmutableList.builder();
        if (outcomes != null) for (RecipeOutcome<?> outcome : outcomes) {
            outcomeBuilder.add(outcome);
        }
        this.predicates = predicateBuilder.build();
        this.outcomes = outcomeBuilder.build();
    }

    @Override
    public boolean matches(@NotNull InWorldRecipeContext inWorldRecipeContext, @NotNull Level level) {
        int count = 0;
        for (RecipePredicate<?> predicate : predicates) {
            if (predicate.test(inWorldRecipeContext)) count++;
        }
        if (count != predicates.size()) return false;
        for (RecipeOutcome<?> outcome : outcomes) {
            outcome.accept(inWorldRecipeContext);
        }
        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull InWorldRecipeContext inWorldRecipeContext, HolderLookup.@NotNull Provider provider) {
        if (!this.matches(inWorldRecipeContext, inWorldRecipeContext.getLevel())) return ItemStack.EMPTY;
        for (RecipeOutcome<?> outcome : outcomes) {
            outcome.accept(inWorldRecipeContext);
        }
        return this.icon.getFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    public ItemStack getIcon() {
        return this.icon.getFirst().copy();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return this.icon.getFirst().copy();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.IN_WORLD_RECIPE.get();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.IN_WORLD_RECIPE_SERIALIZER.get();
    }

    @Override
    public int getPriority() {
        int priority = 0;
        for (RecipePredicate<?> predicate : predicates) {
            priority += predicate.getPriority();
        }
        for (RecipeOutcome<?> outcome : outcomes) {
            priority += outcome.getPriority();
        }
        return priority;
    }

    @Override
    public int compareTo(@NotNull InWorldRecipe o) {
        if (this == o) return 0;
        if (this.getPriority() <= o.getPriority()) return 1;
        else return -1;
    }

    public static class Serializer implements RecipeSerializer<InWorldRecipe> {
        private static final MapCodec<InWorldRecipe> MAP_CODEC = null;
        private static final StreamCodec<RegistryFriendlyByteBuf, InWorldRecipe> STREAM_CODEC =
            StreamCodec.of(InWorldRecipe.Serializer::encode, InWorldRecipe.Serializer::decode);

        @Override
        public @NotNull MapCodec<InWorldRecipe> codec() {
            return InWorldRecipe.Serializer.MAP_CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, InWorldRecipe> streamCodec() {
            return InWorldRecipe.Serializer.STREAM_CODEC;
        }

        private static @NotNull InWorldRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
            ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
            RecipeTrigger trigger = ModRegistries.BuiltIn.RECIPE_TRIGGER.get(buf.readResourceLocation());
            if (trigger == null) throw new IllegalArgumentException("Unknown recipe trigger: " + buf.readResourceLocation());
            int size = buf.readVarInt();
            List<RecipePredicate<?>> predicates = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                RecipePredicateType<?> type = ModRegistries.BuiltIn.RECIPE_PREDICATE.get(buf.readResourceLocation());
                if (type != null) predicates.add(type.streamCodec().decode(buf));
                else throw new IllegalArgumentException("Unknown recipe predicate: " + buf.readResourceLocation());
            }
            size = buf.readVarInt();
            List<RecipeOutcome<?>> outcomes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                RecipeOutcomeType<?> type = ModRegistries.BuiltIn.RECIPE_OUTCOME.get(buf.readResourceLocation());
                if (type != null) outcomes.add(type.streamCodec().decode(buf));
                else throw new IllegalArgumentException("Unknown recipe outcome: " + buf.readResourceLocation());
            }
            return new InWorldRecipe(icon, trigger, predicates, outcomes);
        }

        private static <P extends RecipePredicate<P>, O extends RecipeOutcome<O>> void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull InWorldRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buf, recipe.icon.getFirst());
            buf.writeResourceLocation(recipe.trigger.getId());
            buf.writeVarInt(recipe.predicates.size());
            for (RecipePredicate<?> predicate : recipe.predicates) {
                buf.writeResourceLocation(predicate.getType().getId());
                //noinspection unchecked
                ((RecipePredicate<P>) predicate).getType().streamCodec().encode(buf, (P) predicate);
            }
            buf.writeVarInt(recipe.outcomes.size());
            for (RecipeOutcome<?> outcome : recipe.outcomes) {
                buf.writeResourceLocation(outcome.getType().getId());
                //noinspection unchecked
                ((RecipeOutcome<O>) outcome).getType().streamCodec().encode(buf, (O) outcome);
            }
        }
    }
}
