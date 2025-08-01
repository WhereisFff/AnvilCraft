package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public class BlockStatePredicate implements Predicate<BlockState> {
    public static final Codec<List<PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap(
            Codec.STRING, ValueMatcher.CODEC
        )
        .xmap(
            map -> map.entrySet()
                .stream()
                .map(entry -> new PropertyMatcher(entry.getKey(), entry.getValue()))
                .toList(),
            list -> list.stream()
                .collect(Collectors.toMap(PropertyMatcher::name, PropertyMatcher::valueMatcher))
        );
    public static final StreamCodec<ByteBuf, List<List<PropertyMatcher>>> PROPERTIES_STREAM_CODEC = PropertyMatcher.STREAM_CODEC
        .apply(ByteBufCodecs.list())
        .apply(ByteBufCodecs.list())
        .map(lists -> lists, lists -> lists);
    public static final Codec<BlockStatePredicate> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.BLOCK)
                    .optionalFieldOf("blocks", HolderSet.empty())
                    .forGetter(BlockStatePredicate::getBlocks),
                PROPERTIES_CODEC
                    .listOf()
                    .optionalFieldOf("properties", List.of())
                    .forGetter(BlockStatePredicate::getProperties)
            )
            .apply(instance, BlockStatePredicate::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockStatePredicate> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.holderSet(Registries.BLOCK),
        BlockStatePredicate::getBlocks,
        PROPERTIES_STREAM_CODEC,
        BlockStatePredicate::getProperties,
        BlockStatePredicate::new
    );

    private final HolderSet<Block> blocks;
    private final List<List<PropertyMatcher>> properties;

    private BlockStatePredicate(HolderSet<Block> blocks, List<List<PropertyMatcher>> properties) {
        this.blocks = blocks;
        this.properties = properties;
    }

    @Override
    public boolean test(BlockState state) {
        if (this.blocks.size() > 0 && !state.is(this.blocks)) return false;
        if (this.properties.isEmpty()) return true;
        for (List<PropertyMatcher> matchers : this.properties) {
            boolean flag = true;
            for (PropertyMatcher matcher : matchers) {
                if (!matcher.match(state.getBlock().getStateDefinition(), state)) {
                    flag = false;
                    break;
                }
            }
            if (flag) return true;
        }
        return false;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("deprecation")
    public static class Builder {
        private final List<List<PropertyMatcher>> properties = new ArrayList<>();
        private HolderSet<Block> blocks = HolderSet.empty();
        private List<PropertyMatcher> and = new ArrayList<>();

        private Builder() {
        }

        public Builder of(Block... blocks) {
            this.blocks = HolderSet.direct(Block::builtInRegistryHolder, blocks);
            return this;
        }

        public Builder of(Collection<Block> blocks) {
            this.blocks = HolderSet.direct(Block::builtInRegistryHolder, blocks);
            return this;
        }

        public Builder of(TagKey<Block> tag) {
            this.blocks = BuiltInRegistries.BLOCK.getOrCreateTag(tag);
            return this;
        }

        public Builder with(@NotNull Property<?> property, String value) {
            this.and.add(new PropertyMatcher(property.getName(), new ExactMatcher(value)));
            return this;
        }

        public Builder with(Property<Integer> property, int value) {
            return this.with(property, Integer.toString(value));
        }

        public Builder with(Property<Boolean> property, boolean value) {
            return this.with(property, Boolean.toString(value));
        }

        public <T extends Comparable<T> & StringRepresentable> Builder with(Property<T> property, @NotNull T value) {
            return this.with(property, value.getSerializedName());
        }

        public Builder or() {
            this.properties.add(this.and);
            this.and = new ArrayList<>();
            return this;
        }

        public BlockStatePredicate build() {
            if (!this.and.isEmpty()) this.or();
            return new BlockStatePredicate(this.blocks, Collections.unmodifiableList(this.properties));
        }
    }

    public record PropertyMatcher(String name, ValueMatcher valueMatcher) {
        public static final StreamCodec<ByteBuf, PropertyMatcher> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PropertyMatcher::name,
            ValueMatcher.STREAM_CODEC,
            PropertyMatcher::valueMatcher,
            PropertyMatcher::new
        );

        public <S extends StateHolder<?, S>> boolean match(@NotNull StateDefinition<?, S> properties, S propertyToMatch) {
            Property<?> property = properties.getProperty(this.name);
            return property != null && this.valueMatcher.match(propertyToMatch, property);
        }
    }

    public record ExactMatcher(String value) implements ValueMatcher {
        public static final Codec<ExactMatcher> CODEC = Codec.STRING
            .xmap(ExactMatcher::new, ExactMatcher::value);
        public static final StreamCodec<ByteBuf, ExactMatcher> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(ExactMatcher::new, ExactMatcher::value);

        @Override
        public <T extends Comparable<T>> boolean match(@NotNull StateHolder<?, ?> value, Property<T> property) {
            T t = value.getValue(property);
            Optional<T> optional = property.getValue(this.value);
            return optional.isPresent() && t.compareTo(optional.get()) == 0;
        }
    }

    public record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements ValueMatcher {
        public static final Codec<RangedMatcher> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.optionalFieldOf("min").forGetter(RangedMatcher::minValue),
                    Codec.STRING.optionalFieldOf("max").forGetter(RangedMatcher::maxValue)
                )
                .apply(instance, RangedMatcher::new)
        );
        public static final StreamCodec<ByteBuf, RangedMatcher> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            RangedMatcher::minValue,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            RangedMatcher::maxValue,
            RangedMatcher::new
        );

        @Override
        public <T extends Comparable<T>> boolean match(@NotNull StateHolder<?, ?> stateHolder, Property<T> property) {
            T t = stateHolder.getValue(property);
            if (this.minValue.isPresent()) {
                Optional<T> optional = property.getValue(this.minValue.get());
                if (optional.isEmpty() || t.compareTo(optional.get()) < 0) {
                    return false;
                }
            }

            if (this.maxValue.isPresent()) {
                Optional<T> optional1 = property.getValue(this.maxValue.get());
                return optional1.isPresent() && t.compareTo(optional1.get()) <= 0;
            }

            return true;
        }
    }

    public interface ValueMatcher {
        Codec<ValueMatcher> CODEC = Codec.either(
                ExactMatcher.CODEC, RangedMatcher.CODEC
            )
            .xmap(Either::unwrap, matcher -> {
                if (matcher instanceof ExactMatcher statepropertiespredicate$exactmatcher) {
                    return Either.left(statepropertiespredicate$exactmatcher);
                } else if (matcher instanceof RangedMatcher statepropertiespredicate$rangedmatcher) {
                    return Either.right(statepropertiespredicate$rangedmatcher);
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        StreamCodec<ByteBuf, ValueMatcher> STREAM_CODEC = ByteBufCodecs.either(
                ExactMatcher.STREAM_CODEC, RangedMatcher.STREAM_CODEC
            )
            .map(Either::unwrap, matcher -> {
                if (matcher instanceof ExactMatcher statepropertiespredicate$exactmatcher) {
                    return Either.left(statepropertiespredicate$exactmatcher);
                } else if (matcher instanceof RangedMatcher statepropertiespredicate$rangedmatcher) {
                    return Either.right(statepropertiespredicate$rangedmatcher);
                } else {
                    throw new UnsupportedOperationException();
                }
            });

        <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property);
    }
}
