package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.IExtraItemDisplay;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItems;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

@Getter
@MethodsReturnNonnullByDefault
public class CannedFoodItem extends Item implements IExtraItemDisplay {

    private final Holder<Item> canItem;

    public CannedFoodItem(Properties properties, Holder<Item> canItem) {
        super(properties);
        this.canItem = canItem;
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        if (!stack.has(ModComponents.DISPLAY_ITEM)) {
            this.setFood(stack, ModItems.BEEF_MUSHROOM_STEW.asStack());
        }
        super.verifyComponentsAfterLoad(stack);
    }

    @Override
    public ItemStack getDisplayedItem(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModComponents.DISPLAY_ITEM))
            .map(StoredItem::stored)
            .orElse(ItemStack.EMPTY);
    }

    public ItemStack setFood(ItemStack canStack, ItemStack foodStack) {
        ItemStack displayStack = foodStack.copy();
        if (displayStack.has(DataComponents.RARITY)) {
            canStack.set(DataComponents.RARITY, displayStack.get(DataComponents.RARITY));
        }
        canStack.set(ModComponents.DISPLAY_ITEM, new StoredItem(displayStack));
        FoodProperties copiedFood = displayStack.getFoodProperties(null);
        if (copiedFood != null) {
            canStack.set(DataComponents.FOOD, new FoodProperties.Builder()
                .nutrition(copiedFood.nutrition())
                .saturationModifier(copiedFood.saturation() / (copiedFood.nutrition() * 2.0f))
                .usingConvertsTo(this.canItem.value())
                .fast()
                .build());
        }
        return canStack;
    }

    @Override
    public int xOffset(ItemStack stack) {
        return 5;
    }

    @Override
    public int yOffset(ItemStack stack) {
        return 2;
    }

    @Override
    public float scale(ItemStack stack) {
        return 0.5f;
    }
}
