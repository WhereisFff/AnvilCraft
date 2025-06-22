package dev.dubhe.anvilcraft.api.block;

public interface INegativeMatterBlock extends INegativeShapeBlock<INegativeMatterBlock> {
    @Override
    default Class<INegativeMatterBlock> getBlockType() {
        return INegativeMatterBlock.class;
    }
}
