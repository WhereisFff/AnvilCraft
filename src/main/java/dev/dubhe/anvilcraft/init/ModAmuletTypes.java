package dev.dubhe.anvilcraft.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import dev.dubhe.anvilcraft.item.abnormal.IAbnormal;
import dev.dubhe.anvilcraft.item.abnormal.ICursed;
import dev.dubhe.anvilcraft.item.abnormal.ILevitation;
import dev.dubhe.anvilcraft.item.abnormal.IRadiation;
import dev.dubhe.anvilcraft.item.abnormal.ISuperHeavy;
import dev.dubhe.anvilcraft.item.amulet.ComradeAmuletItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import static dev.dubhe.anvilcraft.init.ModDataAttachments.DISCOUNT_RATE;

public class ModAmuletTypes {
    private static final DeferredRegister<AmuletType> REGISTER = DeferredRegister.create(ModRegistries.AMULET_TYPE_KEY, AnvilCraft.MOD_ID);

    public static final DeferredHolder<AmuletType, ? extends AmuletType> EMERALD = register(
        "emerald",
        type -> AmuletType.builderAnc(type)
            .inventoryTick((player, amulet, isEnabled) -> {
                if (isEnabled) {
                    player.setData(DISCOUNT_RATE, 0.3f);
                } else {
                    player.removeData(DISCOUNT_RATE);
                }
            })
            .amulet(ModItems.EMERALD_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> TOPAZ = register(
        "topaz",
        type -> AmuletType.builderAnc(type)
            .immuneDamageFromObtain()
            .amulet(ModItems.TOPAZ_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> RUBY = register(
        "ruby",
        type -> AmuletType.builderAnc(type)
            .inventoryTick((player, amulet, isEnabled) -> {
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
            })
            .amulet(ModItems.RUBY_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> SAPPHIRE = register(
        "sapphire",
        type -> AmuletType.builderAnc(type)
            .inventoryTick((player, amulet, isEnabled) -> {
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
            })
            .amulet(ModItems.SAPPHIRE_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> ANVIL = register(
        "anvil",
        type -> AmuletType.builderAnc(type)
            .obtain(builder -> builder
                .weapon(ModItemTags.ANVIL_HAMMER)
                .buildAndSub())
            .amulet(ModItems.ANVIL_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> COMRADE = register(
        "comrade",
        type -> AmuletType.builderAnc(type)
            .obtain(builder -> builder
                .isSameTeam(true)
                .buildAndSub())
            .inventoryTick(ComradeAmuletItem::inventoryTick)
            .immuneDamage(ComradeAmuletItem::shouldImmuneDamage)
            .amulet(ModItems.COMRADE_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> FEATHER = register(
        "feather",
        type -> AmuletType.builderAnc(type)
            .immuneDamageFromObtain()
            .amulet(ModItems.FEATHER_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> CAT = register(
        "cat",
        type -> AmuletType.builderAnc(type)
            .inventoryTick((player, amulet, isEnabled) -> {
                CompoundTag root = player.getData(ModDataAttachments.SCARE_ENTITIES);
                root.putBoolean("creepers", isEnabled);
                root.putBoolean("phantoms", isEnabled);
                player.setData(ModDataAttachments.SCARE_ENTITIES, root);
            })
            .amulet(ModItems.CAT_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> DOG = register(
        "dog",
        type -> AmuletType.builderAnc(type)
            .inventoryTick((player, amulet, isEnabled) -> {
                CompoundTag root = player.getData(ModDataAttachments.SCARE_ENTITIES);
                root.putBoolean("skeletons", isEnabled);
                player.setData(ModDataAttachments.SCARE_ENTITIES, root);
            })
            .amulet(ModItems.DOG_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> SILENCE = register(
        "silence",
        type -> AmuletType.builderAnc(type)
            .amulet(ModItems.SILENCE_AMULET)
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> ABNORMAL = register(
        "abnormal",
        type -> AmuletType.builderAnc(type)
            .obtainOr(ModAmuletTypes::obtainAbnormal)
            .amulet(ModItems.ABNORMAL_AMULET)
    );

    private static boolean obtainAbnormal(ServerPlayer player, DamageSource source) {
        if (!source.is(DamageTypes.WITHER)) return false;
        if (IAbnormal.getAbnormalCount(player, ICursed.class) < 1) return false;
        if (IAbnormal.getAbnormalCount(player, ILevitation.class) < 64) return false;
        if (IAbnormal.getAbnormalCount(player, ISuperHeavy.class) < 1) return false;
        if (IAbnormal.getAbnormalCount(player, IRadiation.class) < 1152) return false;
        return true;
    }

    private static DeferredHolder<AmuletType, ? extends AmuletType> register(String typeId, Function<String, AmuletType.Builder> builder) {
        return REGISTER.register(typeId, builder.apply(typeId)::build);
    }

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
