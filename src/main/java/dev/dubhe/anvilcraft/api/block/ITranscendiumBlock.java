package dev.dubhe.anvilcraft.api.block;

public interface ITranscendiumBlock extends INegativeShapeBlock<ITranscendiumBlock> {
    @Override
    default Class<ITranscendiumBlock> getBlockType() {
        return ITranscendiumBlock.class;
    }
}
