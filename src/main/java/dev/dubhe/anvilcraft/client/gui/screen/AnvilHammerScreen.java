package dev.dubhe.anvilcraft.client.gui.screen;

import dev.anvilcraft.lib.v2.wheel.api.WheelMenuModel;
import dev.anvilcraft.lib.v2.wheel.api.WheelSelectionEffect;
import dev.anvilcraft.lib.v2.wheel.client.gui.component.WheelFrostedBackground;
import dev.anvilcraft.lib.v2.wheel.client.gui.component.WheelWidget;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.hammer.IHasHammerEffect;
import dev.dubhe.anvilcraft.api.input.IMouseHandlerExtension;
import dev.dubhe.anvilcraft.block.multipart.FlexibleMultiPartBlock;
import dev.dubhe.anvilcraft.block.multipart.IMultiPartBlockModelHolder;
import dev.dubhe.anvilcraft.block.multipart.IMultiPartBlockModelHolder.ModelRenderTarget;
import dev.dubhe.anvilcraft.client.init.ModRenderTypes;
import dev.dubhe.anvilcraft.client.support.HammerPreviewState;
import dev.dubhe.anvilcraft.integration.iris.IrisState;
import dev.dubhe.anvilcraft.network.HammerChangeBlockPacket;
import dev.dubhe.anvilcraft.network.HammerChangeFlexibleMultiPartBlockPacket;
import dev.dubhe.anvilcraft.network.HammerUsePacket;
import dev.dubhe.anvilcraft.util.StateUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnvilHammerScreen extends Screen implements IHasHammerEffect {
    public static final int DELAY = 150;

    private final Minecraft minecraft = Minecraft.getInstance();
    private final float radialMenuScale = (float) AnvilCraft.CLIENT_CONFIG.ui.anvilHammerWheelScale;
    private final BlockPos targetBlockPos;
    private final BlockState initialBlockState;
    private final ModelRenderTarget initialModelTarget;
    private final Property<?> property;
    private final List<BlockState> possibleStates;
    private final Camera camera;
    private final int pageSize;
    private int pageIndex;

    private BlockState currentBlockState;
    private ModelRenderTarget currentModelTarget;
    private final long displayTime = System.currentTimeMillis();
    private boolean animationStarted;
    private boolean closingAnimationStarted;
    private @Nullable WheelWidget wheel;
    private @Nullable WheelFrostedBackground frostedBackground;
    private boolean previewActive;
    private double heldMouseX = Double.NaN;
    private double heldMouseY = Double.NaN;
    private boolean validate = true;
    private final InteractionHand hand;
    private final BlockHitResult hitVec;

    public AnvilHammerScreen(
        BlockPos targetBlockPos,
        BlockState initialBlockState, Property<?> property, List<BlockState> possibleStates, InteractionHand hand, BlockHitResult hitVec
    ) {
        super(Component.translatable("screen.anvilcraft.anvil_hammer.title"));
        this.targetBlockPos = targetBlockPos;
        this.initialBlockState = initialBlockState;
        this.currentBlockState = initialBlockState;
        this.property = property;
        this.possibleStates = possibleStates;
        this.pageSize = StateUtil.hasHammerPages(initialBlockState, property) ? 4 : possibleStates.size();
        this.pageIndex = possibleStates.indexOf(initialBlockState) / this.pageSize;
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.hand = hand;
        this.hitVec = hitVec;
        this.initialModelTarget = this.getModelRenderTarget(initialBlockState);
        this.currentModelTarget = this.initialModelTarget;
    }

    @Override
    protected void init() {
        if (this.frostedBackground != null) this.frostedBackground.close();
        this.frostedBackground = new WheelFrostedBackground();
        this.rebuildPage();
        var window = this.minecraft.getWindow();
        this.holdSelection(this.minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth(),
            this.minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());
    }

    private void rebuildPage() {
        int start = this.pageIndex * this.pageSize;
        List<WheelWidget.RawSection> sections = new ArrayList<>();
        var sources = this.initialModelTarget.parts();
        for (BlockState state : this.possibleStates.subList(start, Math.min(start + this.pageSize, this.possibleStates.size()))) {
            ModelRenderTarget target = this.getModelRenderTarget(state);
            var icon = new HammerWheelIcon(Objects.requireNonNull(this.minecraft.level), target, sources);
            sections.add(new WheelWidget.RawSection(HammerWheelLabel.describe(state, this.property),
                (graphics, pose, width, height) -> icon.render(pose, this.camera, this.radialMenuScale)));
        }
        this.wheel = new WheelWidget(
            0, 0, this.width, this.height, 55 * this.radialMenuScale, 105 * this.radialMenuScale,
            0, 300, 150, 0x88000000, WheelMenuModel.DEFAULT_SELECTION_EFFECT_COLOR, 5, 0xfdfdfd,
            this.radialMenuScale, sections, (int) (15 * this.radialMenuScale)
        ) {
            @Override
            public void checkMousePos(double mouseX, double mouseY) {
                double dx = mouseX - AnvilHammerScreen.this.heldMouseX;
                double dy = mouseY - AnvilHammerScreen.this.heldMouseY;
                if (dx * dx + dy * dy <= 4) {
                    return;
                }
                AnvilHammerScreen.this.heldMouseX = Double.NaN;
                super.checkMousePos(mouseX, mouseY);
            }
        };
        this.wheel.setSelectionEffect(WheelSelectionEffect.ANNULAR_SECTOR);
        this.wheel.setFrostedBackground(this.frostedBackground);
        this.wheel.setPageState(this.pageIndex > 0, start + this.pageSize < this.possibleStates.size());
        this.wheel.setCurrentIndex(this.possibleStates.indexOf(this.currentBlockState) - start);
    }

    private void holdSelection(double mouseX, double mouseY) {
        this.heldMouseX = mouseX;
        this.heldMouseY = mouseY;
    }

    private ModelRenderTarget getModelRenderTarget(BlockState previewState) {
        if (previewState.getBlock() instanceof IMultiPartBlockModelHolder holder) {
            return holder.getModelRenderTarget(
                Objects.requireNonNull(this.minecraft.level),
                this.targetBlockPos,
                this.initialBlockState,
                previewState
            );
        }
        return new ModelRenderTarget(this.targetBlockPos, previewState);
    }

    private void setCurrentBlockState(BlockState state) {
        this.currentBlockState = state;
        this.currentModelTarget = this.getModelRenderTarget(state);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.wheel == null || this.closingAnimationStarted || scrollY == 0) {
            return true;
        }
        if (this.possibleStates.size() > this.pageSize) {
            final int slot = Math.max(0, this.wheel.getCurrentSectionIndex());
            this.heldMouseX = Double.NaN;
            int pageCount = Math.ceilDiv(this.possibleStates.size(), this.pageSize);
            this.pageIndex = Math.floorMod(this.pageIndex + (scrollY > 0 ? 1 : -1), pageCount);
            this.rebuildPage();
            this.wheel.checkMousePos(mouseX, mouseY);
            if (this.wheel.getCurrentSectionIndex() < 0) {
                this.wheel.setCurrentIndex(Math.min(slot, this.possibleStates.size() - this.pageIndex * this.pageSize - 1));
            }
        } else {
            this.wheel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        this.holdSelection(mouseX, mouseY);
        this.updateSelection();
        return true;
    }

    private void updateSelection() {
        if (this.wheel == null) return;
        int index = this.wheel.getCurrentSectionIndex();
        if (index >= 0 && index < this.pageSize) {
            this.setCurrentBlockState(this.possibleStates.get(this.pageIndex * this.pageSize + index));
        } else {
            this.setCurrentBlockState(this.initialBlockState);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.wheel == null) return;
        if (this.validate && !this.isValidState(this.currentBlockState)) {
            this.validate = false;
            this.closingAnimationStarted = true;
            this.wheel.onClosing();
        }
        if (!this.shouldRender()) return;
        if (!this.closingAnimationStarted) {
            this.animationStarted = true;
            this.triggerChunkRebuild();
        }
        this.wheel.render(guiGraphics, mouseX, mouseY, partialTick);
        if (!this.closingAnimationStarted) this.updateSelection();
    }

    private void triggerChunkRebuild() {
        if (!this.previewActive && this.minecraft.level != null) {
            this.previewActive = true;
            HammerPreviewState.begin(this.minecraft.level, this.targetBlockPos, this.initialModelTarget);
        }
    }

    private boolean isValidState(BlockState selected) {
        BlockState state = Objects.requireNonNull(this.minecraft.level).getBlockState(this.targetBlockPos);
        if (!state.is(selected.getBlock())) return false;
        BlockEntity entity = this.minecraft.level.getBlockEntity(this.targetBlockPos);
        return entity == null || entity.getType().isValid(selected);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        if (this.frostedBackground != null) {
            if (this.wheel != null) {
                this.wheel.setFrostedBackground(null);
            }
            this.frostedBackground.close();
            this.frostedBackground = null;
        }
        this.restoreHiddenBlock();
        if (!this.animationStarted) {
            this.setCurrentBlockState(this.currentBlockState.cycle(this.property));
        }
        if (!this.validate) {
            super.removed();
            return;
        }
        if (this.animationStarted) {
            Level level = Objects.requireNonNull(Minecraft.getInstance().level);
            if (this.wheel == null || this.wheel.getCurrentSectionIndex() < 0) {
                super.removed();
                return;
            }
            this.currentBlockState = StateUtil.hammerTarget(level.getBlockState(this.targetBlockPos), this.currentBlockState);
            if (!(this.currentBlockState.getBlock() instanceof FlexibleMultiPartBlock<?, ?, ?>)
                && !StateUtil.verifyPossibleStatesForProperty(level.getBlockState(this.targetBlockPos), this.currentBlockState)) {
                super.removed();
                return;
            }
            if (this.currentBlockState.getBlock() instanceof FlexibleMultiPartBlock<?, ?, ?>) {
                if (this.currentBlockState.hasProperty(BlockStateProperties.FACING)) {
                    PacketDistributor.sendToServer(new HammerChangeFlexibleMultiPartBlockPacket(
                        this.targetBlockPos,
                        this.currentBlockState,
                        this.currentBlockState.getValue(BlockStateProperties.FACING)
                    ));
                } else if (this.currentBlockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    PacketDistributor.sendToServer(new HammerChangeFlexibleMultiPartBlockPacket(
                        this.targetBlockPos,
                        this.currentBlockState,
                        this.currentBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                    ));
                }
            } else {
                PacketDistributor.sendToServer(new HammerChangeBlockPacket(
                    this.targetBlockPos,
                    this.currentBlockState
                ));
            }
        } else {
            this.runLocalDefaultInteraction();
            PacketDistributor.sendToServer(new HammerUsePacket(this.targetBlockPos, this.hand, this.hitVec));
            super.removed();
            return;
        }
        super.removed();
    }

    private void restoreHiddenBlock() {
        if (this.previewActive) {
            this.previewActive = false;
            HammerPreviewState.end();
        }
    }

    @Override
    public void tick() {
        if (this.closingAnimationStarted) {
            this.minecraft.handleKeybinds();
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.minecraft.options.keyUse.matchesMouse(button)) return super.mouseReleased(mouseX, mouseY, button);
        if (this.shouldRender() && !this.closingAnimationStarted && this.wheel != null) {
            this.wheel.checkMousePos(mouseX, mouseY);
            this.updateSelection();
            IMouseHandlerExtension.of(this.minecraft.mouseHandler).anvilcraft$grabMouseWithScreen();
            this.closingAnimationStarted = true;
            this.wheel.onClosing();
        } else {
            this.minecraft.setScreen(null);
        }
        return true;
    }

    @Override
    public boolean shouldRender() {
        if (this.animationStarted) return true;
        return (this.displayTime + DELAY) <= System.currentTimeMillis();
    }

    @Override
    public boolean shouldSkipRebuildBlock() {
        return this.previewActive;
    }

    private void runLocalDefaultInteraction() {
        Level level = this.minecraft.level;
        if (level == null || this.minecraft.player == null) return;
        BlockState state = level.getBlockState(this.targetBlockPos);
        Block block = state.getBlock();
        if (!(block instanceof DoorBlock
              || block instanceof TrapDoorBlock
              || block instanceof FenceGateBlock
              || block instanceof ButtonBlock
              || block instanceof ComparatorBlock)) {
            return;
        }
        state.useWithoutItem(level, this.minecraft.player, this.hitVec);
    }

    @Override
    public BlockPos renderingBlockPos() {
        return this.currentModelTarget.pos();
    }

    @Override
    public BlockPos hiddenBlockPos() {
        return this.initialModelTarget.pos();
    }

    @Override
    public BlockState renderingBlockState() {
        return this.currentModelTarget.state();
    }

    @Override
    public RenderType renderType() {
        if (IrisState.isShaderEnabled()) {
            return RenderType.translucent();
        }
        return ModRenderTypes.TRANSLUCENT_COLORED_OVERLAY;
    }

}
