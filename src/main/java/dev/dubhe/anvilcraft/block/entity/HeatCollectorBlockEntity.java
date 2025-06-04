package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.api.heat.collector.HeatCollectorManager;
import dev.dubhe.anvilcraft.api.heat.collector.HeatSourceEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HeatCollectorBlockEntity extends BlockEntity implements IPowerProducer {
    private final Set<BlockPos> collectablePosesGetter;
    @Getter
    private int time = 0;
    @Getter
    @Setter
    private PowerGrid grid = null;
    @Getter
    private int outputPower = 0;
    @Getter
    private float rotation = 0;

    public HeatCollectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.collectablePosesGetter = this.getCollectableSourcePoses();
    }

    public static HeatCollectorBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new HeatCollectorBlockEntity(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("tickCache", this.time);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.time = tag.getInt("tickCache");
    }

    @Override
    public void gridTick() {
        this.time++;

        this.outputPower = 0;
        for (BlockPos pos : this.collectablePosesGetter) {
            BlockState state = this.getCurrentLevel().getBlockState(pos);
            for (HeatSourceEntry entry : HeatCollectorManager.SOURCE_ENTRIES) {
                if (entry.accepts(state) > 0) {
                    this.outputPower += entry.accepts(state);
                    if (this.getCurrentLevel().getGameTime() % entry.timeToTransform() == 0) {
                        this.getCurrentLevel().setBlockAndUpdate(pos, entry.transform(state));
                    }
                }
            }
        }
    }

    public void clientTick() {
        rotation += (float) (getServerPower() * 0.03);
    }

    private Set<BlockPos> getCollectableSourcePoses() {
        Set<BlockPos> poses = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = this.getBlockPos().subtract(new Vec3i(-x, -y, -z)).immutable();
                    if (this.getLevel() != null
                        && (this.getLevel().getMinBuildHeight() < this.getPos().getY()
                        || this.getLevel().getMaxBuildHeight() > this.getPos().getY())
                    ) continue;
                    poses.add(pos);
                }
            }
        }
        return poses;
    }

    @Override
    public Level getCurrentLevel() {
        return Objects.requireNonNull(this.getLevel());
    }

    @Override
    public @NotNull BlockPos getPos() {
        return this.getBlockPos();
    }
}
