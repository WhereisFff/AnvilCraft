package dev.dubhe.anvilcraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.api.item.property.Merciless;
import dev.dubhe.anvilcraft.api.item.property.Multiphase;
import dev.dubhe.anvilcraft.client.gui.component.WheelWidget;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.network.MultiphaseChangePacket;
import dev.dubhe.anvilcraft.util.ListUtil;
import dev.dubhe.anvilcraft.util.function.Consumer4;
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
import java.util.List;
import java.util.Objects;

public class MultiphaseScreen extends Screen {
    private final LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
    private final InteractionHand hand;
    private int size;

    public WheelWidget wheel;

    public MultiphaseScreen(InteractionHand hand) {
        super(Component.translatable("screen.anvilcraft.multiphase.title"));
        this.hand = hand;
        if (!player.getItemInHand(hand).has(ModComponents.MULTIPHASE)) this.onClose();
    }

    @Override
    protected void init() {
        int leftPos = (this.width - 75) / 2;
        int topPos = (this.height - 75) / 2;
        ItemStack holding = player.getItemInHand(this.hand);
        List<Multiphase.Phase> phasesCopy = new ArrayList<>(holding.getOrDefault(ModComponents.MULTIPHASE, Multiphase.EMPTY).phases());
        boolean merciless = holding.getOrDefault(ModComponents.MERCILESS, Merciless.DEFAULT).enabled();
        int size = phasesCopy.size();
        List<Pair<Component, Consumer4<GuiGraphics, PoseStack, Integer, Integer>>> sections = Lists.reverse(ListUtil.createWithValues(
            size, i -> new Pair<>(
                phasesCopy.get(i).phaseName().copy(), renderHolding(holding, phasesCopy, i, false))));
        if (merciless) {
            sections.addAll(ListUtil.createWithValues(
                size, i -> new Pair<>(
                    phasesCopy.get(i).phaseName().copy()
                        .append(Component.translatable("screen.anvilcraft.multiphase.merciless")),
                    renderHolding(holding, phasesCopy, i, true))));
        }
        WheelWidget wheel = new WheelWidget(
            leftPos, topPos, 75, 75,
            12.5f, 32.5f, 0.75f, -180f / size / 2,
            sections
        ).setCurrentIndex(this.wheel != null ? this.wheel.getCurrentSectionIndex() : size - 1);
        this.clearWidgets();
        this.wheel = this.addRenderableWidget(wheel);
        this.size = size;
    }

    private static Consumer4<GuiGraphics, PoseStack, Integer, Integer> renderHolding(
        ItemStack holding, List<Multiphase.Phase> phasesCopy, int index, boolean isMerciless
    ) {
        return (graphics, pose, width, height) -> {
            ItemStack stack = holding.copy();
            phasesCopy.get(index % phasesCopy.size()).applyToStack(stack);
            stack.set(ModComponents.MERCILESS, new Merciless(isMerciless));
            graphics.renderItem(stack, 2, 2, 9910597);
        };
    }

    @Override
    public void removed() {
        super.removed();
        PacketDistributor.sendToServer(new MultiphaseChangePacket(
            this.hand, this.wheel.getCurrentSectionIndex() % this.size,
            this.wheel.getCurrentSectionIndex() >= this.size));
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
