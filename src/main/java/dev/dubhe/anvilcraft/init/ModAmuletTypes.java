package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import dev.dubhe.anvilcraft.item.amulet.ComradeAmuletItem;
import dev.dubhe.anvilcraft.util.predicate.DamageSourcePredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

import static dev.dubhe.anvilcraft.init.ModDataAttachments.DISCOUNT_RATE;

public class ModAmuletTypes {
    private static final DeferredRegister<AmuletType> REGISTER = DeferredRegister.create(ModRegistries.AMULET_TYPE_KEY, AnvilCraft.MOD_ID);

    public static final DeferredHolder<AmuletType, ? extends AmuletType> EMERALD = register(
        "emerald", (player, amulet, isEnabled) -> {
            if (isEnabled) {
                player.setData(DISCOUNT_RATE, 0.3f);
            } else {
                player.removeData(DISCOUNT_RATE);
            }
        },
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.EMERALD_AMULET_VALID)
            .murder(ModEntityTypeTags.EMERALD_AMULET_VALID)
            .build(),
        ModItems.EMERALD_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> TOPAZ = registerImmuneDamageFromObtain(
        "topaz",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.TOPAZ_AMULET_VALID)
            .murder(ModEntityTypeTags.TOPAZ_AMULET_VALID)
            .build(),
        ModItems.TOPAZ_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> RUBY = register(
        "ruby", (player, amulet, isEnabled) -> {
            if (isEnabled && !player.isInLava()) {
                MobEffectInstance effect = player.getEffect(MobEffects.FIRE_RESISTANCE);
                if (effect == null) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        2, 0, false, false
                    ));
                } else if (effect.getDuration() < 3600) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        effect.getDuration() + 2, effect.getAmplifier(),
                        effect.isAmbient(), effect.isVisible()
                    ));
                }
            }
        },
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.RUBY_AMULET_VALID)
            .murder(ModEntityTypeTags.RUBY_AMULET_VALID)
            .build(),
        ModItems.RUBY_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> SAPPHIRE = register(
        "sapphire", (player, amulet, isEnabled) -> {
            if (isEnabled && !player.isInWater()) {
                MobEffectInstance effect = player.getEffect(MobEffects.CONDUIT_POWER);
                if (effect == null) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.CONDUIT_POWER,
                        2, 0, false, false
                    ));
                } else if (effect.getDuration() < 3600) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.CONDUIT_POWER,
                        effect.getDuration() + 2, effect.getAmplifier(),
                        effect.isAmbient(), effect.isVisible()
                    ));
                }
            }
        },
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.SAPPHIRE_AMULET_VALID)
            .murder(ModEntityTypeTags.SAPPHIRE_AMULET_VALID)
            .build(),
        ModItems.SAPPHIRE_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> ANVIL = registerImmuneDamageFromObtain(
        "anvil",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.ANVIL_AMULET_VALID)
            .murder(ModEntityTypeTags.ANVIL_AMULET_VALID)
            .weapon(ModItemTags.ANVIL_HAMMER)
            .build(),
        ModItems.ANVIL_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> COMRADE = register(
        "comrade",
        ComradeAmuletItem::inventoryTick,
        ComradeAmuletItem::shouldIgnoreDamage,
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.COMRADE_AMULET_VALID)
            .murder(ModEntityTypeTags.COMRADE_AMULET_VALID)
            .isSameTeam(true)
            .build(),
        ModItems.COMRADE_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> FEATHER = registerImmuneDamageFromObtain(
        "feather",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.FEATHER_AMULET_VALID)
            .murder(ModEntityTypeTags.FEATHER_AMULET_VALID)
            .build(),
        ModItems.FEATHER_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> CAT = register(
        "cat", (player, amulet, isEnabled) -> {
            CompoundTag root = player.getData(ModDataAttachments.SCARE_ENTITIES);
            root.putBoolean("creepers", isEnabled);
            root.putBoolean("phantoms", isEnabled);
            player.setData(ModDataAttachments.SCARE_ENTITIES, root);
        },
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.CAT_AMULET_VALID)
            .murder(ModEntityTypeTags.CAT_AMULET_VALID)
            .build(),
        ModItems.CAT_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> DOG = register(
        "dog", (player, amulet, isEnabled) -> {
            CompoundTag root = player.getData(ModDataAttachments.SCARE_ENTITIES);
            root.putBoolean("skeletons", isEnabled);
            player.setData(ModDataAttachments.SCARE_ENTITIES, root);
        },
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.DOG_AMULET_VALID)
            .murder(ModEntityTypeTags.DOG_AMULET_VALID)
            .build(),
        ModItems.DOG_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> SILENCE = registerSimple(
        "silence",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.SILENCE_AMULET_VALID)
            .murder(ModEntityTypeTags.SILENCE_AMULET_VALID)
            .build(),
        ModItems.SILENCE_AMULET
    );

    private static DeferredHolder<AmuletType, ? extends AmuletType> registerSimple(
        String typeId, DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        return REGISTER.register(typeId, () -> new AmuletType.Simple(
            builder.and().sub().victim(EntityType.PLAYER).build().build(),
            itemLike.asItem().getDefaultInstance()
        ));
    }

    private static DeferredHolder<AmuletType, ? extends AmuletType> registerImmuneDamageFromObtain(
        String typeId,
        DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        return REGISTER.register(typeId, () -> new AmuletType.ImmuneDamageFromObtain(
            builder.and().sub().victim(EntityType.PLAYER).build().build(),
            itemLike.asItem().getDefaultInstance()
        ));
    }

    private static DeferredHolder<AmuletType, ? extends AmuletType> registerImmuneDamageFromObtain(
        String typeId,
        TriConsumer<ServerPlayer, ItemStack, Boolean> inventoryTick,
        DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        return REGISTER.register(typeId, () -> new AmuletType.ImmuneDamageFromObtain(
            builder.and().sub().victim(EntityType.PLAYER).build().build(),
            itemLike.asItem().getDefaultInstance()
        ) {
            @Override
            public void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled) {
                inventoryTick.accept(player, amulet, isEnabled);
            }
        });
    }

    private static DeferredHolder<AmuletType, ? extends AmuletType> register(
        String typeId,
        TriConsumer<ServerPlayer, ItemStack, Boolean> inventoryTick,
        DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        return register(typeId, inventoryTick, null, builder, itemLike);
    }

    private static DeferredHolder<AmuletType, ? extends AmuletType> register(
        String typeId,
        BiPredicate<ServerPlayer, DamageSource> shouldIgnoreDamage,
        DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        return register(typeId, null, shouldIgnoreDamage, builder, itemLike);
    }

    private static DeferredHolder<AmuletType, ? extends AmuletType> register(
        String typeId,
        @Nullable TriConsumer<ServerPlayer, ItemStack, Boolean> inventoryTick,
        @Nullable BiPredicate<ServerPlayer, DamageSource> shouldIgnoreDamage,
        DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        if (inventoryTick == null) {
            inventoryTick = (player, amulet, isEnabled) -> {};
        }
        if (shouldIgnoreDamage == null) {
            shouldIgnoreDamage = (player, source) -> false;
        }
        TriConsumer<ServerPlayer, ItemStack, Boolean> finalInventoryTick = inventoryTick;
        BiPredicate<ServerPlayer, DamageSource> finalShouldIgnoreDamage = shouldIgnoreDamage;
        return REGISTER.register(typeId, () -> new AmuletType.Simple(
            builder.and().sub().victim(EntityType.PLAYER).build().build(),
            itemLike.asItem().getDefaultInstance()
        ) {
            @Override
            public void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled) {
                finalInventoryTick.accept(player, amulet, isEnabled);
            }

            @Override
            public boolean shouldImmuneDamage(ServerPlayer player, DamageSource source) {
                return finalShouldIgnoreDamage.test(player, source);
            }
        });
    }

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
