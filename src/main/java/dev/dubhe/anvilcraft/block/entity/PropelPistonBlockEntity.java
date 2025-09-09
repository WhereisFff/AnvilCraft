package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.PowerComponentType;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.block.PropelPiston;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.network.UpdatePropelPistonStoredEnergyPacket;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    @Getter
    @Setter
    private PowerGrid grid;

    /**
     * 储存的能量 单位：kJ
     */
    @Getter
    private int storedEnergy = 0;

    public PropelPistonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void updateStoredEnergy(Integer energy) {
        this.storedEnergy = Math.clamp(energy, 0, 80000);
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()), new UpdatePropelPistonStoredEnergyPacket(getBlockPos(), storedEnergy));
    }

    public void addEnergy(int energy) {
        updateStoredEnergy(getStoredEnergy() + energy);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (this.grid != null && this.grid.isWorking()) {
            if (this.getInputPower() >= 256 && this.storedEnergy < 80000) {
                addEnergy(12);
            }
        }
        if (getStoredEnergy() > 0) {
            level.setBlockAndUpdate(pos, state.setValue(PropelPiston.EXHAUSTED, false));
            if (!level.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
                checkCanMove(level, pos, state);
            }
        } else {
            level.setBlockAndUpdate(pos, state.setValue(PropelPiston.EXHAUSTED, true).setValue(PropelPiston.MOVING, false));
        }
    }

    private void checkCanMove(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(PropelPiston.FACING);
        if (state.getValue(PropelPiston.MOVING)) {
            if (new PistonStructureResolver(level, pos, direction, true).resolve()) {
                level.blockEvent(pos, state.getBlock(), 0, state.getValue(PropelPiston.FACING).get3DDataValue());
            } else {
                level.setBlockAndUpdate(pos, state.setValue(PropelPiston.MOVING, false));
            }
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag compound = new CompoundTag();
        compound.putLong("storedEnergy", this.storedEnergy);
        return compound;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
}
