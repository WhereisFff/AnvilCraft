package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.item.property.component.PillBocContents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class PillBoxItem extends Item {
    public PillBoxItem(Properties properties) {
        super(properties.component(ModComponents.PILL_BOC_CONTENTS, PillBocContents.EMPTY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        PillBocContents contents = itemStack.getOrDefault(ModComponents.PILL_BOC_CONTENTS, PillBocContents.EMPTY);
        if (contents.pills().isEmpty()) {
            return InteractionResultHolder.pass(itemStack);
        }
        PillBocContents.Mutable mutable = contents.mutable();
        mutable.useAll(player);
        itemStack.set(ModComponents.PILL_BOC_CONTENTS, mutable.immutable());
        player.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public boolean overrideOtherStackedOnMe(
        ItemStack stack,
        ItemStack other,
        Slot slot,
        ClickAction action,
        Player player,
        SlotAccess access
    ) {
        if (!slot.allowModification(player)) {
            return false;
        }
        PillBocContents contents = stack.getOrDefault(ModComponents.PILL_BOC_CONTENTS, PillBocContents.EMPTY);
        PillBocContents.Mutable mutable = contents.mutable();
        if (other.isEmpty()) {
            Optional<ItemStack> stackOptional = mutable.get();
            if (stackOptional.isPresent()) {
                ItemStack itemStack = stackOptional.get();
                if (!itemStack.isEmpty()) {
                    access.set(itemStack);
                    stack.set(ModComponents.PILL_BOC_CONTENTS, mutable.immutable());
                    return true;
                }
            }
        } else if (mutable.insert(other)) {
            stack.set(ModComponents.PILL_BOC_CONTENTS, mutable.immutable());
            access.set(ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (!slot.allowModification(player)) {
            return false;
        }
        PillBocContents contents = stack.getOrDefault(ModComponents.PILL_BOC_CONTENTS, PillBocContents.EMPTY);
        PillBocContents.Mutable mutable = contents.mutable();
        ItemStack other = slot.getItem();
        if (other.isEmpty()) {
            Optional<ItemStack> stackOptional = mutable.get();
            if (stackOptional.isPresent()) {
                ItemStack itemStack = stackOptional.get();
                if (!itemStack.isEmpty()) {
                    slot.set(itemStack);
                    stack.set(ModComponents.PILL_BOC_CONTENTS, mutable.immutable());
                    return true;
                }
            }
        } else if (mutable.insert(other)) {
            stack.set(ModComponents.PILL_BOC_CONTENTS, mutable.immutable());
            slot.set(ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.literal("储存药片，右键时会把储存的药片各吃一片，在物品栏时也可以按 [V] 使用").withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.anvilcraft.press_key", "Shift").withStyle(ChatFormatting.GRAY));
        }
    }
}
