package dev.dubhe.anvilcraft.block.entity;

import dev.dubhe.anvilcraft.block.NeutronIrradiatorBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class NeutronIrradiatorBlockEntity extends BlockEntity {
    public static final int RANGE = 7;

    public NeutronIrradiatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.NEUTRON_IRRADIATOR.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NeutronIrradiatorBlockEntity blockEntity) {
        if (level.getGameTime() % 2 != 0) return;

        // 定义检测范围 - 中子辐照器自身及上方7格范围
        AABB detectionArea = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + RANGE + 1, pos.getZ() + 1);
        // 获取范围内的所有实体
        level.getEntitiesOfClass(
            Entity.class,
            detectionArea,
            entity -> entity instanceof ItemEntity || entity instanceof FallingBlockEntity || entity instanceof LivingEntity
        ).forEach(entity -> {
            // 铁砧瞬间落地且10倍fallDistance
            if (entity instanceof FallingBlockEntity fallingBlock) {
                if (fallingBlock.getBlockState().getBlock() instanceof AnvilBlock) {
                    double distanceToIrradiator = entity.getY() - (pos.getY() + 0.9);
                    entity.setDeltaMovement(entity.getDeltaMovement().x, -7, entity.getDeltaMovement().z);
                    entity.fallDistance += (float) (10 * Math.abs(distanceToIrradiator));
                }
            }
        });
    }

    public static NeutronIrradiatorBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new NeutronIrradiatorBlockEntity(pos, blockState);
    }

    public static boolean isInIrradiatorRange(Level level, BlockPos pos) {
        // 检查下方最多7格内是否有中子辐照器
        for (int i = 0; i <= RANGE; i++) {
            BlockPos checkPos = pos.below(i);
            BlockState blockState = level.getBlockState(checkPos);

            if (blockState.getBlock() instanceof NeutronIrradiatorBlock) {
                BlockEntity blockEntity = level.getBlockEntity(checkPos);
                if (blockEntity != null && blockEntity.getType() == ModBlockEntities.NEUTRON_IRRADIATOR.get()) {
                    return true;
                }
            }
        }
        return false;
    }
}