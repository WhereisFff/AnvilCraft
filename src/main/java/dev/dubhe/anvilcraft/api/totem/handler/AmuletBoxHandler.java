package dev.dubhe.anvilcraft.api.totem.handler;

import dev.dubhe.anvilcraft.api.item.property.BoxContents;
import dev.dubhe.anvilcraft.api.totem.TotemManager;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;


public class AmuletBoxHandler implements TotemHandler {
    private boolean result = false;

    @Override
    public TotemHandler execute(DamageSource damageSource, LivingEntity entity, ItemStack totemItem) {
        Map<Item, TotemHandler> totemMap = TotemManager.INSTANCE.getTotemMap();
        List<ItemStack> totems = totemItem.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY).getTotems();
        if (!totems.isEmpty()) {
            for (Item item : totemMap.keySet()) {
                if (totems.getFirst().is(item)) {
                    result = totemMap.get(item).execute(damageSource, entity, totems.getFirst()).getResult();
                }
            }
        }
        result = false;
        return this;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public TotemHandler shrink(ItemStack totemItem) {
        BoxContents boxContents = totemItem.get(ModComponents.BOX_CONTENTS);
        BoxContents.Mutable mutable = boxContents.mutable();
        mutable.popTotem();
        totemItem.set(ModComponents.BOX_CONTENTS, mutable.immutable());
        return this;
    }

    @Override
    public boolean getResult() {
        return result;
    }
}
