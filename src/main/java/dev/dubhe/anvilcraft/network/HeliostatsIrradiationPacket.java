package dev.dubhe.anvilcraft.network;

import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.ISensitiveBiPacket;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.entity.HeliostatsBlockEntity;
import dev.dubhe.anvilcraft.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public record HeliostatsIrradiationPacket(BlockPos pos, BlockPos irritatePos) implements ISensitiveBiPacket {
    public static final Type<HeliostatsIrradiationPacket> TYPE = IPacket.type(AnvilCraft.of("heliostats_irradiation_pack"));
    public static final StreamCodec<ByteBuf, HeliostatsIrradiationPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        HeliostatsIrradiationPacket::pos,
        BlockPos.STREAM_CODEC,
        HeliostatsIrradiationPacket::irritatePos,
        HeliostatsIrradiationPacket::new
    );

    @Override
    public Type<HeliostatsIrradiationPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (!(player.level().getBlockEntity(this.pos) instanceof HeliostatsBlockEntity heliostats)) return;
        heliostats.setIrritatePos(this.irritatePos);
    }

    @Override
    public void handleOnServer(Player player) {
        if (!(player.level().getBlockEntity(this.pos) instanceof HeliostatsBlockEntity heliostats)) return;
        PacketDistributor.sendToPlayer(
            Util.cast(player),
            new HeliostatsIrradiationPacket(this.pos, heliostats.getIrritatePos())
        );
    }
}
