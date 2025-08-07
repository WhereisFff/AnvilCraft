package dev.dubhe.anvilcraft.block;

import dev.dubhe.anvilcraft.api.chargecollector.ChargeCollectorManager;
import dev.dubhe.anvilcraft.block.entity.ChargeCollectorBlockEntity;
import dev.dubhe.anvilcraft.block.state.FragmentationDegree;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SugarBlock extends Block {
    public static final EnumProperty<FragmentationDegree> FRAGMENTATION_DEGREE = EnumProperty.create("fragmentation_degree", FragmentationDegree.class);

    public SugarBlock(Properties properties) {
        super(properties);
        this.stateDefinition.any().setValue(FRAGMENTATION_DEGREE, FragmentationDegree.ZERO);
    }

    public void onHit(Level level, BlockPos pos) {
        level.scheduleTick(pos, this, 2);
        Collection<ChargeCollectorManager.Entry> chargeCollectorCollection =
            ChargeCollectorManager.getInstance(level).getNearestChargeCollect(pos);
        double surplus = 1;
        for (ChargeCollectorManager.Entry entry : chargeCollectorCollection) {
            ChargeCollectorBlockEntity chargeCollectorBlockEntity = entry.getBlockEntity();
            if (!ChargeCollectorManager.getInstance(level).canCollect(chargeCollectorBlockEntity, pos)) return;
            surplus = chargeCollectorBlockEntity.incomingCharge(surplus, pos);
            if (surplus == 0) return;
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        double chance = random.nextDouble();
        if (!level.isClientSide) {
            if (state.getValue(FRAGMENTATION_DEGREE) != FragmentationDegree.THREE) {
                if (chance <= 0.05) {
                    level.setBlockAndUpdate(pos, state.setValue(FRAGMENTATION_DEGREE, state.getValue(FRAGMENTATION_DEGREE).next()));
                }
            } else {
                if (chance <= 0.05) {
                    destroy(level, pos, state);
                }
            }
        }
    }

    public void destroy(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        if (state.getValue(FRAGMENTATION_DEGREE) == FragmentationDegree.THREE) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.SUGAR, 9));
            level.addFreshEntity(itemEntity);
        } else {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), ModBlocks.SUGAR_BLOCK.asStack());
            level.addFreshEntity(itemEntity);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!player.isCreative()) {
            destroy(level, pos, state);
            return true;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FRAGMENTATION_DEGREE, FragmentationDegree.ZERO);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FRAGMENTATION_DEGREE);
    }
}
