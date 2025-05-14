package dev.dubhe.anvilcraft.init;

import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import dev.dubhe.anvilcraft.api.amulet.SimpleAmuletType;
import dev.dubhe.anvilcraft.item.amulet.ComradeAmuletItem;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import dev.dubhe.anvilcraft.util.predicate.DamageSourcePredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ModAmuletTypes {
    private static final DeferredRegister<AmuletType> REGISTER = DeferredRegister.create(ModRegistries.AMULET_TYPE_KEY, AnvilCraft.MOD_ID);

    public static final DeferredHolder<AmuletType, SimpleAmuletType> EMERALD = register(
        "emerald",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.EMERALD_AMULET_VALID)
            .murder(ModEntityTypeTags.EMERALD_AMULET_VALID)
            .build(),
        ModItems.EMERALD_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> TOPAZ = register(
        "topaz",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.TOPAZ_AMULET_VALID)
            .murder(ModEntityTypeTags.TOPAZ_AMULET_VALID)
            .build(),
        ModItems.TOPAZ_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> RUBY = register(
        "ruby",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.RUBY_AMULET_VALID)
            .murder(ModEntityTypeTags.RUBY_AMULET_VALID)
            .build(),
        ModItems.RUBY_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> SAPPHIRE = register(
        "sapphire",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.SAPPHIRE_AMULET_VALID)
            .murder(ModEntityTypeTags.SAPPHIRE_AMULET_VALID)
            .build(),
        ModItems.SAPPHIRE_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> ANVIL = register(
        "anvil",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.ANVIL_AMULET_VALID)
            .murder(ModEntityTypeTags.ANVIL_AMULET_VALID)
            .weapon(ModItemTags.ANVIL_HAMMER)
            //.buildAndSub()
            //.and()
            //.type(DamageTypes.FALLING_BLOCK)
            //.murder(ModEntities.FALLING_GIANT_ANVIL)
            .build(),
        ModItems.ANVIL_AMULET
    );
    public static final DeferredHolder<AmuletType, ? extends AmuletType> COMRADE = REGISTER.register(
        "comrade", () -> new AmuletType(
            DamageSourcePredicate.Builder.builder()
                .type(ModDamageTypeTags.COMRADE_AMULET_VALID)
                .murder(ModEntityTypeTags.COMRADE_AMULET_VALID)
                .isSameTeam(true)
                .build().build(),
            ModItems.COMRADE_AMULET.asStack()
        ) {
            @Override
            public @NotNull Codec<? extends AmuletType> codec() {
                return SimpleAmuletType.CODEC;
            }

            @Override
            public boolean shouldIgnoreDamage(ServerPlayer player, DamageSource source) {
                ItemStack comrade = InventoryUtil.getFirstItem(player.getInventory(), ModItems.COMRADE_AMULET);
                UUID murderUUID = Objects.requireNonNull(source.getEntity()).getUUID();
                return !comrade.isEmpty() && ComradeAmuletItem.canIgnorePlayer(comrade, murderUUID);
            }
        }
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> FEATHER = register(
        "feather",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.FEATHER_AMULET_VALID)
            .murder(ModEntityTypeTags.FEATHER_AMULET_VALID)
            .build(),
        ModItems.FEATHER_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> CAT = register(
        "cat",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.CAT_AMULET_VALID)
            .murder(ModEntityTypeTags.CAT_AMULET_VALID)
            .build(),
        ModItems.CAT_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> DOG = register(
        "dog",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.DOG_AMULET_VALID)
            .murder(ModEntityTypeTags.DOG_AMULET_VALID)
            .build(),
        ModItems.DOG_AMULET
    );
    public static final DeferredHolder<AmuletType, SimpleAmuletType> SILENCE = register(
        "silence",
        DamageSourcePredicate.Builder.builder()
            .type(ModDamageTypeTags.SILENCE_AMULET_VALID)
            .murder(ModEntityTypeTags.SILENCE_AMULET_VALID)
            .build(),
        ModItems.SILENCE_AMULET
    );

    private static DeferredHolder<AmuletType, SimpleAmuletType> register(
        String typeId, DamageSourcePredicate.Builder builder, ItemLike itemLike
    ) {
        return REGISTER.register(typeId, () -> new SimpleAmuletType(
            builder.sub().victim(EntityType.PLAYER).build().build(),
            itemLike.asItem().getDefaultInstance()
        ));
    }

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
