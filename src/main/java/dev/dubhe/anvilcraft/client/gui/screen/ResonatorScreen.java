package dev.dubhe.anvilcraft.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.api.input.IMouseHandlerExtension;
import dev.dubhe.anvilcraft.client.gui.component.WheelWidget;
import dev.dubhe.anvilcraft.client.init.ModKeyMappings;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.util.RenderHelper;
import dev.dubhe.anvilcraft.util.function.Consumer4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;
import java.util.Objects;

public class ResonatorScreen extends Screen {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
    private final InteractionHand hand;
    private final int mode;

    private int leftPos;
    private int topPos;
    private WheelWidget wheel;
    private boolean alreadyClosing = false;

    public ResonatorScreen(InteractionHand hand, int mode) {
        super(Component.translatable("screen.anvilcraft.resonator.title"));
        this.hand = hand;
        this.mode = mode;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - 105) / 2;
        this.topPos = (this.height - 105) / 2;
        ItemStack holding = player.getItemInHand(this.hand);
        WheelWidget wheel = new WheelWidget(
            this.leftPos, this.topPos, 105, 105,
            27.5f, 52.5f,
            List.of(
                new Pair<>(Component.translatable("screen.anvilcraft.resonator.auto"), Consumer4::noop),
                new Pair<>(
                    Component.empty(),
                    (graphics, pose, width, height) -> {
                        ItemStack stack = holding.copy();
                        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
                        graphics.renderItem(stack, 2, 2, 9910597);
                    }),
                new Pair<>(
                    Component.empty(),
                    (graphics, pose, width, height) -> {
                        ItemStack stack = holding.copy();
                        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(2));
                        graphics.renderItem(stack, 2, 2, 9910597);
                    }),
                new Pair<>(
                    Component.empty(),
                    (graphics, pose, width, height) -> {
                        ItemStack stack = holding.copy();
                        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(3));
                        graphics.renderItem(stack, 2, 2, 9910597);
                    }),
                new Pair<>(
                    Component.empty(),
                    (graphics, pose, width, height) -> {
                        ItemStack stack = holding.copy();
                        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(4));
                        graphics.renderItem(stack, 2, 2, 9910597);
                    })
            )
        ).setCurrentIndex(this.mode);
        this.wheel = this.addRenderableWidget(wheel);
    }

    @Override
    public void tick() {
        if (!ModKeyMappings.SWITCH_RESONATE_MODE.get().isDown() && this.wheel.shouldRender() && !this.wheel.isClosingAnimationStarted()) {
            this.wheel.setClosingAnimationStarted(true);
        }
    }
}
