package dev.dubhe.anvilcraft.block.cfa.item;

import dev.dubhe.anvilcraft.block.item.FlexibleMultiPartBlockItem;
import dev.dubhe.anvilcraft.block.multipart.FlexibleMultiPartBlock;
import dev.dubhe.anvilcraft.block.state.DirectionGate331PartHalf;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class CelestialForgingAnvilPortalBlockItem
    extends FlexibleMultiPartBlockItem<DirectionGate331PartHalf, DirectionProperty, Direction> {
    public CelestialForgingAnvilPortalBlockItem(
        FlexibleMultiPartBlock<DirectionGate331PartHalf, DirectionProperty, Direction> block,
        Properties properties
    ) {
        super(block, properties);
    }
}
