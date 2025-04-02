package dev.dubhe.anvilcraft.client.gui.screen;

import dev.dubhe.anvilcraft.inventory.AdvancedRepeaterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AdvancedRepeaterScreen extends AbstractContainerScreen<AdvancedRepeaterMenu> {
    public AdvancedRepeaterScreen(AdvancedRepeaterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }
}
