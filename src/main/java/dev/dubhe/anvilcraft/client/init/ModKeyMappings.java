package dev.dubhe.anvilcraft.client.init;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final Lazy<KeyMapping> TOGGLE_GOGGLE = register(
        "toggle_goggle",
        KeyConflictContext.IN_GAME,
        Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN
    );

    @Contract(value = "_, _, _, _ -> new", pure = true)
    @SuppressWarnings("SameParameterValue")
    private static @NotNull Lazy<KeyMapping> register(String name, KeyConflictContext context, Type type, int key) {
        return Lazy.of(() -> new KeyMapping("key.anvilcraft." + name, context, type, key, "key.categories.anvilcraft"));
    }

    public static void register(@NotNull RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_GOGGLE.get());
    }
}
