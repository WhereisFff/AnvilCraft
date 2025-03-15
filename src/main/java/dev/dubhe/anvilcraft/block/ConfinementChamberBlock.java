package dev.dubhe.anvilcraft.block;

import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.entity.ConfinementChamberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfinementChamberBlock extends BaseEntityBlock {
    public ConfinementChamberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ConfinementChamberBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new ConfinementChamberBlockEntity(blockPos, blockState);
    }
    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack,
            @NotNull BlockState state,
            @NotNull Level level,
            @NotNull BlockPos pos,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ConfinementChamberBlockEntity confinementChamberBlockEntity)) return ItemInteractionResult.FAIL;
        ItemStack itemStack = confinementChamberBlockEntity.getItemHandler().getStackInSlot(0);
        ItemStack handItemStack = player.getItemInHand(hand);
        if (itemStack.is(handItemStack.getItem())) return ItemInteractionResult.FAIL;
        player.setItemInHand(hand, itemStack.copy());
        confinementChamberBlockEntity.getItemHandler().setStackInSlot(0, handItemStack.copy());
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
