package dev.dubhe.anvilcraft.client.gui.screen;

import dev.dubhe.anvilcraft.client.gui.component.WheelWidget;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.network.multiple.MultiphasePackets;
import dev.dubhe.anvilcraft.saved.multiphase.Multiphase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MultiphaseScreen extends Screen {
    private final LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
    private final InteractionHand hand;
    private final List<Multiphase.Phase> phasesCopy = new ArrayList<>();

    public WheelWidget wheel;

    public MultiphaseScreen(InteractionHand hand) {
        super(Component.translatable("screen.anvilcraft.multiphase.title"));
        this.hand = hand;
        if (!player.getItemInHand(hand).has(ModComponents.MULTIPHASE)) this.onClose();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int leftPos = (this.width - 75) / 2;
        int topPos = (this.height - 75) / 2;
        ItemStack holding = this.player.getItemInHand(this.hand);
        if (this.phasesCopy.isEmpty()) {
            Multiphase multiphase = holding.get(ModComponents.MULTIPHASE).toMultiphase();
            if (multiphase != null) {
                this.phasesCopy.addAll(multiphase.phases());
                this.phasesCopy.sort(Comparator.comparingInt(Multiphase.Phase::index));
            }
        }
        int size = this.phasesCopy.size();
        var sections = MultiphaseScreen.createWheelSections(holding, this.phasesCopy);
        WheelWidget wheel = new WheelWidget(
            leftPos,
            topPos,
            75,
            75,
            12.5f,
            32.5f,
            0.75f,
            90f - 180f / size,
            sections
        ).setCurrentIndex(this.wheel != null ? this.wheel.getCurrentSectionIndex() : 0);
        this.wheel = this.addRenderableWidget(wheel);
    }

    private static List<WheelWidget.RawSection> createWheelSections(ItemStack holding, List<Multiphase.Phase> phasesCopy) {
        List<WheelWidget.RawSection> sections = new ArrayList<>();
        for (int i = 0; i < phasesCopy.size(); i++) {
            sections.add(new WheelWidget.RawSection(
                phasesCopy.get(i).phaseName().copy(),
                MultiphaseScreen.renderHolding(holding, phasesCopy, i)
            ));
        }
        return sections;
    }

    private static WheelWidget.SectionRenderer renderHolding(
        ItemStack holding,
        List<Multiphase.Phase> phasesCopy,
        int index
    ) {
        return (graphics, pose, width, height) -> {
            ItemStack stack = holding.copy();
            phasesCopy.get(index % phasesCopy.size()).applyToStack(stack);
            graphics.renderItem(stack, 2, 2, 9910597);
        };
    }

    @Override
    public void removed() {
        super.removed();
        int index = this.wheel.getCurrentSectionIndex();
        PacketDistributor.sendToServer(new MultiphasePackets.ChangePhase(this.hand, index));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
