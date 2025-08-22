package dev.dubhe.anvilcraft.advancements.criteron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModCriterionTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class InWorldTimewarpRecipeTrigger extends SimpleCriterionTrigger<InWorldTimewarpRecipeTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceLocation id) {
        this.trigger(player, (instance) -> instance.matches(id));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> id) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
            ResourceLocation.CODEC.optionalFieldOf("id").forGetter(TriggerInstance::id)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> timeWrap(ResourceLocation id) {
            return ModCriterionTriggers.IN_WORLD_TIME_WARP_RECIPE.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(id)));
        }

        public static Criterion<TriggerInstance> timeWrap() {
            return ModCriterionTriggers.IN_WORLD_TIME_WARP_RECIPE.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public boolean matches(ResourceLocation id) {
            if (this.id.isEmpty()) {
                return true;
            } else {
                return this.id.map(resourceLocation -> resourceLocation.equals(id)).orElse(false);
            }
        }
    }
}
