package dev.dubhe.anvilcraft.network.multiple;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.inventory.EnergyWeaponMakeMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class EnergyWeaponMakePackets {
    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
            Make.TYPE,
            Make.STREAM_CODEC,
            Make.HANDLER
        );
        registrar.playToServer(
            Select.TYPE,
            Select.STREAM_CODEC,
            Select.HANDLER
        );
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> of(String id) {
        return new CustomPacketPayload.Type<>(AnvilCraft.of("energy_weapon_" + id));
    }

    public record Make() implements CustomPacketPayload {
        public static final Type<Make> TYPE = EnergyWeaponMakePackets.of("make");
        public static final StreamCodec<ByteBuf, Make> STREAM_CODEC = StreamCodec.unit(new Make());
        public static final IPayloadHandler<Make> HANDLER = Make::serverHandler;

        @Override
        public Type<Make> type() {
            return TYPE;
        }

        public void serverHandler(IPayloadContext context) {
            ServerPlayer player = (ServerPlayer) context.player();
            context.enqueueWork(() -> {
                if (!player.hasContainerOpen()) return;
                if (!(player.containerMenu instanceof EnergyWeaponMakeMenu menu)) return;
                menu.make(player);
            });
        }
    }

    public record Select(int index) implements CustomPacketPayload {
        public static final Type<Select> TYPE = EnergyWeaponMakePackets.of("select");
        public static final StreamCodec<ByteBuf, Select> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            Select::index,
            Select::new
        );
        public static final IPayloadHandler<Select> HANDLER = Select::serverHandler;

        @Override
        public Type<Select> type() {
            return TYPE;
        }

        public void serverHandler(IPayloadContext context) {
            ServerPlayer player = (ServerPlayer) context.player();
            context.enqueueWork(() -> {
                if (!player.hasContainerOpen()) return;
                if (!(player.containerMenu instanceof EnergyWeaponMakeMenu menu)) return;
                menu.setSelectedIndex(this.index);
            });
        }
    }
}
