package dev.dubhe.anvilcraft.advancements.criteron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModCriterionTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class AnvilHandleBlockTrigger extends SimpleCriterionTrigger<AnvilHandleBlockTrigger.TriggerInstance> {
    @Override
    public Codec<AnvilHandleBlockTrigger.TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Block block) {
        this.trigger(player, (instance) -> instance.matches(block));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<BlockPredicate> block) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            BlockPredicate.CODEC.optionalFieldOf("block").forGetter(TriggerInstance::block)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> handleBlock(Block block) {
            return handleBlock(BlockPredicate.Builder.block().block(block));
        }

        public static Criterion<TriggerInstance> handleBlock(BlockPredicate.Builder block) {
            return ModCriterionTriggers.ANVIL_HANDLE_BLOCK.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(block.build())));
        }

        public boolean matches(Block block) {
            return this.block.isEmpty() || this.block.get().test(block);
        }
    }
}
