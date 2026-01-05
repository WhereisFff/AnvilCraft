package dev.dubhe.anvilcraft.network.multiple;

import dev.anvilcraft.lib.util.CodecUtil;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.inventory.FrostSmithingMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

public class FrostSmithingPackets {
    public static void register(PayloadRegistrar registrar) {

    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> of(String path) {
        return new CustomPacketPayload.Type<>(AnvilCraft.of("frost_smithing_" + path));
    }

    public record OriginalSync(int selected, List<Item> results) implements CustomPacketPayload {
        public static final Type<OriginalSync> TYPE = FrostSmithingPackets.of("sync");
        public static final StreamCodec<RegistryFriendlyByteBuf, OriginalSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            OriginalSync::selected,
            CodecUtil.ITEM_STREAM_CODEC.apply(ByteBufCodecs.list()),
            OriginalSync::results,
            OriginalSync::new
        );
        public static final IPayloadHandler<OriginalSync> HANDLER = OriginalSync::clientHandler;

        @Override
        public Type<OriginalSync> type() {
            return TYPE;
        }

        public void clientHandler(IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!(ctx.player().containerMenu instanceof FrostSmithingMenu menu)) return;
                menu.sync(this.selected);
                menu.sync(this.results);
            });
        }
    }
}
