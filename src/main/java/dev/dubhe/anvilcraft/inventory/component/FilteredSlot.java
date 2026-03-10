package dev.dubhe.anvilcraft.inventory.component;

import dev.anvilcraft.lib.recipe.component.ItemIngredientPredicate;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 可过滤槽位，允许设置自定义过滤器
 *
 * <p>PS: 与{@link dev.dubhe.anvilcraft.item.FilterItem 过滤器（物品）}无关</p>
 */
@Getter
@Setter
public class FilteredSlot extends Slot {
    private ItemIngredientPredicate filter = RecipeUtil.EMPTY_ITEM_INGREDIENT;

    public FilteredSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.filter.testIgnoreCount(stack);
    }

    public boolean canCraft() {
        return this.filter.testCount(this.getItem().getCount());
    }

    public void resetFilter() {
        this.filter = RecipeUtil.EMPTY_ITEM_INGREDIENT;
    }
}
