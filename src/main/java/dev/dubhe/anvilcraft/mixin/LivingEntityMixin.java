package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.dubhe.anvilcraft.api.item.property.BoxContents;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModLootTables;
import dev.dubhe.anvilcraft.item.amulet.AmuletBoxItem;
import net.minecraft.advancements.critereon.UsedTotemTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
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
        LivingEntity thiz = (LivingEntity) (Object) this;
        LootTable beheadingLoot = ModLootTables.getBeheadingLoot(thiz);
        if (beheadingLoot == LootTable.EMPTY) return;
        beheadingLoot.getRandomItems(lootParams, thiz.getLootTableSeed(), thiz::spawnAtLocation);
    }

    @WrapOperation(
        method = "checkTotemDeathProtection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
        )
    )
    private boolean redirectTotemCheck(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item)
            || (instance.is(ModItems.AMULET_BOX)
                && !instance.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY).getTotems().isEmpty());
    }

    @WrapOperation(
        method = "checkTotemDeathProtection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/UsedTotemTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void onlyUseTotemToTrigger(UsedTotemTrigger instance, ServerPlayer player, ItemStack item, Operation<Void> original) {
        if (item.getItem() instanceof AmuletBoxItem) {
            original.call(instance, player, Items.TOTEM_OF_UNDYING.getDefaultInstance());
        }
        original.call(instance, player, item);
    }


    @WrapOperation(
        method = "checkTotemDeathProtection",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void shrinkCorrect(ItemStack instance, int decrement, Operation<Void> original) {
        if (instance.is(ModItems.AMULET_BOX) && instance.has(ModComponents.BOX_CONTENTS)) {
            BoxContents contents = instance.get(ModComponents.BOX_CONTENTS);
            @SuppressWarnings("DataFlowIssue") // 当运行这段代码时，contents一定是非null的
            BoxContents.Mutable mutable = contents.mutable();
            mutable.popTotem();
            instance.set(ModComponents.BOX_CONTENTS, mutable.immutable());
            return;
        }
        original.call(instance, decrement);
    }
}
