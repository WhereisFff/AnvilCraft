package dev.dubhe.anvilcraft.recipe.neo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.init.ModRegistries;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class InWorldRecipe implements Recipe<InWorldRecipeContext>, IPrioritized {
    @Unmodifiable
    private final ItemStack icon;
    private final IRecipeTrigger trigger;
    @Unmodifiable
    private final List<IRecipePredicate<?>> conflicting;
    @Unmodifiable
    private final List<IRecipePredicate<?>> nonConflicting;
    @Unmodifiable
    private final List<IRecipeOutcome<?>> outcomes;
    private final int priority;
    private final boolean compatible;

    public InWorldRecipe(
        @NotNull ItemStack icon,
        IRecipeTrigger trigger,
        @Unmodifiable List<IRecipePredicate<?>> conflicting,
        @Unmodifiable List<IRecipePredicate<?>> nonConflicting,
        @Unmodifiable List<IRecipeOutcome<?>> outcomes,
        int priority,
        boolean compatible
    ) {
        this.icon = icon;
        this.trigger = trigger;
        this.conflicting = conflicting;
        this.nonConflicting = nonConflicting;
        this.outcomes = outcomes;
        this.priority = priority;
        this.compatible = compatible;
    }

    @Override
    public boolean matches(@NotNull InWorldRecipeContext context, @NotNull Level level) {
        boolean nonConflicting = ShapelessMatcher.compatible(this.nonConflicting, context);
        if (!nonConflicting) {
            return false;
        }
        if (this.compatible) {
            return ShapelessMatcher.compatible(this.conflicting, context);
        } else {
            return ShapelessMatcher.incompatible(this.conflicting, context);
        }
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull InWorldRecipeContext context, @NotNull HolderLookup.Provider provider) {
        List<IRecipePredicate<?>> stack = context.getStack();
        IRecipePredicate<?> predicate;
        while (!stack.isEmpty()) {
            predicate = stack.removeFirst();
            predicate.accept(context);
        }
        for (IRecipeOutcome<?> outcome : this.outcomes) {
            outcome.accept(context);
        }
        return this.icon.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return this.icon.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.IN_WORLD_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.IN_WORLD_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<InWorldRecipe> {
        private static final Codec<IRecipePredicate<?>> PREDICATE_CODEC = ModRegistries.PREDICATE_TYPE_REGISTRY
            .byNameCodec()
            .dispatch(IRecipePredicate::getType, IRecipePredicate.Type::codec);
        private static final Codec<IRecipeOutcome<?>> OUTCOME_CODEC = ModRegistries.OUTCOME_TYPE_REGISTRY
            .byNameCodec()
            .dispatch(IRecipeOutcome::getType, IRecipeOutcome.Type::codec);
        private static final MapCodec<InWorldRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ItemStack.CODEC
                    .fieldOf("icon")
                    .orElse(Items.ANVIL.getDefaultInstance())
                    .forGetter(InWorldRecipe::getIcon),
                ModRegistries.TRIGGER_REGISTRY
                    .byNameCodec()
                    .fieldOf("trigger")
                    .forGetter(InWorldRecipe::getTrigger),
                PREDICATE_CODEC
                    .listOf()
                    .fieldOf("conflicting")
                    .forGetter(InWorldRecipe::getConflicting),
                PREDICATE_CODEC
                    .listOf()
                    .fieldOf("non_conflicting")
                    .forGetter(InWorldRecipe::getNonConflicting),
                OUTCOME_CODEC
                    .listOf()
                    .fieldOf("outcomes")
                    .forGetter(InWorldRecipe::getOutcomes),
                Codec.INT
                    .fieldOf("priority")
                    .orElse(1)
                    .forGetter(InWorldRecipe::getPriority),
                Codec.BOOL
                    .fieldOf("compatible")
                    .orElse(true)
                    .forGetter(InWorldRecipe::isCompatible)
            ).apply(instance, InWorldRecipe::new)
        );

        @Override
        public @NotNull MapCodec<InWorldRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, InWorldRecipe> streamCodec() {
            return StreamCodec.of(Serializer::encode, Serializer::decode);
        }

        @SuppressWarnings("unchecked")
        private static <P extends IRecipePredicate<P>, O extends IRecipeOutcome<O>> void encode(
            RegistryFriendlyByteBuf buf, @NotNull InWorldRecipe recipe
        ) {
            ItemStack.STREAM_CODEC.encode(buf, recipe.icon);
            buf.writeResourceLocation(recipe.trigger.getId());
            buf.writeVarInt(recipe.conflicting.size());
            for (IRecipePredicate<?> predicate : recipe.conflicting) {
                buf.writeResourceLocation(predicate.getType().getId());
                ((P) predicate).getType().streamCodec().encode(buf, (P) predicate);
            }
            buf.writeVarInt(recipe.nonConflicting.size());
            for (IRecipePredicate<?> predicate : recipe.nonConflicting) {
                buf.writeResourceLocation(predicate.getType().getId());
                ((P) predicate).getType().streamCodec().encode(buf, (P) predicate);
            }
            buf.writeVarInt(recipe.outcomes.size());
            for (IRecipeOutcome<?> outcome : recipe.outcomes) {
                buf.writeResourceLocation(outcome.getType().getId());
                ((O) outcome).getType().streamCodec().encode(buf, (O) outcome);
            }
            buf.writeInt(recipe.priority);
            buf.writeBoolean(recipe.compatible);
        }

        private static @NotNull InWorldRecipe decode(RegistryFriendlyByteBuf buf) {
            ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
            IRecipeTrigger trigger = ModRegistries.TRIGGER_REGISTRY.get(buf.readResourceLocation());
            List<IRecipePredicate<?>> conflicting = decodeRecipePredicateList(buf);
            List<IRecipePredicate<?>> nonConflicting = decodeRecipePredicateList(buf);
            List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
            int outcomesSize = buf.readVarInt();
            for (int i = 0; i < outcomesSize; i++) {
                ResourceLocation location = buf.readResourceLocation();
                IRecipeOutcome.Type<?> type = ModRegistries.OUTCOME_TYPE_REGISTRY.get(location);
                if (type == null) throw new IllegalArgumentException("Unknown outcome type: " + location);
                IRecipeOutcome<?> outcome = type.streamCodec().decode(buf);
                outcomes.add(outcome);
            }
            return new InWorldRecipe(
                icon,
                trigger,
                Collections.unmodifiableList(conflicting),
                Collections.unmodifiableList(nonConflicting),
                Collections.unmodifiableList(outcomes),
                buf.readInt(),
                buf.readBoolean()
            );
        }

        private static @NotNull List<IRecipePredicate<?>> decodeRecipePredicateList(
            @NotNull RegistryFriendlyByteBuf buf
        ) {
            int size = buf.readVarInt();
            List<IRecipePredicate<?>> predicates = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                ResourceLocation location = buf.readResourceLocation();
                IRecipePredicate.Type<?> type = ModRegistries.PREDICATE_TYPE_REGISTRY.get(location);
                if (type == null) throw new IllegalArgumentException("Unknown predicate type: " + location);
                IRecipePredicate<?> predicate = type.streamCodec().decode(buf);
                predicates.add(predicate);
            }
            return predicates;
        }
    }
}
