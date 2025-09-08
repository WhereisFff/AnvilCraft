package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.PowerComponentType;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.block.PropelPiston;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.network.UpdatePropelPistonStoredEnergyPacket;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class PropelPistonBlockEntity extends BlockEntity implements IPowerConsumer {
    private PowerGrid powerGrid = null;

    /**
     * 储存的能量 单位：kJ
     */
    @Getter
    private int storedEnergy = 0;

    private static int cooldown = 0;

    public PropelPistonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void updateStoredEnergy(Integer energy) {
        this.storedEnergy = Math.min(energy, 80000);
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()), new UpdatePropelPistonStoredEnergyPacket(getBlockPos(), storedEnergy));
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        cooldown--;
        if (this.powerGrid != null && this.powerGrid.isWorking()) {
            if (this.storedEnergy < 80000) {
                updateStoredEnergy(this.storedEnergy + 12);
            }
        }
        if (cooldown > 0) {
            return;
        }
        if (this.storedEnergy > 0) {
            check(level, pos, state);
            cooldown = 4;
        } else {
            level.setBlockAndUpdate(pos, state.setValue(PropelPiston.EXHAUSTED, true));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storedEnergy = tag.getInt("storedEnergy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("storedEnergy", Math.min(this.storedEnergy, 80000));
    }

    @Override
    public int getInputPower() {
        if (this.storedEnergy >= 80000) {
            return 0;
        }
        return 256;
    }

    @Override
    public @Nullable Level getCurrentLevel() {
        return this.getLevel();
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public void setGrid(@Nullable PowerGrid grid) {
        this.powerGrid = grid;
    }

    @Override
    public @Nullable PowerGrid getGrid() {
        return this.powerGrid;
    }

    @Override
    public PowerComponentType getComponentType() {
        return PowerComponentType.CONSUMER;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        Integer energy = componentInput.getOrDefault(ModComponents.STORED_ENERGY, 0);
        this.updateStoredEnergy(energy);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        components.set(ModComponents.STORED_ENERGY, this.storedEnergy);
    }

    private void check(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(PropelPiston.FACING);
        boolean flag = state.getValue(PropelPiston.EXHAUSTED);
        if (!flag) {
            if (new PistonStructureResolver(level, pos, direction, true).resolve()) {
                level.blockEvent(pos, getBlockState().getBlock(), 0, direction.get3DDataValue());
            } else {
                level.setBlockAndUpdate(pos, state.setValue(PropelPiston.EXHAUSTED, true));
            }
        }
    }
}
