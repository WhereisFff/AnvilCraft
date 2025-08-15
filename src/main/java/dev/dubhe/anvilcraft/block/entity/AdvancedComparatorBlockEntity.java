package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.api.item.IDiskCloneable;
import dev.dubhe.anvilcraft.block.AdvancedComparatorBlock;
import dev.dubhe.anvilcraft.init.ModBlockEntities;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.inventory.AdvancedComparatorMenu;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@Setter
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedComparatorBlockEntity extends BlockEntity implements MenuProvider, IDiskCloneable {
    protected Mode compareMode = Mode.HYSTERESIS;
    protected boolean outputInvert = false;
    protected boolean redstoneControl = false;
    protected int highLimit = 10;
    protected int lowLimit = 5;
    protected int inputtingSignal = 0;

    @Setter(AccessLevel.NONE)
    private State state = State.OUTPUT_LOW;

    public AdvancedComparatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ADVANCED_COMPARATOR.get(), pos, blockState);
    }

    private AdvancedComparatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static AdvancedComparatorBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new AdvancedComparatorBlockEntity(type, pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("ExtraData", this.constructDataNbt());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        CompoundTag data = tag.getCompound("ExtraData");
        this.readDataNbt(data);
        if ((this.compareMode == Mode.HYSTERESIS && this.inputtingSignal >= this.highLimit)
            || (this.compareMode == Mode.WINDOW && this.inputtingSignal <= this.highLimit))
            this.state = State.OUTPUT_HIGH;
        else this.state = State.OUTPUT_LOW;
    }

    public CompoundTag constructDataNbt() {
        CompoundTag data = new CompoundTag();
        data.putByte("CompareMode", this.compareMode.index());
        data.putBoolean("OutputMode", this.outputInvert);
        data.putBoolean("RedstoneControl", this.redstoneControl);
        data.putInt("HighLimit", this.highLimit);
        data.putInt("LowLimit", this.lowLimit);
        data.putInt("InputSignal", this.inputtingSignal);
        return data;
    }

    public AdvancedComparatorBlockEntity readDataNbt(CompoundTag data) {
        this.compareMode = Mode.fromIndex(data.getByte("CompareMode"));
        this.outputInvert = data.getBoolean("OutputMode");
        this.redstoneControl = data.getBoolean("RedstoneControl");
        this.highLimit = data.getInt("HighLimit");
        this.lowLimit = data.getInt("LowLimit");
        this.inputtingSignal = data.getInt("InputSignal");
        return this;
    }

    @Override
    public void storeDiskData(CompoundTag tag) {
        tag.put("Data", this.constructDataNbt());
    }

    @Override
    public void applyDiskData(CompoundTag data) {
        this.readDataNbt(data.getCompound("Data"));
    }

    protected void tickTime() {
        if (this.compareMode == Mode.WINDOW && highLimit == lowLimit && inputtingSignal == highLimit) {
            this.state = State.OUTPUT_HIGH;
            return;
        }
        switch (this.state) {
            case OUTPUT_LOW -> {
                if (this.compareMode == Mode.HYSTERESIS) {
                    if (inputtingSignal >= highLimit) {
                        this.state = State.OUTPUT_HIGH;
                        this.setChanged();
                    }
                } else if (this.compareMode == Mode.WINDOW) {
                    if (inputtingSignal >= lowLimit && inputtingSignal <= highLimit) {
                        this.state = State.OUTPUT_HIGH;
                        this.setChanged();
                    }
                }
            }
            case OUTPUT_HIGH -> {
                if (this.compareMode == Mode.HYSTERESIS) {
                    if (inputtingSignal < lowLimit) {
                        this.state = State.OUTPUT_LOW;
                        this.setChanged();
                    }
                } else if (this.compareMode == Mode.WINDOW) {
                    if (inputtingSignal < lowLimit || inputtingSignal > highLimit) {
                        this.state = State.OUTPUT_LOW;
                        this.setChanged();
                    }
                }
            }
        }
    }

    private void updateInputtingSignal(Level level, BlockPos pos, BlockState state) {
        this.inputtingSignal = AdvancedComparatorBlock.getInputSignal(level, pos, state);
        if (this.isRedstoneControl()) {
            this.highLimit = AdvancedComparatorBlock.getAlternateSignal(level, pos, state, true);
            this.lowLimit = AdvancedComparatorBlock.getAlternateSignal(level, pos, state, false);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedComparatorBlockEntity ComparatorEntity) {
        if (level.isClientSide) return;
        ComparatorEntity.tickTime();
        ComparatorEntity.updateInputtingSignal(level, pos, state);
        ComparatorEntity.updateBlockAndNeighbours(level, pos, state);
    }

    protected void updateBlockAndNeighbours(Level level, BlockPos pos, BlockState state) {
        if (!(level.getBlockEntity(pos) instanceof AdvancedComparatorBlockEntity ComparatorEntity)) return;
        Direction direction = state.getValue(AdvancedComparatorBlock.FACING).getOpposite();
        BlockPos neighbourPos = pos.relative(direction);
        boolean shouldPower = ComparatorEntity.isOutputting();
        boolean isInput = ComparatorEntity.inputtingSignal > 0;
        Mode mode = ComparatorEntity.compareMode;
        level.setBlockAndUpdate(pos, state
            .setValue(AdvancedComparatorBlock.POWERED, shouldPower)
            .setValue(AdvancedComparatorBlock.INPUT, isInput)
            .setValue(AdvancedComparatorBlock.OUTPUT, shouldPower)
            .setValue(AdvancedComparatorBlock.MODE, mode));
        level.neighborChanged(neighbourPos, state.getBlock(), pos);
        level.updateNeighborsAtExceptFromFacing(neighbourPos, state.getBlock(), direction.getOpposite());
    }

    public boolean isOutputting() {
        return this.state == AdvancedComparatorBlockEntity.State.OUTPUT_HIGH != this.outputInvert;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level == null) return;
        updateInputtingSignal(this.level, this.getBlockPos(), this.getBlockState());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvilcraft.advanced_comparator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (player.isSpectator()) return null;
        if (player.level().getBlockEntity(getBlockPos()) instanceof AdvancedComparatorBlockEntity blockEntity)
            return new AdvancedComparatorMenu(ModMenuTypes.ADVANCED_COMPARATOR.get(), containerId, inventory, blockEntity);
        return null;
    }

    public enum State {
        OUTPUT_LOW, OUTPUT_HIGH
    }

    public enum Mode implements StringRepresentable {
        HYSTERESIS, WINDOW;

        public byte index() {
            return (byte) this.ordinal();
        }

        public static AdvancedComparatorBlockEntity.Mode fromIndex(int index) {
            return values()[index];
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }
}
