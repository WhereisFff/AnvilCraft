package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.integration.IntegrationUtil;
import dev.dubhe.anvilcraft.network.OpenIntegrationScreenPacket;
import dev.dubhe.anvilcraft.util.ModEventUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforgespi.Environment;

import java.util.List;

public class GuideBookItem extends Item {
    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (ModEventUtil.hasGuideBook()) {
                ModEventUtil.openGuideBook(level, serverPlayer, usedHand);
                return new InteractionResultHolder<>(InteractionResult.CONSUME, player.getItemInHand(usedHand));
            } else {
                serverPlayer.connection.send(new OpenIntegrationScreenPacket());
            }
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(usedHand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!Environment.get().getDist().isClient()) {
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            return;
        }
        long lastThoughtTime = IntegrationUtil.getLastThoughtTime();
        if (lastThoughtTime <= 0) {
            tooltipComponents.add(Component.translatable("tooltip.anvilcraft.thought", Component.keybind("key.anvilcraft.thought")));
            super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        long curTime = minecraft.gui.getGuiTicks();
        long deltaTime = curTime - lastThoughtTime;
        final int maxPlaceholderCount = 20;
        final double maxSeconds = 1.5;
        int placeholderCount = (int) Math.floor(Math.min(deltaTime, 20 * maxSeconds) / (20 * maxSeconds) * maxPlaceholderCount);
        int blankCount = maxPlaceholderCount - placeholderCount;
        StringBuilder builder = new StringBuilder("[");
        builder.append("||".repeat(Math.max(0, placeholderCount)));
        builder.append(" ".repeat(Math.max(0, blankCount)));
        tooltipComponents.add(Component.literal(builder.append("]").toString()));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
