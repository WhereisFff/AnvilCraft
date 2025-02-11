package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EmberAnvilHammerItem extends AnvilHammerItem {
    /**
     * 初始化铁砧锤
     *
     * @param properties 物品属性
     */
    public EmberAnvilHammerItem(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    protected float getAttackDamageModifierAmount() {
        return 9;
    }

    @Override
    public Block getAnvil(){
        return ModBlocks.EMBER_ANVIL.get();
    }

    @Override
    protected float calculateFallDamageBonus(float fallDistance) {
        return Math.min(120, fallDistance * 2);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack defaultInstance = super.getDefaultInstance();

        defaultInstance.set(ModComponents.FIRE_REFORGING, new ToolAttributes.FireReforging());
        defaultInstance.set(ModComponents.TOUGH, new ToolAttributes.Tough());

        return defaultInstance;
    }
}
