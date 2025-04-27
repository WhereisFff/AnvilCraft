package dev.dubhe.anvilcraft.client.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final Lazy<KeyMapping> SWITCH_PHASE = register(
        "change_enchantment_space",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X
    );

    private static Lazy<KeyMapping> register(String name, KeyConflictContext context, InputConstants.Type type, int key) {
        return Lazy.of(() -> new KeyMapping("key.anvilcraft." + name, context, type, key, "key.categories.anvilcraft"));
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SWITCH_PHASE.get());
    }
}
