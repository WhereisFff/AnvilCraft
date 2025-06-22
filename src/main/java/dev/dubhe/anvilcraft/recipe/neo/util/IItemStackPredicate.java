package dev.dubhe.anvilcraft.recipe.neo.util;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public interface IItemStackPredicate extends Predicate<ItemStack> {
    Optional<HolderSet<Item>> items();

    DataComponentPredicate components();

    Map<ItemSubPredicate.Type<?>, ItemSubPredicate> subPredicates();

    boolean testCount(int count);

    default boolean testIgnoreCount(ItemStack itemStack) {
        if (this.items().isPresent() && !itemStack.is(this.items().get())) {
            return false;
        } else if (!this.components().test(itemStack)) {
            return false;
        } else {
            for (ItemSubPredicate itemsubpredicate : this.subPredicates().values()) {
                if (!itemsubpredicate.matches(itemStack)) {
                    return false;
                }
            }
            return true;
        }
    }

    default Predicate<ItemStack> testIgnoreCount() {
        return new TestIgnoreCountPredicate(this);
    }

    class TestIgnoreCountPredicate implements Predicate<ItemStack> {
        private final IItemStackPredicate self;

        public TestIgnoreCountPredicate(IItemStackPredicate self) {
            this.self = self;
        }

        @Override
        public boolean test(ItemStack stack) {
            return this.self.testIgnoreCount(stack);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof TestIgnoreCountPredicate predicate && this.self.equals(predicate.self));
        }

        @Override
        public int hashCode() {
            return this.self.hashCode();
        }
    }
}
