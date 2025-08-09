package dev.dubhe.anvilcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

public class GunpowderBlock extends Block {
    public GunpowderBlock(Properties properties) {
        super(properties);
    }

    /**
     * 不要把这个方法修改的和 {@link dev.dubhe.anvilcraft.block.GunpowderBlock#explosion(Level, BlockPos)} 方法一样，不然不会正常工作
     */
    public void onHit(Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        level.explode(null,
            null,
            new ExplosionDamageCalculator() {
                @Override
                public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
                    return Optional.of(Float.MAX_VALUE);
                }

                @Override
                public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                    return false;
                }
            },
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            4.0F,
            false,
            Level.ExplosionInteraction.BLOCK
        );
    }

    public void explosion(Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        level.explode(null,
            null,
            new ExplosionDamageCalculator() {
                @Override
                public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
                    return Optional.of(Float.MAX_VALUE);
                }

                @Override
                public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
                    return false;
                }
            },
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            4.0f,
            false,
            Level.ExplosionInteraction.BLOCK,
            true,
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            SoundEvents.GENERIC_EXPLODE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            explosion(level, pos);
            Item item = stack.getItem();
            if (stack.is(Items.FLINT_AND_STEEL)) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            } else {
                stack.consume(1, player);
            }
            player.awardStat(Stats.ITEM_USED.get(item));
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
