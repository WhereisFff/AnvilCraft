package dev.dubhe.anvilcraft.network;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.item.AnvilHammerItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public record ChangeEnchantmentSpacePacket() implements CustomPacketPayload {
    public static final Type<ChangeEnchantmentSpacePacket> TYPE = new Type<>(AnvilCraft.of("change_enchantment_space"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeEnchantmentSpacePacket> STREAM_CODEC =
        CustomPacketPayload.codec(ChangeEnchantmentSpacePacket::encode, ChangeEnchantmentSpacePacket::decode);
    public static final IPayloadHandler<ChangeEnchantmentSpacePacket> HANDLER = ChangeEnchantmentSpacePacket::serverHandler;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void encode(@NotNull RegistryFriendlyByteBuf buf) {}

    public static ChangeEnchantmentSpacePacket decode(@NotNull RegistryFriendlyByteBuf buf) {
        return new ChangeEnchantmentSpacePacket();
    }

    public static void serverHandler(ChangeEnchantmentSpacePacket data, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            if (!(player.level() instanceof ServerLevel)) return;
            ItemStack itemInHand = Optional.of(player.getMainHandItem()).filter(stack -> stack.get(ModComponents.MORPH) != null)
                .or(() -> Optional.of(player.getOffhandItem()).filter(stack -> stack.get(ModComponents.MORPH) != null))
                .orElse(ItemStack.EMPTY);
            if (itemInHand.isEmpty()) return;
            try {
                Objects.requireNonNull(itemInHand.get(ModComponents.MORPH)).switchSpaces();
            } catch (NullPointerException ignored) {}
        });
    }
}
