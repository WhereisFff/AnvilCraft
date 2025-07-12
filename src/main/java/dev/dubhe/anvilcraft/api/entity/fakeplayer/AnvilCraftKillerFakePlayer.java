package dev.dubhe.anvilcraft.api.entity.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class AnvilCraftKillerFakePlayer {
    static final UUID killerUUID = UUID.randomUUID();
    static final String killerName = "AnvilCraftKiller";
    static final GameProfile fakeProfile = new GameProfile(killerUUID, "[Killer of " + killerName + "]");
    private static ServerPlayer fakePlayer;

    private static ItemStack DUMMY_LOOTING_5_WEAPON = null;

    public AnvilCraftKillerFakePlayer(ServerLevel world) {
        fakePlayer = FakePlayerFactory.get(world, fakeProfile);
    }

    public ServerPlayer getPlayer() {
        return fakePlayer;
    }

    public ItemStack getDummyLooting5Weapon(ServerLevel level) {
        if (DUMMY_LOOTING_5_WEAPON == null) {
            ItemStack weapon = Items.NETHERITE_SWORD.getDefaultInstance();
            weapon.set(DataComponents.CUSTOM_NAME, Component.literal("A Dummy Looting 5 Weapon"));
            level.holderLookup(Registries.ENCHANTMENT)
                .get(Enchantments.LOOTING)
                .ifPresent(e -> weapon.enchant(e, 5));
            DUMMY_LOOTING_5_WEAPON = weapon;
        }
        return DUMMY_LOOTING_5_WEAPON;
    }
}
