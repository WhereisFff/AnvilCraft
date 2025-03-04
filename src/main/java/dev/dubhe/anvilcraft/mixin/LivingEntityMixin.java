package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModLootTables;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow public abstract ItemStack getOffhandItem();

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

    @Redirect(
        method = "checkTotemDeathProtection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
        )
    )
    private boolean alsoCheckAmuletBox(ItemStack instance, Item item) {
        return instance.is(item) || instance.is(ModItems.AMULET_BOX);
    }
    @ModifyArg(
        method = "checkTotemDeathProtection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/stats/StatType;get(Ljava/lang/Object;)Lnet/minecraft/stats/Stat;"
        )
    )
    private Object alsoAwardAmuletBoxStat(Object value, @Local ItemStack stack) {
        return stack.getItem();
    }
    @ModifyArg(
        method = "checkTotemDeathProtection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/critereon/UsedTotemTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V"
        ),
        index = 1
    )
    private ItemStack onlyUseTotemToTrigger(ItemStack stack) {
        return Items.TOTEM_OF_UNDYING.getDefaultInstance();
    }
    @ModifyVariable(
        method = "checkTotemDeathProtection",
        at = @At(
            value = "STORE",
            ordinal = 1
        ),
        ordinal = 0
    )
    private ItemStack firstUseTotem(
        ItemStack originalCopy, @Local(name = "itemstack1") ItemStack original, @Share("consumed") LocalRef<ItemStack> ref
    ) {
        ref.set(original);
        if (originalCopy.is(Items.TOTEM_OF_UNDYING)) {
            return originalCopy;
        } else if (originalCopy.is(ModItems.AMULET_BOX.asItem())) {
            if (this.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                ref.set(this.getOffhandItem());
            } else if (((Object) this) instanceof Player player) {
                Optional<ItemStack> optional = Optional.of(InventoryUtil.getFirstItem(player.getInventory(), Items.TOTEM_OF_UNDYING))
                    .filter(stack -> stack != ItemStack.EMPTY);
                if (optional.isPresent()) {
                    ref.set(optional.get());
                } else {
                    return originalCopy;
                }
            } else {
                return originalCopy;
            }
        } else {
            return originalCopy;
        }

        return ref.get();
    }
    @Redirect(
        method = "checkTotemDeathProtection",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V")
    )
    private void shrinkCorrect(ItemStack instance, int decrement, @Share("consumed") LocalRef<ItemStack> ref) {
        ref.get().shrink(decrement);
    }
}
