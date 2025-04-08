package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.api.injections.menu.IAbstractContainerMenuExtension;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements IAbstractContainerMenuExtension {
    @Shadow @Final private List<DataSlot> dataSlots;

    @Override
    public int anvilcraft$getData(int id) {
        return this.dataSlots.get(id).get();
    }
}
