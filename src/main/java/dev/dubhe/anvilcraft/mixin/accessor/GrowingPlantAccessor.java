package dev.dubhe.anvilcraft.mixin.accessor;

import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GrowingPlantBlock.class)
public interface GrowingPlantAccessor {
    @Invoker
    GrowingPlantHeadBlock invokeGetHeadBlock();
}
