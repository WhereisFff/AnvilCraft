package dev.dubhe.anvilcraft.network;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.client.gui.screen.ResonatorScreen;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItemTags;
import dev.dubhe.anvilcraft.item.ResonatorItem;
import dev.dubhe.anvilcraft.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public record SwitchResonateModePacket(InteractionHand hand, int mode) implements CustomPacketPayload {
    public static final Type<SwitchResonateModePacket> TYPE = new Type<>(AnvilCraft.of("switch_resonate_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwitchResonateModePacket> STREAM_CODEC =
        CustomPacketPayload.codec(SwitchResonateModePacket::encode, SwitchResonateModePacket::decode);
    public static final IPayloadHandler<SwitchResonateModePacket> HANDLER = new DirectionalPayloadHandler<>(
        SwitchResonateModePacket::clientHandler, SwitchResonateModePacket::serverHandler
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void encode(@NotNull RegistryFriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeVarInt(this.mode);
    }

    public static SwitchResonateModePacket decode(@NotNull RegistryFriendlyByteBuf buf) {
        return new SwitchResonateModePacket(buf.readEnum(InteractionHand.class), buf.readVarInt());
    }

    public static void clientHandler(SwitchResonateModePacket data, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ResonatorScreen(data.hand, data.mode)));
    }

    public static void serverHandler(SwitchResonateModePacket data, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            if (data.mode == -2) {
                PlayerUtil.getHand(player, stack -> stack.is(ModItemTags.RESONATOR))
                    .ifPresent(hand -> PacketDistributor.sendToPlayer(
                        player,
                        new SwitchResonateModePacket(
                            hand, player.getItemInHand(hand).getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.DEFAULT).value()
                        )
                    ));
            } else {
                ResonatorItem.set((ServerPlayer) context.player(), data.hand, data.mode);
            }
        });
    }
}
