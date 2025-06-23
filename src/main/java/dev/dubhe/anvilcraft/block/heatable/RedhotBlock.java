package dev.dubhe.anvilcraft.block.heatable;

import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedhotBlock extends HeatableBlock {
    private final float steppingDamage;

    public RedhotBlock(Properties properties) {
        this(properties, 1);
    }

    protected RedhotBlock(Properties properties, float steppingDamage) {
        super(properties);
        this.steppingDamage = steppingDamage;
    }

    @Override
    public HeatableBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.REDHOT_BLOCK.create(pos, state);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully()
            && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), this.steppingDamage);
        }
        super.stepOn(level, pos, state, entity);
    }
}
