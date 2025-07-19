package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.util.mixin.ref.ProvidenceRef;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Shadow
    @Deprecated
    public abstract void getRandomItemsRaw(LootContext context, Consumer<ItemStack> output);

    @Shadow
    public abstract ResourceLocation getLootTableId();

    @ModifyArg(
        method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;"
                     + "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;"
                     + "Ljava/util/function/Consumer;)V"),
        index = 1)
    private Consumer<ItemStack> storeOutputConsumer(Consumer<ItemStack> output, @Share("output") LocalRef<Consumer<ItemStack>> ref) {
        ref.set(output);
        return output;
    }

    @Inject(
        method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;"
                     + "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;"
                     + "Ljava/util/function/Consumer;)V"))
    private void registerRef(
        LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir,
        @Share("shouldInvokeMultiple") LocalBooleanRef ref
    ) {
        ref.set(context.hasParam(LootContextParams.TOOL) && context.getParam(LootContextParams.TOOL).has(ModComponents.PROVIDENCE));
        if (!ref.get()) return;
        ProvidenceRef.register(this.getLootTableId());
    }

    // TODO: 强运实现不会多倍掉落普通方块
    @Inject(
        method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/level/storage/loot/LootTable;"
                     + "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;"
                     + "Ljava/util/function/Consumer;)V"))
    private void invokeMultipleForProvidence(
        LootContext context,
        CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir,
        @Share("shouldInvokeMultiple") LocalBooleanRef booleanRef,
        @Share("output") LocalRef<Consumer<ItemStack>> ref
    ) {
        if (!booleanRef.get()) return;
        if (!ProvidenceRef.shouldProvidence(this.getLootTableId())) return;
        RandomSource random = context.getRandom();
        if (random.nextFloat() > 0.25f) return;
        Consumer<ItemStack> output = ref.get();
        this.getRandomItemsRaw(context, output);
        if (random.nextFloat() > 0.05f) return;
        this.getRandomItemsRaw(context, output);
    }

}
