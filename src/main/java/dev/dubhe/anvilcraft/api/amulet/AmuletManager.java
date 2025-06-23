package dev.dubhe.anvilcraft.api.amulet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.dubhe.anvilcraft.api.item.property.BoxContents;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModDataAttachments;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModRegistries;
import dev.dubhe.anvilcraft.item.amulet.AmuletItem;
import dev.dubhe.anvilcraft.util.CollectionUtil;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AmuletManager {
    public static final AmuletManager INSTANCE = new AmuletManager();
    private final Set<Supplier<AmuletItem>> amuletItems = Sets.newConcurrentHashSet();
    private final List<BiConsumer<Player, List<ItemStack>>> amuletFinders = new ArrayList<>();

    private AmuletManager() {
        this.registerAmulets(
            () -> (AmuletItem) ModItems.EMERALD_AMULET.asItem(),
            () -> (AmuletItem) ModItems.TOPAZ_AMULET.asItem(),
            () -> (AmuletItem) ModItems.RUBY_AMULET.asItem(),
            () -> (AmuletItem) ModItems.SAPPHIRE_AMULET.asItem(),
            () -> (AmuletItem) ModItems.ANVIL_AMULET.asItem(),
            () -> (AmuletItem) ModItems.COMRADE_AMULET.asItem(),
            () -> (AmuletItem) ModItems.FEATHER_AMULET.asItem(),
            () -> (AmuletItem) ModItems.CAT_AMULET.asItem(),
            () -> (AmuletItem) ModItems.DOG_AMULET.asItem(),
            () -> (AmuletItem) ModItems.SILENCE_AMULET.asItem()
        );
        this.registerFinders(
            (player, holders) -> processFoundStack(player.getWeaponItem(), holders),
            (player, holders) -> processFoundStack(player.getOffhandItem(), holders)
        );
    }

    @SafeVarargs
    public final void registerAmulets(Supplier<AmuletItem>... amuletItems) {
        Collections.addAll(this.amuletItems, amuletItems);
    }

    @SafeVarargs
    public final void registerFinders(BiConsumer<Player, List<ItemStack>>... typeFinders) {
        Collections.addAll(this.amuletFinders, typeFinders);
    }

    public static void processFoundStack(ItemStack found, List<ItemStack> holders) {
        if (found.is(ModItems.AMULET_BOX)) {
            BoxContents contents = found.get(ModComponents.BOX_CONTENTS);
            if (contents == null) return;
            for (ItemStack stack : contents.getAmulets()) {
                if (stack.getItem() instanceof AmuletItem) {
                    holders.add(stack.copy());
                }
            }
        } else if (found.getItem() instanceof AmuletItem) {
            holders.add(found);
        }
    }

    public HashMultimap<Holder<AmuletType>, ItemStack> getAmuletsFromInventory(Player player) {
        List<ItemStack> amuletItems = new ArrayList<>();
        for (BiConsumer<Player, List<ItemStack>> amuletFinder : this.amuletFinders) {
            amuletFinder.accept(player, amuletItems);
        }
        return CollectionUtil.newMultimap(
            HashMultimap.create(), amuletItems, stack -> ((AmuletItem) stack.getItem()).getType()
        );
    }

    public List<Holder<AmuletType>> getTypesFromInventory(Player player) {
        return List.copyOf(this.getAmuletsFromInventory(player).keySet());
    }

    public Optional<Holder<AmuletType>> getTypeMatchedDamage(ServerPlayer player, DamageSource source, HolderLookup.Provider registryAccess) {
        Optional<HolderLookup.RegistryLookup<AmuletType>> lookupOptional = registryAccess.lookup(ModRegistries.AMULET_TYPE_KEY);
        return lookupOptional.flatMap(lookup -> lookup.listElements()
            .filter(reference -> reference.value().matchesByDamage(player, source))
            .findFirst());
    }

    public void startRaffle(ServerPlayer player, DamageSource source, boolean isConsumedInBox) {
        RandomSource random = player.getRandom();
        int raffleProbability = Math.min(this.getRaffleProbability(player, source, isConsumedInBox), 100);

        if (raffleProbability > random.nextIntBetweenInclusive(0, 100)) {
            Optional<AmuletType> type = this.getTypeMatchedDamage(player, source, player.registryAccess()).map(Holder::value);
            type.ifPresent(amuletType -> player.getInventory().placeItemBackInInventory(amuletType.amulet()));

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
            type = this.getTypeMatchedDamage(serverPlayer, source, serverPlayer.registryAccess());
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
        Optional<Holder<AmuletType>> typeHolder = this.getTypeMatchedDamage(player, source, player.registryAccess());
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
        List<Holder<AmuletType>> holders = this.getTypesFromInventory(player);
        return !holders.isEmpty() && CollectionUtil.anyMatch(holders, holder -> holder.value().matchesByItem(itemLike));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasAmuletInInventory(Player player, Holder<AmuletType> type) {
        List<Holder<AmuletType>> holders = this.getTypesFromInventory(player);
        return !holders.isEmpty() && CollectionUtil.anyMatch(holders, holder -> holder.equals(type));
    }

    public void inventoryTick(ServerPlayer player) {
        HashMultimap<Holder<AmuletType>, ItemStack> amulets = this.getAmuletsFromInventory(player);
        for (Supplier<AmuletItem> amuletGetter : this.amuletItems) {
            AmuletItem amuletItem = amuletGetter.get();
            Holder<AmuletType> type = amuletItem.getType();
            if (amulets.containsKey(type)) {
                for (ItemStack amulet : amulets.get(type)) {
                    type.value().inventoryTick(player, amulet, true);
                }
            } else {
                type.value().inventoryTick(player, type.value().amulet(), false);
            }
        }
    }

    public boolean shouldIgnoreDamage(ServerPlayer player, DamageSource source) {
        List<Holder<AmuletType>> holders = this.getTypesFromInventory(player);
        return !holders.isEmpty() && CollectionUtil.anyMatch(holders, holder -> holder.value().shouldImmuneDamage(player, source));
    }
}
