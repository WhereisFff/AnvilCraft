package dev.dubhe.anvilcraft.advancements.criteron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModCriterionTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class BlockCompressingRecipeTrigger extends SimpleCriterionTrigger<BlockCompressingRecipeTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Item input, Item output) {
        this.trigger(player, (instance) -> instance.matches(input, output));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> inputItem, Optional<ItemPredicate> outputItem) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            ItemPredicate.CODEC.optionalFieldOf("input").forGetter(TriggerInstance::inputItem),
            ItemPredicate.CODEC.optionalFieldOf("output").forGetter(TriggerInstance::outputItem)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> blockCompressing(Item input, Item output) {
            return blockCompressing(ItemPredicate.Builder.item().of(input), ItemPredicate.Builder.item().of(output));
        }

        public static Criterion<TriggerInstance> blockCompressing(ItemPredicate.Builder input, ItemPredicate.Builder output) {
            return ModCriterionTriggers.BLOCK_COMPRESSING.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(input.build()), Optional.of(output.build())));
        }

        public boolean matches(Item input, Item output) {
            return this.inputItem.isEmpty()
                || this.outputItem.isEmpty()
                || (this.inputItem.get().test(input.getDefaultInstance())
                && this.outputItem.get().test(output.getDefaultInstance()));
        }
    }
}
