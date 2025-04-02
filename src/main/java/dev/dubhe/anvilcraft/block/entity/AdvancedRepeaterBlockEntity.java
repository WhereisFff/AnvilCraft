package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.init.ModBlockEntities;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.inventory.AdvancedRepeaterMenu;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@Setter
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedRepeaterBlockEntity extends BlockEntity implements MenuProvider {
    protected byte startMode = 0;
    protected boolean outputInvert = false;
    protected int waitingTime = 2;
    protected int signalDuration = 2;

    private boolean isWaiting = false;
    @Setter(AccessLevel.NONE)
    private boolean isOutputting = false;
    @Setter(AccessLevel.NONE)
    private int waitingTimeRemaining;
    @Setter(AccessLevel.NONE)
    private int signalDurationRemaining;

    public AdvancedRepeaterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ADVANCED_REPEATER.get(), pos, blockState);
    }

    private AdvancedRepeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static AdvancedRepeaterBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new AdvancedRepeaterBlockEntity(type, pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("StartMode", this.startMode);
        tag.putBoolean("OutputMode", this.outputInvert);
        tag.putInt("WaitingTime", this.waitingTime);
        tag.putInt("SignalDuration", this.signalDuration);
        tag.putInt("RemainingWaitingTime", this.waitingTimeRemaining);
        tag.putInt("RemainingSignalDuration", this.signalDurationRemaining);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.startMode = tag.getByte("StartMode");
        this.outputInvert = tag.getBoolean("OutputMode");
        this.waitingTime = tag.getInt("WaitingTime");
        this.signalDuration = tag.getInt("SignalDuration");
        this.waitingTimeRemaining = tag.getInt("RemainingWaitingTime");
        this.signalDurationRemaining = tag.getInt("RemainingSignalDuration");
    }

    public void tick() {
        this.isOutputting = this.signalDurationRemaining > 0;

        if (this.waitingTimeRemaining > 0) {
            this.waitingTimeRemaining--;
            if (this.waitingTimeRemaining <= 0) {
                startOutputting();
            }
            return;
        }
        if (this.signalDurationRemaining > 0) {
            this.signalDurationRemaining--;
            if (this.signalDurationRemaining <= 0) {
                this.checkOnSignalEnd();
            }
        }
    }

    protected void startWaiting() {
        this.isWaiting = true;
        this.waitingTimeRemaining = this.waitingTime;
    }

    protected void startOutputting() {
        this.isOutputting = true;
        this.signalDurationRemaining = this.signalDuration;
    }

    protected void checkOnSignalEnd() {
        this.isOutputting = false;

        if (this.startMode == 2) {
            this.startWaiting();
        }
    }

    public void start() {
        startWaiting();
    }

    public int getOutputSignal() {
        if (isOutputting) {
            if (outputInvert) {
                return 0;
            } else {
                return 15;
            }
        } else {
            if (outputInvert) {
                return 15;
            } else {
                return 0;
            }
        }
    }

    public void setSignalDuration(int signalDuration) {
        this.signalDuration = Math.clamp(0, signalDuration, 24000);
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = Math.clamp(0, waitingTime, 24000);
    }

    public void setStartMode(byte mode) {
        this.startMode = mode;
        if (this.startMode == 2 && !this.isWaiting && !this.isOutputting) {
            this.startWaiting();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvilcraft.advanced_repeater");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (level == null || player.isSpectator()) return null;
        if (level.getBlockEntity(getBlockPos()) instanceof AdvancedRepeaterBlockEntity blockEntity)
            return new AdvancedRepeaterMenu(ModMenuTypes.ADVANCED_REPEATER.get(), containerId, inventory, blockEntity);
        return null;
    }
}
