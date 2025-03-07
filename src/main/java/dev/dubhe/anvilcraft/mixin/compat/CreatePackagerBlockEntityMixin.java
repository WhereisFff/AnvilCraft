package dev.dubhe.anvilcraft.mixin.compat;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.dubhe.anvilcraft.api.itemhandler.FilteredItemStackHandler;
import dev.dubhe.anvilcraft.api.itemhandler.ItemHandlerUtil;
import dev.dubhe.anvilcraft.block.entity.BatchCrafterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackagerBlockEntity.class)
@Debug(export = true)
abstract class CreatePackagerBlockEntityMixin extends SmartBlockEntity {

    @Shadow
    public int animationTicks;

    @Shadow
    public ItemStack previouslyUnwrapped;

    @Shadow
    public boolean animationInward;

    @Shadow
    @Final
    public static int CYCLE;

    public CreatePackagerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Inject(
        method = "unwrapBox",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private void batchCrafterSupport(ItemStack box, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (animationTicks > 0) {
            cir.setReturnValue(false);
        }
        BlockEntity targetBE = level.getBlockEntity(worldPosition.relative(getBlockState().getOptionalValue(PackagerBlock.FACING)
            .orElse(Direction.UP)
            .getOpposite()));
        if (targetBE instanceof BatchCrafterBlockEntity) {
            ItemStackHandler contents = PackageItem.getContents(box);
            PackageOrder orderContext = PackageItem.getOrderContext(box);
            if (orderContext != null && targetBE instanceof BatchCrafterBlockEntity batchCrafter) {
                FilteredItemStackHandler itemHandler = batchCrafter.getItemHandler();
                // 有物品或者过滤开了就不接受包裹物品
                if (itemHandler.isFilterEnabled() || !itemHandler.isEmpty()) {
                    cir.setReturnValue(false);
                    return;
                }
                int itemCount = ItemHandlerUtil.countItemsInHandler(contents);
                int craftSlots = 0;
                int minStackSize = 64;
                for (int slot = 0; slot < orderContext.stacks().size(); slot++) {
                    ItemStack stack = orderContext.stacks().get(slot).stack;
                    if (!stack.isEmpty()) {
                        craftSlots++;
                        minStackSize = Math.min(stack.getMaxStackSize(), minStackSize);
                    }
                }
                if (craftSlots * minStackSize < itemCount) {
                    cir.setReturnValue(false);
                    return;
                }
                if (simulate) {
                    cir.setReturnValue(true);
                    return;
                }
                for (var entry : ItemHandlerUtil.mergeHandlerItems(contents).object2IntEntrySet()) {
                    Item toInsertItem = entry.getKey();
                    int toInsertCount = entry.getIntValue();
                    if (toInsertCount <= 0) {
                        continue;
                    }
                    while (toInsertCount > 0) {
                        for (int slot = 0; slot < orderContext.stacks().size(); slot++) {
                            BigItemStack bigItemStack = orderContext.stacks().get(slot);
                            if (toInsertCount > 0 && bigItemStack.stack.is(toInsertItem)) {
                                ItemStack itemInSlot = itemHandler.getStackInSlot(slot);
                                if (itemInSlot.isEmpty()) {
                                    itemHandler.setStackInSlot(slot, toInsertItem.getDefaultInstance());
                                } else {
                                    itemInSlot.grow(1);
                                }
                                toInsertCount--;
                            }
                        }
                    }
                }
                batchCrafter.craft(level);
                previouslyUnwrapped = box;
                animationInward = true;
                animationTicks = CYCLE;
                notifyUpdate();
                cir.setReturnValue(true);
            }
        }
    }
}
