package dev.dubhe.anvilcraft.util;

import dev.dubhe.anvilcraft.api.event.GuideBookEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

public class ModEventUtil {
    public static boolean hasGuideBook() {
        return NeoForge.EVENT_BUS.post(new GuideBookEvent.HasGuideBookEvent()).isHasGuideBook();
    }

    public static void openGuideBook(Level level, ServerPlayer player, InteractionHand usedHand) {
        NeoForge.EVENT_BUS.post(new GuideBookEvent.OpenGuideBookEvent(level, player, usedHand));
    }
}
