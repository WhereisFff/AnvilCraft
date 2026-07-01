package dev.dubhe.anvilcraft.api.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * 只进不出的虚空流体容器。
 *
 * <p>接受任意流体的填充并直接丢弃（相当于创造流体储罐的只输入版本），
 * 永不存储、永不输出。用于门格海绵：通过管道输入的流体会被无限吸收并消失。
 */
public final class VoidFluidHandler implements IFluidHandler {
    public static final VoidFluidHandler INSTANCE = new VoidFluidHandler();

    private VoidFluidHandler() {
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return resource.getAmount();
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
