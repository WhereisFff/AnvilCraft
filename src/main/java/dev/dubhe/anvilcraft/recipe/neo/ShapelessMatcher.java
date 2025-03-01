package dev.dubhe.anvilcraft.recipe.neo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ShapelessMatcher {
    public interface Context<C extends Context<C>> {
        default <P extends Predicate<C>> void push(@NotNull Stack<P> stack, P predicate) {
            stack.push(predicate);
        }

        default <P extends Predicate<C>> void pop(@NotNull Stack<P> stack) {
            stack.pop();
        }
    }

    public interface MatcherPredicate<T extends Context<T>> extends Predicate<T>, Consumer<T> {

    }

    public static <T extends Context<T>, P extends Predicate<T>> boolean incompatible(@NotNull List<P> predicates, @NotNull T ctx, Stack<P> stack) {
        for (P predicate : predicates) {
            if (!predicate.test(ctx)) continue;
            List<P> next = new ArrayList<>();
            for (P predicate1 : predicates) {
                if (predicate1 == predicate) continue;
                next.add(predicate1);
            }
            ctx.push(stack, predicate);
            if (next.isEmpty()) return true;
            boolean flag = incompatible(next, ctx, stack);
            if (!flag) ctx.pop(stack);
            return flag;
        }
        return false;
    }

    public static <T, P extends Predicate<T>> boolean compatible(@NotNull List<P> predicates, @Nullable T ctx) {
        for (P predicate : predicates) {
            if (!predicate.test(ctx)) return false;
        }
        return true;
    }

    public record ItemStack(String name, int count) {
    }

    public static class TestContext implements Context<TestContext> {
        private final List<ItemStack> stacks = new ArrayList<>();
        private final Map<String, Object> data = new HashMap<>();

        public TestContext addStack(ItemStack stack) {
            stacks.add(stack);
            return this;
        }

        public TestContext addData(String key, Object value) {
            data.put(key, value);
            return this;
        }

        public <T> T getData(String key, @SuppressWarnings("unused") Class<T> typeOfT) {
            //noinspection unchecked
            return (T) data.get(key);
        }

        public <T> T computeIfAbsent(String key, Function<String, T> supplier) {
            //noinspection unchecked
            return (T) data.computeIfAbsent(key, supplier);
        }

        public <T> void setData(String key, T value) {
            data.put(key, value);
        }
    }

    public record ItemStackPredicate(List<String> names, int count) implements Predicate<TestContext> {
        @Override
        public boolean test(TestContext testContext) {
            return false;
        }

        public <P extends Predicate<TestContext>> void push(@NotNull Stack<P> stack, TestContext context) {
        }

        public <P extends Predicate<TestContext>> void pop(@NotNull Stack<P> stack, TestContext context) {
        }
    }

    public static void main(String[] args) {

    }
}
