package dev.dubhe.anvilcraft.util.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public record EntityTypePredicate(HolderSet<EntityType<?>> types, boolean canBeEmpty) {
    public static final Codec<EntityTypePredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("types").forGetter(EntityTypePredicate::types),
        Codec.BOOL.fieldOf("canBeEmpty").forGetter(EntityTypePredicate::canBeEmpty)
    ).apply(ins, EntityTypePredicate::new));

    @SuppressWarnings("deprecation")
    public static EntityTypePredicate of(EntityType<?> type, boolean canBeEmpty) {
        return new EntityTypePredicate(HolderSet.direct(type.builtInRegistryHolder()), canBeEmpty);
    }

    public static EntityTypePredicate of(TagKey<EntityType<?>> tag, boolean canBeEmpty) {
        return new EntityTypePredicate(BuiltInRegistries.ENTITY_TYPE.getOrCreateTag(tag), canBeEmpty);
    }

    public boolean matches(EntityType<?> type) {
        return (types.size() == 0 && this.canBeEmpty) || type.is(this.types);
    }
}
