package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.anvilcraft.api.totem.TotemManager;
import dev.dubhe.anvilcraft.api.totem.handler.TotemHandler;
import dev.dubhe.anvilcraft.init.ModLootTables;
import dev.dubhe.anvilcraft.init.ModMobEffects;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.EffectCure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);

    private LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "dropFromLootTable",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"
        )
    )
    private void dropBeheadingLoot(
        DamageSource damageSource,
        boolean hitByPlayer,
        CallbackInfo ci,
        @Local LootParams lootParams) {
        LivingEntity thiz = Util.cast(this);
        LootTable beheadingLoot = ModLootTables.getBeheadingLoot(thiz);
        if (beheadingLoot == LootTable.EMPTY) return;
        beheadingLoot.getRandomItems(lootParams, thiz.getLootTableSeed(), thiz::spawnAtLocation);
    }

    /**
     * @author burin
     * @reason 添加其他图腾
     */
    @Overwrite
    private boolean checkTotemDeathProtection(DamageSource damageSource) {
        LivingEntity self = (LivingEntity) (Object) this;
        Map<Item, TotemHandler> totemMap = TotemManager.INSTANCE.getTotemMap();
        for(InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = this.getItemInHand(hand);
            for (Item item : totemMap.keySet()) {
                if (itemStack.is(item) && CommonHooks.onLivingUseTotem(self, damageSource, itemStack, hand)) {
                    TotemHandler handler = totemMap.get(item);
                    boolean result = handler.execute(damageSource, self, itemStack);
                    handler.shrink(itemStack);
                    return result;
                }
            }
        }
        return false;
    }

    @Inject(
        method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"
        ),
        cancellable = true
    )
    private void preventAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (this.hasEffect(ModMobEffects.RAGE)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(
        method = "removeEffectsCuredBy",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
        )
    )
    private boolean preventRemovalRageEffect(Set<EffectCure> instance, Object o, Operation<Boolean> original, @Local MobEffectInstance effect) {
        return original.call(instance, o) && !effect.is(ModMobEffects.RAGE);
    }

    @Inject(
        method = "hurt",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private void invulnerableEffect(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.hasEffect(ModMobEffects.INVULNERABLE)) {
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                cir.setReturnValue(false);
            }
        }
    }
}
