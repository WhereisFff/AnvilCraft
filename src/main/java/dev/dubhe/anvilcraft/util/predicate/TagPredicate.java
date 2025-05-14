package dev.dubhe.anvilcraft.util.predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;

public record TagPredicate<T>(List<TagKey<T>> tags, boolean anyOf, boolean expected) {
    public static <T> Codec<TagPredicate<T>> codec(ResourceKey<? extends Registry<T>> registryKey) {
        return RecordCodecBuilder.create(
            p_299212_ -> p_299212_.group(
                    TagKey.codec(registryKey).listOf().fieldOf("tags").forGetter(TagPredicate::tags),
                    Codec.BOOL.fieldOf("anyOf").forGetter(TagPredicate::anyOf),
                    Codec.BOOL.fieldOf("expected").forGetter(TagPredicate::expected)
                )
                .apply(p_299212_, TagPredicate::new)
        );
    }

    @SafeVarargs
    public static <T> TagPredicate<T> is(boolean anyOf, TagKey<T>... tag) {
        return new TagPredicate<>(ImmutableList.copyOf(tag), anyOf, true);
    }

    @SafeVarargs
    public static <T> TagPredicate<T> isNot(boolean anyOf, TagKey<T>... tag) {
        return new TagPredicate<>(ImmutableList.copyOf(tag), anyOf, false);
    }

    public TagPredicate<T> append(TagPredicate<T> another) {
        return new TagPredicate<>(
            ImmutableList.<TagKey<T>>builder().addAll(this.tags).addAll(another.tags).build(),
            this.anyOf, this.expected
        );
    }

    public boolean matches(Holder<T> value) {
        for (TagKey<T> tag : this.tags) {
            if (value.is(tag) == this.anyOf) {
                return this.anyOf == this.expected;
            }
        }
        return !this.expected;
    }
}
