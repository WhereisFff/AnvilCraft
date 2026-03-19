package dev.dubhe.anvilcraft.network;

import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import dev.anvilcraft.lib.v2.recipe.util.CodecUtil;
import dev.dubhe.anvilcraft.AnvilCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record HammerChangeBlockPacket(BlockPos pos, BlockState state) implements IServerboundPacket {
    public static final Type<HammerChangeBlockPacket> TYPE = IPacket.type(AnvilCraft.of("hammer_change_block"));
    public static final StreamCodec<ByteBuf, HammerChangeBlockPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        HammerChangeBlockPacket::pos,
        CodecUtil.BLOCK_STATE_STREAM_CODEC,
        HammerChangeBlockPacket::state,
        HammerChangeBlockPacket::new
    );

    @Override
    public Type<HammerChangeBlockPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(Player player) {
        Level level = player.level();
        if (!level.isLoaded(this.pos)) return;
        level.setBlock(this.pos, this.state, Block.UPDATE_ALL_IMMEDIATE);
    }
}
