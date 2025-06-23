package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

public class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> AMULET_VALID = bind("amulet_valid");
    public static final TagKey<EntityType<?>> EMERALD_AMULET_VALID = bind("amulet_valid/emerald");
    public static final TagKey<EntityType<?>> TOPAZ_AMULET_VALID = bind("amulet_valid/topaz");
    public static final TagKey<EntityType<?>> RUBY_AMULET_VALID = bind("amulet_valid/ruby");
    public static final TagKey<EntityType<?>> SAPPHIRE_AMULET_VALID = bind("amulet_valid/sapphire");
    public static final TagKey<EntityType<?>> ANVIL_AMULET_VALID = bind("amulet_valid/anvil");
    public static final TagKey<EntityType<?>> COMRADE_AMULET_VALID = bind("amulet_valid/comrade");
    public static final TagKey<EntityType<?>> FEATHER_AMULET_VALID = bind("amulet_valid/feather");
    public static final TagKey<EntityType<?>> CAT_AMULET_VALID = bind("amulet_valid/cat");
    public static final TagKey<EntityType<?>> DOG_AMULET_VALID = bind("amulet_valid/dog");
    public static final TagKey<EntityType<?>> SILENCE_AMULET_VALID = bind("amulet_valid/silence");
    public static final TagKey<EntityType<?>> COGWHEEL_AMULET_VALID = bind("amulet_valid/cogwheel");

    private static TagKey<EntityType<?>> bindC(String id) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", id));
    }

    private static TagKey<EntityType<?>> bind(String id) {
        return TagKey.create(Registries.ENTITY_TYPE, AnvilCraft.of(id));
    }
}
