package dev.dubhe.anvilcraft.api.amulet;

import com.google.common.collect.Sets;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.dubhe.anvilcraft.init.ModDataAttachments;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.init.ModRegistries;
import dev.dubhe.anvilcraft.item.amulet.AbstractAmuletItem;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class AmuletManager {
    public static final AmuletManager INSTANCE = new AmuletManager();
    public final Set<Function<Player, Optional<Holder<AmuletType>>>> typeFinders = Sets.newConcurrentHashSet();

    private AmuletManager() {
        this.registerFinder(
            player -> {
                ItemStack firstAmulet = InventoryUtil.getFirstItem(player.getInventory(), stack -> stack.is(ModItemTags.AMULET));
                if (firstAmulet.getItem() instanceof AbstractAmuletItem amulet) {
                    return Optional.of(amulet.getType());
                } else {
                    return Optional.empty();
                }
            }
        );
    }

    public void registerFinder(Function<Player, Optional<Holder<AmuletType>>> typeFinder) {
        this.typeFinders.add(typeFinder);
    }

    public Optional<Holder<AmuletType>> getType(Player player) {
        for (Function<Player, Optional<Holder<AmuletType>>> typeFinder : this.typeFinders) {
            Optional<Holder<AmuletType>> amuletType = typeFinder.apply(player);
            if (amuletType.isPresent()) return amuletType;
        }
        return Optional.empty();
    }

    public Optional<Holder<AmuletType>> getType(ServerPlayer player, DamageSource source, HolderLookup.Provider registryAccess) {
        Optional<HolderLookup.RegistryLookup<AmuletType>> lookupOptional = registryAccess.lookup(ModRegistries.AMULET_TYPE_KEY);
        return lookupOptional.flatMap(lookup -> lookup.listElements()
            .filter(reference -> reference.value().matchesByDamage(player, source))
            .findFirst());
    }

    public void startRaffle(ServerPlayer player, DamageSource source, boolean isConsumedInBox) {
        RandomSource random = player.getRandom();
        int raffleProbability = Math.min(this.getRaffleProbability(player, source, isConsumedInBox), 100);

        if (raffleProbability > random.nextIntBetweenInclusive(0, 100)) {
            Optional<AmuletType> type = this.getType(player, source, player.registryAccess()).map(Holder::value);
            type.ifPresent(amuletType -> InventoryUtil.addToInventory(player.getInventory(), amuletType.amulet()));

            this.setRaffleProbability(player, source, value -> 20);
        } else {
            this.setRaffleProbability(
                player, source,
                value -> Math.min(value + (isConsumedInBox ? 10 : 5), 100)
            );
        }
    }

    public static int getStoredRaffleProbability(Player player, Holder<AmuletType> type) {
        ResourceKey<AmuletType> typeKey = type.getKey();
        if (typeKey == null) return 0;
        return player.getData(ModDataAttachments.AMULET_RAFFLE_PROBABILITY).getInt(typeKey.location().toString());
    }

    public int getRaffleProbability(Player player, DamageSource source, boolean isConsumedInBox) {
        Optional<Holder<AmuletType>> type = Optional.empty();
        if (player instanceof ServerPlayer serverPlayer) {
            type = this.getType(serverPlayer, source, serverPlayer.registryAccess());
        }
        return type.map(holder -> this.getRaffleProbability(player, holder, isConsumedInBox)).orElse(0);
    }

    public int getRaffleProbability(Player player, Holder<AmuletType> type, boolean isConsumedInBox) {
        if (!this.hasAmuletInInventory(player, type)) {
            return getStoredRaffleProbability(player, type) + (isConsumedInBox ? 20 : 5);
        } else {
            return 0;
        }
    }

    public void setRaffleProbability(ServerPlayer player, DamageSource source, NonNullUnaryOperator<Integer> modifier) {
        Optional<Holder<AmuletType>> typeHolder = this.getType(player, source, player.registryAccess());
        typeHolder.ifPresent(damageTypeHolder -> this.setRaffleProbability(player, damageTypeHolder, modifier));
    }

    public void setRaffleProbability(ServerPlayer player, Holder<AmuletType> type, NonNullUnaryOperator<Integer> modifier) {
        CompoundTag root = player.getData(ModDataAttachments.AMULET_RAFFLE_PROBABILITY);
        String key = Objects.requireNonNull(type.getKey()).location().toString();
        if (!this.hasAmuletInInventory(player, type)) {
            root.putInt(key, modifier.apply(root.getInt(key)));
        } else {
            root.putInt(key, 0);
        }
    }

    public boolean hasAmuletInInventory(Player player, ItemLike itemLike) {
        Optional<Holder<AmuletType>> typeOptional = this.getType(player);
        return typeOptional.isPresent() && typeOptional.get().value().matchesByItem(itemLike);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasAmuletInInventory(Player player, Holder<AmuletType> type) {
        Optional<Holder<AmuletType>> typeOptional = this.getType(player);
        return typeOptional.isPresent() && typeOptional.get().is(Objects.requireNonNull(type.getKey()));
    }

    public boolean shouldIgnoreDamage(ServerPlayer player, DamageSource source) {
        Optional<Holder<AmuletType>> type = this.getType(player);
        return type.map(amuletTypeHolder -> amuletTypeHolder.value().shouldIgnoreDamage(player, source))
            .orElse(false);
    }
}
