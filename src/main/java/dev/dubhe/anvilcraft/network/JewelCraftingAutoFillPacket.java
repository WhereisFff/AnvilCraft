package dev.dubhe.anvilcraft.network;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.inventory.JewelCraftingMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * 宝石工艺台自动填充网络包
 * 用于将客户端的自动填充请求发送到服务端执行
 */
public record JewelCraftingAutoFillPacket() implements CustomPacketPayload {

    public static final Type<JewelCraftingAutoFillPacket> TYPE =
        new Type<>(AnvilCraft.of("jewel_crafting_auto_fill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JewelCraftingAutoFillPacket> STREAM_CODEC =
        StreamCodec.unit(new JewelCraftingAutoFillPacket());

    public static final IPayloadHandler<JewelCraftingAutoFillPacket> HANDLER =
        JewelCraftingAutoFillPacket::serverHandler;

    private static void serverHandler(JewelCraftingAutoFillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof JewelCraftingMenu menu) {
                menu.autoFill();
            }
        });
    }

    @Override
    public Type<JewelCraftingAutoFillPacket> type() {
        return TYPE;
    }

}