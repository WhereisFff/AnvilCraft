package dev.dubhe.anvilcraft.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedhotMetalBlock extends Block {
    private final float steppingHarmAmount;

    public RedhotMetalBlock(Properties properties) {
        super(properties);
        this.steppingHarmAmount = 1;
    }

    public RedhotMetalBlock(Properties properties, float steppingHarmAmount) {
        super(properties);
        this.steppingHarmAmount = steppingHarmAmount;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isSteppingCarefully()
            && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), steppingHarmAmount);
        }
        super.stepOn(level, pos, state, entity);
    }
}
