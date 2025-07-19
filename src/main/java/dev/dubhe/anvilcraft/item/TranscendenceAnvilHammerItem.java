package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.api.item.property.Eternal;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TranscendenceAnvilHammerItem extends AnvilHammerItem {
    /**
     * 初始化铁砧锤
     *
     * @param properties 物品属性
     */
    public TranscendenceAnvilHammerItem(Properties properties) {
        super(properties.fireResistant().component(ModComponents.ETERNAL, Eternal.INSTANCE));
    }

    @Override
    protected float getAttackDamageModifierAmount() {
        return 13;
    }

    @Override
    public Block getAnvil() {
        return ModBlocks.TRANSCENDENCE_ANVIL.get();
    }

    @Override
    protected float calculateFallDamageBonus(float fallDistance) {
        return fallDistance * 2;
    }
}
