package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.api.heat.collector.HeatCollectorManager;
import dev.dubhe.anvilcraft.api.heat.collector.HeatSourceEntry;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import dev.dubhe.anvilcraft.block.ChargeCollectorBlock;
import dev.dubhe.anvilcraft.block.HeatCollectorBlock;
import dev.dubhe.anvilcraft.network.ChargeCollectorIncomingChargePacket;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class HeatCollectorBlockEntity extends BlockEntity implements IPowerProducer, IHasAffectRange {
    private static final int MAX_OUTPUT_POWER = 4096;
    private final Set<BlockPos> collectablePosesGetter;
    @Getter
    private int time = 0;
    @Getter
    @Setter
    private PowerGrid grid = null;
    @Getter
    private int outputPower = 0;
    private int inputtingPower = 0;
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
        if (level == null || level.isClientSide()) return;
        int oldPower = this.outputPower;
        this.outputPower = this.inputtingPower;
        if (this.outputPower > 0 && this.getBlockState().getBlock() instanceof HeatCollectorBlock collector) {
            collector.activate(this.level, this.getBlockPos(), this.getBlockState());
        }
        if (this.outputPower != oldPower && grid != null) grid.markChanged();
        this.inputtingPower = 0;
        this.time++;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        HeatCollectorManager.addHeatCollector(this.getPos(), this.getCurrentLevel());
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        HeatCollectorManager.removeHeatCollector(this.getPos(), this.getCurrentLevel());
    }

    public void clientTick() {
        rotation += (float) (getServerPower() * 0.03);
    }

    public Set<BlockPos> getCollectableSourcePoses() {
        Set<BlockPos> poses = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = this.getBlockPos().subtract(new Vec3i(-x, -y, -z)).immutable();
                    if (this.getLevel() != null
                        && (this.getLevel().getMinBuildHeight() > this.getPos().getY()
                        || this.getLevel().getMaxBuildHeight() < this.getPos().getY())
                    ) continue;
                    poses.add(pos);
                }
            }
        }
        return poses;
    }

    /**
     * 向集热器添加电荷
     *
     * @param num 添加至收集器的热量
     * @return 溢出的热量(即未被添加至该收集器的热量)
     */
    public int inputtingHeat(int num) {
        int overflow = num - (MAX_OUTPUT_POWER - this.inputtingPower);
        if (overflow < 0) {
            overflow = 0;
        }
        int acceptableChargeCount = num - overflow;
        this.inputtingPower += acceptableChargeCount;
        return overflow;
    }

    @Override
    public Level getCurrentLevel() {
        return Objects.requireNonNull(this.getLevel());
    }

    @Override
    public @NotNull BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public AABB shape() {
        return AABB.ofSize(getBlockPos().getCenter(), 5, 5, 5);
    }
}
