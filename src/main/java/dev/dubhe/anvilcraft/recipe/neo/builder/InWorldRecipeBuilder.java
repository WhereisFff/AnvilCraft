package dev.dubhe.anvilcraft.recipe.neo.builder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeTrigger;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.neo.builder.predicate.item.HasItemBuilder;
import dev.dubhe.anvilcraft.recipe.neo.builder.predicate.item.HasItemIngredientBuilder;
import dev.dubhe.anvilcraft.recipe.neo.outcome.DamageAnvil;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SpawnItem;
import lombok.EqualsAndHashCode;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@EqualsAndHashCode
public class InWorldRecipeBuilder implements RecipeBuilder {
    private final NonNullList<ItemStack> icon = NonNullList.withSize(1, Items.ANVIL.getDefaultInstance());
    private final @NotNull IRecipeTrigger trigger;
    private final List<IRecipePredicate<?>> conflicting = new ArrayList<>();
    private final List<IRecipePredicate<?>> nonConflicting = new ArrayList<>();
    private final List<IRecipeOutcome<?>> outcomes = new ArrayList<>();
    private final boolean compatible;
    private String group;
    private final Map<String, Criterion<?>> criteria = Maps.newLinkedHashMap();

    private InWorldRecipeBuilder(@NotNull IRecipeTrigger trigger, boolean compatible) {
        this.trigger = trigger;
        this.compatible = compatible;
    }

    public static @NotNull InWorldRecipeBuilder compatible(@NotNull IRecipeTrigger trigger) {
        return new InWorldRecipeBuilder(trigger, true);
    }

    public static @NotNull InWorldRecipeBuilder incompatible(@NotNull IRecipeTrigger trigger) {
        return new InWorldRecipeBuilder(trigger, false);
    }

    public InWorldRecipeBuilder icon(@NotNull ItemStack icon) {
        this.icon.set(0, icon);
        return this;
    }

    public InWorldRecipeBuilder with(@NotNull IRecipePredicate<?> predicate) {
        if (predicate.getType().conflict()) {
            this.conflicting.add(predicate);
        } else {
            this.nonConflicting.add(predicate);
        }
        return this;
    }

    public HasItemBuilder hasItem() {
        return new HasItemBuilder(this);
    }

    public InWorldRecipeBuilder hasItem(ItemLike... items) {
        return new HasItemBuilder(this).of(items).build();
    }

    public InWorldRecipeBuilder hasItem(TagKey<Item> items) {
        return new HasItemBuilder(this).of(items).build();
    }

    public HasItemIngredientBuilder hasItemIngredient() {
        return new HasItemIngredientBuilder(this);
    }

    public InWorldRecipeBuilder hasItemIngredient(ItemLike... items) {
        return new HasItemIngredientBuilder(this).of(items).build();
    }

    public InWorldRecipeBuilder hasItemIngredient(TagKey<Item> items) {
        return new HasItemIngredientBuilder(this).of(items).build();
    }

    public InWorldRecipeBuilder out(@NotNull IRecipeOutcome<?> outcome) {
        this.outcomes.add(outcome);
        return this;
    }

    public InWorldRecipeBuilder spawnItem(ItemStack stack) {
        return this.spawnItem(stack, -1.0, -1.0, -1.0);
    }

    public InWorldRecipeBuilder spawnItem(ItemStack stack, double offsetX, double offsetY, double offsetZ) {
        return this.spawnItem(stack, new Vec3(offsetX, offsetY, offsetZ));
    }

    public InWorldRecipeBuilder spawnItem(ItemStack stack, Vec3 offset) {
        return this.out(new SpawnItem(stack, offset));
    }

    public InWorldRecipeBuilder damageAnvil() {
        this.outcomes.add(new DamageAnvil());
        return this;
    }

    public InWorldRecipe build() {
        int priority = trigger.getPriority();
        for (IRecipePredicate<?> predicate : conflicting) {
            priority += predicate.getPriority();
        }
        for (IRecipePredicate<?> predicate : nonConflicting) {
            priority += predicate.getPriority();
        }
        for (IRecipeOutcome<?> outcome : outcomes) {
            priority += outcome.getPriority();
        }
        return new InWorldRecipe(
            icon.getFirst(),
            trigger,
            ImmutableList.copyOf(conflicting),
            ImmutableList.copyOf(nonConflicting),
            ImmutableList.copyOf(outcomes),
            priority,
            compatible
        );
    }

    @Override
    public @NotNull InWorldRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull InWorldRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.icon.getFirst().getItem();
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull ResourceLocation id) {
        Advancement.Builder builder = recipeOutput.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR);
        Objects.requireNonNull(builder);
        this.criteria.forEach(builder::addCriterion);
        InWorldRecipe recipe = this.build();
        recipeOutput.accept(
            ResourceLocation.fromNamespaceAndPath(id.getNamespace(), this.group + "/" + id.getPath()),
            recipe,
            builder.build(id.withPrefix("recipes/" + this.group + "/"))
        );
    }
}
