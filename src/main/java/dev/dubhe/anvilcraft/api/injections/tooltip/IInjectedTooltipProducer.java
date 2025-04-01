package dev.dubhe.anvilcraft.api.injections.tooltip;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 头戴铁砧锤时需要显示tooltip，用于接口注入至原版内容
 */
@MethodsReturnNonnullByDefault
public interface IInjectedTooltipProducer {
    /**
     * 获取需要渲染的tooltip
     *
     * @return 一个按顺序存储了所有需要渲染的tooltip的List
     * @see IInjectedTooltipProducer#anvilcraft$getTooltip(BlockState)
     */
    default List<Component> anvilcraft$getTooltip() {
        return List.of();
    }

    /**
     * （用于Block）获取需要渲染的tooltip
     *
     * @return 一个按顺序存储了所有需要渲染的tooltip的List
     * @see IInjectedTooltipProducer#anvilcraft$getTooltip()
     */
    default List<Component> anvilcraft$getTooltip(BlockState state) {
        return List.of();
    }
}
