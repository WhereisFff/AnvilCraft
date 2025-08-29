package dev.dubhe.anvilcraft.client.gui.screen;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.inventory.RoyalGrindstoneMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RoyalGrindstoneScreen extends AbstractContainerScreen<RoyalGrindstoneMenu> {
    private static final ResourceLocation GRINDSTONE_LOCATION =
        AnvilCraft.of("textures/gui/container/smithing/background/royal_grindstone.png");

    public RoyalGrindstoneScreen(
        RoyalGrindstoneMenu menu, Inventory playerInventory, @SuppressWarnings("unused") Component title) {
        super(menu, playerInventory, Component.translatable("screen.anvilcraft.royal_grindstone.title"));
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderLabels(guiGraphics);
    }

    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GRINDSTONE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.setColor(1f, 1f, 1f, 1);
        final int maskColor = 0x55777777;
        ItemStack repairMaterialSlotsItem = this.menu.getSlot(1).getItem();
        ItemStack resultMaterialSlotsItem = this.menu.getSlot(3).getItem();
        if (repairMaterialSlotsItem.isEmpty() && resultMaterialSlotsItem.isEmpty()) {
            guiGraphics.renderItem(
                RoyalGrindstoneMenu.DEFAULT_REPAIR_MATERIAL.getDefaultInstance(), i + 89, j + 22, (int) (partialTick * 100));
            guiGraphics.renderItem(
                RoyalGrindstoneMenu.REPAIR_COST_RECIPES.get(RoyalGrindstoneMenu.DEFAULT_REPAIR_MATERIAL).getSecond().getDefaultInstance(), i + 89, j + 47, (int) (partialTick * 100));
            guiGraphics.fill(RenderType.guiOverlay(), i + 89, j + 22, i + 89 + 16, j + 22 + 16, maskColor);
            guiGraphics.fill(RenderType.guiOverlay(), i + 89, j + 47, i + 89 + 16, j + 47 + 16, maskColor);
        } else if (resultMaterialSlotsItem.isEmpty()) {
            guiGraphics.renderItem(
                RoyalGrindstoneMenu.REPAIR_COST_RECIPES.get(repairMaterialSlotsItem.getItem()).getSecond().getDefaultInstance(), i + 89, j + 47, (int) (partialTick * 100));
            guiGraphics.fill(RenderType.guiOverlay(), i + 89, j + 47, i + 89 + 16, j + 47 + 16, maskColor);
        } else if (repairMaterialSlotsItem.isEmpty()) {
            Item repairItem = RoyalGrindstoneMenu.REPAIR_COST_RECIPES.entrySet().stream()
                .filter(entry -> entry.getValue().getSecond() == resultMaterialSlotsItem.getItem())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(RoyalGrindstoneMenu.DEFAULT_REPAIR_MATERIAL);
            guiGraphics.renderItem(repairItem.getDefaultInstance(), i + 89, j + 22, (int) (partialTick * 100));
            guiGraphics.fill(RenderType.guiOverlay(), i + 89, j + 22, i + 89 + 16, j + 22 + 16, maskColor);
        }
    }

    protected void renderLabels(GuiGraphics guiGraphics) {
        if (this.menu.getSlot(2).hasItem()) {
            Component usedGoldText = Component.literal("" + this.menu.usedGold);
            Component removedCurseCountText = Component.translatable(
                "screen.anvilcraft.royal_grindstone.remove_curse_count",
                this.menu.removedCurseCount, this.menu.totalCurseCount);
            Component removedRepairCostText = Component.translatable(
                "screen.anvilcraft.royal_grindstone.remove_repair_cost",
                this.menu.removedRepairCost, this.menu.totalRepairCost);
            drawLabel(
                (int) (92 + 4.5 - (this.font.width(usedGoldText) / 2f)),
                38,
                usedGoldText,
                guiGraphics);
            drawLabel(
                170 - this.font.width(removedCurseCountText),
                13,
                removedCurseCountText,
                guiGraphics);
            drawLabel(
                170 - this.font.width(removedRepairCostText),
                58,
                removedRepairCostText,
                guiGraphics);
        }
    }

    private void drawLabel(int x, int y, Component component, @NotNull GuiGraphics guiGraphics) {
        int i = (this.width - this.imageWidth - 2) / 2;
        int j = (this.height - this.imageHeight + 23) / 2;
        x += i;
        y += j;
        guiGraphics.drawString(this.font, component, x + 2, y - 10, 8453920);
    }
}
