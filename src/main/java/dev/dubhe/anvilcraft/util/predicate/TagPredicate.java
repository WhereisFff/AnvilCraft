package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.function.Consumers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public record TagPredicate<T>(List<TagKey<T>> tags, boolean anyOf, boolean expected, boolean canBeEmpty) {
    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends Registry<T>> registryKey) {
        return RecordCodecBuilder.create(ins -> ins.group(
            TagKey.codec(registryKey).listOf().fieldOf("tags").forGetter(TagPredicate::tags),
            Codec.BOOL.fieldOf("anyOf").forGetter(TagPredicate::anyOf),
            Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected),
            Codec.BOOL.fieldOf("canBeEmpty").forGetter(TagPredicate::canBeEmpty)
        ).apply(ins, TagPredicate::new));
    }

    @SafeVarargs
    public static <T> TagPredicate<T> is(boolean anyOf, boolean canBeEmpty, TagKey<T>... tag) {
        return new TagPredicate<>(ImmutableList.copyOf(tag), anyOf, true, canBeEmpty);
    }

    @SafeVarargs
    public static <T> TagPredicate<T> isNot(boolean anyOf, boolean canBeEmpty, TagKey<T>... tag) {
        return new TagPredicate<>(ImmutableList.copyOf(tag), anyOf, false, canBeEmpty);
    }

    public TagPredicate<T> append(TagPredicate<T> another) {
        return new TagPredicate<>(
            ImmutableList.<TagKey<T>>builder().addAll(this.tags).addAll(another.tags).build(),
            this.anyOf, this.expected, this.canBeEmpty
        );
    }

    public boolean matches(Holder<T> value) {
        AtomicBoolean isAllEmpty = new AtomicBoolean(true);
        Optional<HolderLookup.RegistryLookup<T>> registry = Optional.ofNullable(value.unwrapLookup());
        for (TagKey<T> tag : this.tags) {
            registry.flatMap(lookup -> lookup.get(tag))
                .filter(it -> it.size() != 0)
                .ifPresent(it -> isAllEmpty.set(false));
            if (value.is(tag) == this.anyOf) {
                return this.anyOf == this.expected;
            }
        }
        boolean emptyCheck = isAllEmpty.get() && this.canBeEmpty;
        return emptyCheck || !this.expected;
    }
}
