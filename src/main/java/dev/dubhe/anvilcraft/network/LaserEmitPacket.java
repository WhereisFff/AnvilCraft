package dev.dubhe.anvilcraft.network;

import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.entity.BaseLaserBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record LaserEmitPacket(int level, BlockPos laserPos, BlockPos irradiatePos) implements IClientboundPacket {
    public static final Type<LaserEmitPacket> TYPE = IPacket.type(AnvilCraft.of("laser_emit"));
    public static final StreamCodec<ByteBuf, LaserEmitPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        LaserEmitPacket::level,
        BlockPos.STREAM_CODEC,
        LaserEmitPacket::laserPos,
        BlockPos.STREAM_CODEC,
        LaserEmitPacket::irradiatePos,
        LaserEmitPacket::new
    );

    @Override
    public Type<LaserEmitPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (!(player.level().getBlockEntity(this.laserPos) instanceof BaseLaserBlockEntity laser)) return;
        laser.clientUpdate(this.irradiatePos, this.level);
        Minecraft.getInstance().levelRenderer.setBlockDirty(this.laserPos, false);
    }
}
