package dev.dubhe.anvilcraft.inventory;

import com.mojang.datafixers.util.Pair;
import dev.dubhe.anvilcraft.block.entity.AdvancedRepeaterBlockEntity;
import dev.dubhe.anvilcraft.client.util.MenuUtil;
import dev.dubhe.anvilcraft.init.ModBlocks;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedRepeaterMenu extends AbstractContainerMenu {
    @Getter
    private final AdvancedRepeaterBlockEntity blockEntity;
    private final Level level;

    public AdvancedRepeaterMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, @NotNull BlockEntity machine) {
        super(menuType, containerId);
        this.blockEntity = (AdvancedRepeaterBlockEntity) machine;
        this.level = inventory.player.level();

        MenuUtil.addDataSlots(
            this::addDataSlot,
            new Pair<>(() -> (int) this.blockEntity.getStartMode(), this.blockEntity::setStartMode),
            new Pair<>(this.blockEntity::getOutputMode, this.blockEntity::setOutputMode),
            new Pair<>(this.blockEntity::getWaitingTime, this.blockEntity::setWaitingTime),
            new Pair<>(this.blockEntity::getSignalDuration, this.blockEntity::setSignalDuration)
        );
    }

    public AdvancedRepeaterMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, @NotNull FriendlyByteBuf extraData) {
        this(menuType, containerId, inventory, Objects.requireNonNull(inventory.player.level().getBlockEntity(extraData.readBlockPos())));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
            ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
            player,
            ModBlocks.ADVANCED_REPEATER.get()
        );
    }

    public void setStartMode(byte mode) {
        this.setData(0, mode);
    }

    public void setOutputMode(byte mode) {
        this.setData(1, mode);
    }

    public int getWaitingTime() {
        return this.blockEntity.getWaitingTime();
    }

    public int getSignalDuration() {
        return this.blockEntity.getSignalDuration();
    }

    public void addWaitingTime(int delta) {
        this.setData(2, this.blockEntity.getWaitingTime() + delta);
    }

    public void addSignalDuration(int delta) {
        this.setData(3, this.blockEntity.getSignalDuration() + delta);
    }
}
