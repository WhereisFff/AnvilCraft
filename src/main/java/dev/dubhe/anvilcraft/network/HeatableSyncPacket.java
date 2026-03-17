package dev.dubhe.anvilcraft.network;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record HeatableSyncPacket(BlockPos pos, int duration) implements IClientboundPacket {
    public static final Type<HeatableSyncPacket> TYPE = IPacket.type(AnvilCraft.of("heatable_sync"));
    public static final StreamCodec<ByteBuf, HeatableSyncPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        HeatableSyncPacket::pos,
        ByteBufCodecs.VAR_INT,
        HeatableSyncPacket::duration,
        HeatableSyncPacket::new
    );

    @Override
    public Type<HeatableSyncPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (!(player.level().getBlockEntity(this.pos) instanceof HeatableBlockEntity heatable)) return;
        heatable.setDuration(this.duration);
    }
}
