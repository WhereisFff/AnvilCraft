package dev.dubhe.anvilcraft.recipe.anvil.builder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.anvil.IRecipeTrigger;
import dev.dubhe.anvilcraft.recipe.anvil.InWorldRecipe;
import dev.dubhe.anvilcraft.recipe.anvil.outcome.DamageAnvil;
import dev.dubhe.anvilcraft.recipe.anvil.outcome.ProduceHeat;
import dev.dubhe.anvilcraft.recipe.anvil.outcome.SetBlock;
import dev.dubhe.anvilcraft.recipe.anvil.outcome.SpawnItem;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasBlock;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasBlockIngredient;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.block.HasCauldron;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.item.HasItem;
import dev.dubhe.anvilcraft.recipe.anvil.predicate.item.HasItemIngredient;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@EqualsAndHashCode
@SuppressWarnings("unused")
public class InWorldRecipeBuilder implements RecipeBuilder {
    private final NonNullList<ItemStack> icon = NonNullList.withSize(1, Items.ANVIL.getDefaultInstance());
    private Vec3 offset = Vec3.ZERO;
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

    public InWorldRecipeBuilder offset(Vec3 offset) {
        this.offset = offset;
        return this;
    }

    public InWorldRecipeBuilder offset(double x, double y, double z) {
        return this.offset(new Vec3(x, y, z));
    }

    public InWorldRecipeBuilder below(double below) {
        return this.offset(Vec3.ZERO.subtract(0, below, 0));
    }

    public InWorldRecipeBuilder below() {
        return this.below(1);
    }

    public InWorldRecipeBuilder above(double above) {
        return this.offset(Vec3.ZERO.add(0, above, 0));
    }

    public InWorldRecipeBuilder above() {
        return this.above(1);
    }

    public InWorldRecipeBuilder hasItem(@NotNull Consumer<HasItem.Builder> consumer) {
        HasItem.Builder builder = HasItem.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.with(builder.build());
    }

    public InWorldRecipeBuilder hasItem(ItemLike... items) {
        return this.with(HasItem.builder().of(items).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasItem(Vec3 offset, ItemLike... items) {
        return this.with(HasItem.builder().of(items).offset(offset).build());
    }

    public InWorldRecipeBuilder hasItem(double x, double y, double z, ItemLike... items) {
        return this.with(HasItem.builder().of(items).offset(x, y, z).build());
    }

    public InWorldRecipeBuilder hasItem(TagKey<Item> items) {
        return this.with(HasItem.builder().of(items).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasItem(Vec3 offset, TagKey<Item> items) {
        return this.with(HasItem.builder().offset(offset).build());
    }

    public InWorldRecipeBuilder hasItem(double x, double y, double z, TagKey<Item> items) {
        return this.with(HasItem.builder().offset(x, y, z).build());
    }

    public InWorldRecipeBuilder hasItemIngredient(@NotNull Consumer<HasItemIngredient.Builder> consumer) {
        HasItemIngredient.Builder builder = HasItemIngredient.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.with(builder.build());
    }

    public InWorldRecipeBuilder hasItemIngredient(ItemLike... items) {
        return this.with(HasItemIngredient.builder().of(items).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasItemIngredient(Vec3 offset, ItemLike... items) {
        return this.with(HasItemIngredient.builder().of(items).offset(offset).build());
    }

    public InWorldRecipeBuilder hasItemIngredient(double x, double y, double z, ItemLike... items) {
        return this.with(HasItemIngredient.builder().of(items).offset(x, y, z).build());
    }

    public InWorldRecipeBuilder hasItemIngredient(TagKey<Item> items) {
        return this.with(HasItemIngredient.builder().of(items).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasItemIngredient(Vec3 offset, TagKey<Item> items) {
        return this.with(HasItemIngredient.builder().of(items).offset(offset).build());
    }

    public InWorldRecipeBuilder hasItemIngredient(double x, double y, double z, TagKey<Item> items) {
        return this.with(HasItemIngredient.builder().of(items).offset(x, y, z).build());
    }

    public InWorldRecipeBuilder hasBlock(@NotNull Consumer<HasBlock.Builder> consumer) {
        HasBlock.Builder builder = HasBlock.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.with(builder.build());
    }

    public InWorldRecipeBuilder hasBlock(Block... blocks) {
        return this.with(HasBlock.builder().of(blocks).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasBlock(Vec3 offset, Block... blocks) {
        return this.with(HasBlock.builder().of(blocks).offset(offset).build());
    }

    public InWorldRecipeBuilder hasBlock(double x, double y, double z, Block... blocks) {
        return this.with(HasBlock.builder().of(blocks).offset(x, y, z).build());
    }

    public InWorldRecipeBuilder hasBlock(Collection<Block> blocks) {
        return this.with(HasBlock.builder().of(blocks).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasBlock(Vec3 offset, Collection<Block> blocks) {
        return this.with(HasBlock.builder().of(blocks).offset(offset).build());
    }

    public InWorldRecipeBuilder hasBlock(double x, double y, double z, Collection<Block> blocks) {
        return this.with(HasBlock.builder().of(blocks).offset(x, y, z).build());
    }

    public InWorldRecipeBuilder hasBlock(TagKey<Block> tag) {
        return this.with(HasBlock.builder().of(tag).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasBlock(Vec3 offset, TagKey<Block> tag) {
        return this.with(HasBlock.builder().of(tag).offset(offset).build());
    }

    public InWorldRecipeBuilder hasBlock(double x, double y, double z, TagKey<Block> tag) {
        return this.with(HasBlock.builder().of(tag).offset(x, y, z).build());
    }

    public <T extends Comparable<T>> InWorldRecipeBuilder hasBlock(Vec3 offset, @NotNull BlockState state) {
        HasBlock.Builder builder = HasBlock.builder();
        Block block = state.getBlock();
        builder.of(block);
        builder.offset(offset);
        BlockState defaultState = block.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            Comparable<?> value = state.getValue(property);
            Comparable<?> defaultValue = defaultState.getValue(property);
            if (value.equals(defaultValue)) continue;
            //noinspection unchecked
            builder.with((Property<T>) property, (T) value);
        }
        return this.with(builder.build());
    }

    public <T extends Comparable<T>> InWorldRecipeBuilder hasBlock(double x, double y, double z, @NotNull BlockState state) {
        return this.hasBlock(new Vec3(x, y, z), state);
    }

    public <T extends Comparable<T>> InWorldRecipeBuilder hasBlock(@NotNull BlockState state) {
        return this.hasBlock(this.offset, state);
    }

    public InWorldRecipeBuilder hasBlockIngredient(@NotNull Consumer<HasBlockIngredient.Builder> consumer) {
        HasBlockIngredient.Builder builder = HasBlockIngredient.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.with(builder.build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(Block... blocks) {
        return this.with(HasBlockIngredient.builder().of(blocks).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(Vec3 offset, Block... blocks) {
        return this.with(HasBlockIngredient.builder().of(blocks).offset(offset).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(double x, double y, double z, Block... blocks) {
        return this.with(HasBlockIngredient.builder().of(blocks).offset(new Vec3(x, y, z)).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(Collection<Block> blocks) {
        return this.with(HasBlockIngredient.builder().of(blocks).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(Vec3 offset, Collection<Block> blocks) {
        return this.with(HasBlockIngredient.builder().of(blocks).offset(offset).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(double x, double y, double z, Collection<Block> blocks) {
        return this.with(HasBlockIngredient.builder().of(blocks).offset(new Vec3(x, y, z)).build());
    }


    public InWorldRecipeBuilder hasBlockIngredient(TagKey<Block> tag) {
        return this.with(HasBlockIngredient.builder().of(tag).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(Vec3 offset, TagKey<Block> tag) {
        return this.with(HasBlockIngredient.builder().of(tag).offset(offset).build());
    }

    public InWorldRecipeBuilder hasBlockIngredient(double x, double y, double z, TagKey<Block> tag) {
        return this.with(HasBlockIngredient.builder().of(tag).offset(new Vec3(x, y, z)).build());
    }

    public <T extends Comparable<T>> InWorldRecipeBuilder hasBlockIngredient(Vec3 offset, @NotNull BlockState state) {
        HasBlockIngredient.Builder builder = HasBlockIngredient.builder();
        Block block = state.getBlock();
        BlockState defaultState = block.defaultBlockState();
        builder.of(block);
        builder.offset(offset);
        for (Property<?> property : state.getProperties()) {
            Comparable<?> value = state.getValue(property);
            if (value.equals(defaultState.getValue(property))) continue;
            //noinspection unchecked
            builder.with((Property<T>) property, (T) value);
        }
        return this.with(builder.build());
    }

    public <T extends Comparable<T>> InWorldRecipeBuilder hasBlockIngredient(double x, double y, double z, @NotNull BlockState state) {
        return this.hasBlockIngredient(new Vec3(x, y, z), state);
    }

    public <T extends Comparable<T>> InWorldRecipeBuilder hasBlockIngredient(@NotNull BlockState state) {
        return this.hasBlockIngredient(this.offset, state);
    }

    public InWorldRecipeBuilder hasCauldron(@NotNull Consumer<HasCauldron.Builder> consumer) {
        HasCauldron.Builder builder = HasCauldron.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.with(builder.build());
    }

    public InWorldRecipeBuilder hasCauldron() {
        return this.with(HasCauldron.builder().empty().offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasCauldron(Vec3 offset) {
        return this.with(HasCauldron.builder().empty().offset(offset).build());
    }

    public InWorldRecipeBuilder hasCauldron(double x, double y, double z) {
        return this.with(HasCauldron.builder().empty().offset(new Vec3(x, y, z)).build());
    }

    public InWorldRecipeBuilder hasCauldron(ResourceLocation fluid) {
        return this.with(HasCauldron.builder().fluid(fluid).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasCauldron(Vec3 offset, ResourceLocation fluid) {
        return this.with(HasCauldron.builder().fluid(fluid).offset(offset).build());
    }

    public InWorldRecipeBuilder hasCauldron(double x, double y, double z, ResourceLocation fluid) {
        return this.with(HasCauldron.builder().fluid(fluid).offset(new Vec3(x, y, z)).build());
    }

    public InWorldRecipeBuilder hasCauldron(ResourceLocation fluid, int consume) {
        return this.with(HasCauldron.builder().fluid(fluid).offset(this.offset).consume(consume).build());
    }

    public InWorldRecipeBuilder hasCauldron(Vec3 offset, ResourceLocation fluid, int consume) {
        return this.with(HasCauldron.builder().fluid(fluid).offset(offset).consume(consume).build());
    }

    public InWorldRecipeBuilder hasCauldron(double x, double y, double z, ResourceLocation fluid, int consume) {
        return this.with(HasCauldron.builder().fluid(fluid).offset(new Vec3(x, y, z)).consume(consume).build());
    }

    public InWorldRecipeBuilder hasCauldron(Block cauldron) {
        return this.with(HasCauldron.builder().cauldron(cauldron).offset(this.offset).build());
    }

    public InWorldRecipeBuilder hasCauldron(Vec3 offset, Block cauldron) {
        return this.with(HasCauldron.builder().cauldron(cauldron).offset(offset).build());
    }

    public InWorldRecipeBuilder hasCauldron(double x, double y, double z, Block cauldron) {
        return this.with(HasCauldron.builder().cauldron(cauldron).offset(new Vec3(x, y, z)).build());
    }

    public InWorldRecipeBuilder hasCauldron(Block cauldron, int consume) {
        return this.with(HasCauldron.builder().cauldron(cauldron).offset(this.offset).consume(consume).build());
    }

    public InWorldRecipeBuilder hasCauldron(Vec3 offset, Block cauldron, int consume) {
        return this.with(HasCauldron.builder().cauldron(cauldron).offset(offset).consume(consume).build());
    }

    public InWorldRecipeBuilder hasCauldron(double x, double y, double z, Block cauldron, int consume) {
        return this.with(HasCauldron.builder().cauldron(cauldron).offset(new Vec3(x, y, z)).consume(consume).build());
    }

    public InWorldRecipeBuilder out(@NotNull IRecipeOutcome<?> outcome) {
        this.outcomes.add(outcome);
        return this;
    }

    public InWorldRecipeBuilder spawnItem(@NotNull Consumer<SpawnItem.Builder> consumer) {
        SpawnItem.Builder builder = SpawnItem.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.out(builder.build());
    }

    public InWorldRecipeBuilder spawnItem(Vec3 offset, double chance, ItemStack stack) {
        return this.out(SpawnItem.builder().offset(offset).count((float) chance).item(stack).build());
    }

    public InWorldRecipeBuilder spawnItem(Vec3 offset, ItemStack stack) {
        return this.spawnItem(offset, 1, stack);
    }

    public InWorldRecipeBuilder spawnItem(double x, double y, double z, double chance, ItemStack stack) {
        return this.spawnItem(new Vec3(x, y, z), chance, stack);
    }

    public InWorldRecipeBuilder spawnItem(double x, double y, double z, ItemStack stack) {
        return this.spawnItem(new Vec3(x, y, z), stack);
    }

    public InWorldRecipeBuilder spawnItem(ItemStack stack) {
        return this.spawnItem(this.offset, stack);
    }

    public InWorldRecipeBuilder setBlock(@NotNull Consumer<SetBlock.Builder> consumer) {
        SetBlock.Builder builder = SetBlock.builder();
        builder.offset(this.offset);
        consumer.accept(builder);
        return this.out(builder.build());
    }

    public InWorldRecipeBuilder setBlock(Vec3 offset, double chance, @NotNull BlockState state) {
        return this.out(SetBlock.builder().block(state).offset(offset).chance((float) chance).build());
    }

    public InWorldRecipeBuilder setBlock(Vec3 offset, @NotNull BlockState state) {
        return this.setBlock(offset, 1, state);
    }

    public InWorldRecipeBuilder setBlock(double x, double y, double z, double chance, @NotNull BlockState state) {
        return this.setBlock(new Vec3(x, y, z), chance, state);
    }

    public InWorldRecipeBuilder setBlock(double x, double y, double z, @NotNull BlockState state) {
        return this.setBlock(new Vec3(x, y, z), state);
    }

    public InWorldRecipeBuilder setBlock(@NotNull BlockState state) {
        return this.setBlock(this.offset, state);
    }

    public InWorldRecipeBuilder produceHeat(ProduceHeat.@NotNull Builder builder) {
        this.out(builder.build());
        return this;
    }

    public InWorldRecipeBuilder damageAnvil() {
        this.out(new DamageAnvil());
        return this;
    }

    public InWorldRecipe build() {
        return new InWorldRecipe(
            icon.getFirst(),
            trigger,
            ImmutableList.copyOf(conflicting),
            ImmutableList.copyOf(nonConflicting),
            ImmutableList.copyOf(outcomes),
            InWorldRecipe.calcPriority(trigger, conflicting, nonConflicting, outcomes),
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
