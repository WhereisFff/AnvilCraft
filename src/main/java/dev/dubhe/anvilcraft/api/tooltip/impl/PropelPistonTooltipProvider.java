package dev.dubhe.anvilcraft.api.tooltip.impl;

import dev.dubhe.anvilcraft.block.PropelPiston;
import dev.dubhe.anvilcraft.block.entity.PropelPistonBlockEntity;
import dev.dubhe.anvilcraft.util.UnitUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class PropelPistonTooltipProvider extends PowerComponentTooltipProvider {
    @Override
    public boolean accepts(BlockEntity blockEntity) {
        return blockEntity instanceof PropelPistonBlockEntity;
    }

    @Override
    public List<Component> tooltip(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (level != null) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(PropelPiston.EXHAUSTED) && !state.getValue(PropelPiston.EXHAUSTED)) {
                return List.of();
            }
        }
        boolean original = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.isShiftKeyDown()) {
            original = true;
        }
        List<Component> tooltips = new ArrayList<>(super.tooltip(blockEntity));
        if (blockEntity instanceof PropelPistonBlockEntity propelPistonBlockEntity) {
            tooltips.add(Component.literal("推进活塞状态：").withStyle(ChatFormatting.BLUE));
            tooltips.add(Component.literal("  储存的能量：")
                .append(UnitUtil.energyUnit(propelPistonBlockEntity.getStoredEnergy(), original))
                .withStyle(ChatFormatting.GRAY));
        }
        return tooltips;
    }

    @Override
    public int priority() {
        return -1;
    }
}
