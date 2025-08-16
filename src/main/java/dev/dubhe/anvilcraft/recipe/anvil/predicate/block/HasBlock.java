package dev.dubhe.anvilcraft.recipe.anvil.predicate.block;

import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.anvil.util.BlockStatePredicate;
import lombok.Getter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

@Getter
public class HasBlock extends HasBlockBase<HasBlock> {
    public HasBlock(Vec3 offset, BlockStatePredicate predicate) {
        super(offset, predicate);
    }

    @Override
    public Type getType() {
        return ModRecipePredicateTypes.HAS_BLOCK.get();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Type extends AbstractType<HasBlock> {
        @Override
        public HasBlock of(Vec3 offset, BlockStatePredicate predicate) {
            return new HasBlock(offset, predicate);
        }
    }

    public static class Builder {
        private Vec3 offset = Vec3.ZERO;
        private final BlockStatePredicate.Builder predicate = BlockStatePredicate.builder();

        public Builder offset(Vec3 offset) {
            this.offset = offset;
            return this;
        }

        public Builder offset(double x, double y, double z) {
            this.offset = new Vec3(x, y, z);
            return this;
        }

        public Builder below(double below) {
            return this.offset(Vec3.ZERO.subtract(0, below, 0));
        }

        public Builder below() {
            return this.below(1);
        }

        public Builder above(double above) {
            return this.offset(Vec3.ZERO.add(0, above, 0));
        }

        public Builder above() {
            return this.above(1);
        }

        public Builder predicate(@NotNull Consumer<BlockStatePredicate.Builder> consumer) {
            consumer.accept(this.predicate);
            return this;
        }

        public Builder of(Block... blocks) {
            this.predicate.of(blocks);
            return this;
        }

        public Builder of(Collection<Block> blocks) {
            this.predicate.of(blocks);
            return this;
        }

        public Builder of(TagKey<Block> tag) {
            this.predicate.of(tag);
            return this;
        }

        public Builder with(@NotNull Property<?> property, String value) {
            this.predicate.with(property, value);
            return this;
        }

        public Builder with(Property<Integer> property, int value) {
            this.predicate.with(property, value);
            return this;
        }

        public Builder with(Property<Boolean> property, boolean value) {
            this.predicate.with(property, value);
            return this;
        }

        public <T extends Comparable<T>> Builder with(Property<T> property, @NotNull T value) {
            this.predicate.with(property, value);
            return this;
        }

        public <T extends Comparable<T>> Builder with(
            @NotNull Property<T> property,
            @Nullable T minValue,
            @Nullable T maxValue
        ) {
            this.predicate.with(property, minValue, maxValue);
            return this;
        }

        public <T extends Comparable<T>> Builder withMin(
            @NotNull Property<T> property,
            T minValue
        ) {
            this.predicate.withMin(property, minValue);
            return this;
        }

        public <T extends Comparable<T>> Builder withMax(
            @NotNull Property<T> property,
            T maxValue
        ) {
            this.predicate.withMax(property, maxValue);
            return this;
        }

        public Builder or() {
            this.predicate.or();
            return this;
        }

        public HasBlockIngredient build() {
            return new HasBlockIngredient(offset, predicate.build());
        }
    }
}
