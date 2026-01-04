package dev.dubhe.anvilcraft.inventory;

import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.item.abnormal.IAbnormal;
import dev.dubhe.anvilcraft.item.abnormal.ICursed;
import dev.dubhe.anvilcraft.util.anvil.AnvilMenuResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;

public class FrostAnvilMenu extends AnvilMenu {
    private final AnvilMenuResult result = AnvilMenuResult.builder()
        .ignoreEnchantmentCompatible()
        .noTaxInRepairUsingItem()
        .useNewRepairCostAlgorithm()
        .create();

    public FrostAnvilMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory);
    }

    public FrostAnvilMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.FROST_ANVIL.get();
    }

    @Override
    public void createResult() {
        ItemStack inputLeft = this.getSlot(0).getItem();
        ItemStack inputRight = this.getSlot(1).getItem();
        this.result.createResult(
            this.player,
            inputLeft,
            inputRight,
            this.itemName,
            tax -> CommonHooks.onAnvilChange(this, inputLeft, inputRight, this.resultSlots, this.itemName, tax, this.player)
        );
        this.resultSlots.setItem(0, this.result.result);
        this.cost.set(this.result.xpCost);
        this.repairItemCountCost = this.result.repairItemCountCost;
    }
}
