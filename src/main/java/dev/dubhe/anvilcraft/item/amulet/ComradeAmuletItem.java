package dev.dubhe.anvilcraft.item.amulet;

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.api.amulet.AmuletType;
import dev.dubhe.anvilcraft.init.ModAmuletTypes;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComradeAmuletItem extends AmuletItem {
    public ComradeAmuletItem(Properties properties) {
        super(properties.component(ModComponents.SIGNED_PLAYERS, SignedPlayers.EMPTY));
    }

    @Override
    public Holder<AmuletType> getType() {
        return ModAmuletTypes.COMRADE;
    }

    @SuppressWarnings("unused")
    public static void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled) {
        amulet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, !getSignedPlayers(amulet).isEmpty());
    }

    public static boolean shouldImmuneDamage(ServerPlayer player, DamageSource source) {
        ItemStack comrade = InventoryUtil.getFirstItem(player.getInventory(), ModItems.COMRADE_AMULET);
        return Optional.ofNullable(source.getEntity())
            .map(Entity::getUUID)
            .filter(uuid -> !comrade.isEmpty() && canIgnorePlayer(comrade, uuid))
            .isPresent();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack amulet = player.getItemInHand(usedHand);

        if (registerPlayerToAmulet(amulet, player)) {
            return InteractionResultHolder.success(amulet);
        } else {
            return InteractionResultHolder.pass(amulet);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.anvilcraft.comrade_amulet.tooltip").withStyle(ChatFormatting.GRAY));

        HashBiMap<String, UUID> signedPlayers = getSignedPlayers(stack);
        for (String playerName : signedPlayers.keySet()) {
            tooltipComponents.add(Component.literal("- " + playerName));
        }
    }

    public static boolean registerPlayerToAmulet(ItemStack amulet, Player player) {
        try {
            HashBiMap<String, UUID> signedPlayers = getSignedPlayers(amulet);
            signedPlayers.put(player.getName().getString(), player.getUUID());
            amulet.set(ModComponents.SIGNED_PLAYERS, new SignedPlayers(signedPlayers));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean canIgnorePlayer(ItemStack amulet, UUID playerUUID) {
        return getSignedPlayers(amulet).containsValue(playerUUID);
    }

    public static HashBiMap<String, UUID> getSignedPlayers(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.SIGNED_PLAYERS))
            .map(signedPlayers -> HashBiMap.create(signedPlayers.playerInfos()))
            .orElse(HashBiMap.create());
    }

    public record SignedPlayers(Map<String, UUID> playerInfos) {
        public static final SignedPlayers EMPTY = new SignedPlayers(new HashMap<>());
        public static final Codec<SignedPlayers> CODEC = Codec.unboundedMap(
            Codec.STRING, UUIDUtil.CODEC
        ).xmap(SignedPlayers::new, SignedPlayers::playerInfos);

        public static final StreamCodec<RegistryFriendlyByteBuf, SignedPlayers> STREAM_CODEC = StreamCodec.of(
            SignedPlayers::encode,
            SignedPlayers::decode
        );

        private static void encode(FriendlyByteBuf buf, SignedPlayers value) {
            buf.writeMap(value.playerInfos(), ByteBufCodecs.STRING_UTF8, UUIDUtil.STREAM_CODEC);
        }

        private static SignedPlayers decode(FriendlyByteBuf buf) {
            return new SignedPlayers(buf.readMap(ByteBufCodecs.STRING_UTF8, UUIDUtil.STREAM_CODEC));
        }
    }
}
