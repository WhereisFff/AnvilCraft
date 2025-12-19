package dev.dubhe.anvilcraft.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class IntegrationScreen extends Screen {
    public static final Component TITLE = Component.translatable("screen.anvilcraft.integration_screen.title");

    public IntegrationScreen() {
        super(IntegrationScreen.TITLE);
    }
}
