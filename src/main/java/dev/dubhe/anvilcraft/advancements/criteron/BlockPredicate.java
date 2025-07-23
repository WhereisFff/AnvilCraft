package dev.dubhe.anvilcraft.advancements.criteron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;

public record BlockPredicate(HolderSet<Block> blocks) implements Predicate<Block> {
    public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
        RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(BlockPredicate::blocks)
    ).apply(instance, BlockPredicate::new));

    @Override
    public boolean test(Block block) {
        return block.defaultBlockState().is(this.blocks.get(0).value());
    }

    public static class Builder {
        private HolderSet<Block> blocks = HolderSet.empty();

        private Builder() {}

        public static Builder block() {
            return new Builder();
        }

        public Builder block(Block block) {
            this.blocks = HolderSet.direct((b) -> b.defaultBlockState().getBlockHolder(), block);
            return this;
        }

        public Builder tag(TagKey<Block> tag) {
            this.blocks = BuiltInRegistries.BLOCK.getOrCreateTag(tag);
            return this;
        }

        public BlockPredicate build() {
            return new BlockPredicate(this.blocks);
        }
    }
}
