package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.init.entity.ModDamageTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin implements ModDamageTypes.DamageSourceExtra {
    @Shadow
    public abstract DamageSource source(ResourceKey<DamageType> damageTypeKey);

    @Unique
    private DamageSource anvilcraft$laser;
    @Unique
    private DamageSource anvilcraft$lostInTime;

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    void initModDamageSources(RegistryAccess registry, CallbackInfo ci) {
        this.anvilcraft$laser = this.source(ModDamageTypes.LASER);
        this.anvilcraft$lostInTime = this.source(ModDamageTypes.LOST_IN_TIME);
    }

    @Override
    public DamageSource anvilcraft$laser() {
        return this.anvilcraft$laser;
    }

    @Override
    public DamageSource anvilcraft$lostInTime() {
        return this.anvilcraft$lostInTime;
    }
}
