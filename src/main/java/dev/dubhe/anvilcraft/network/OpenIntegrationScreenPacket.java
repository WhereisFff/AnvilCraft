package dev.dubhe.anvilcraft.network;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.integration.IntegrationUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record OpenIntegrationScreenPacket() implements IClientboundPacket {
    public static final Type<OpenIntegrationScreenPacket> TYPE = IPacket.type(AnvilCraft.of("open_integration_screen"));
    public static final StreamCodec<ByteBuf, OpenIntegrationScreenPacket> STREAM_CODEC = StreamCodec.unit(
        new OpenIntegrationScreenPacket()
    );

    @Override
    public Type<OpenIntegrationScreenPacket> type() {
        return OpenIntegrationScreenPacket.TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        IntegrationUtil.openIntegrationScreen();
    }
}
