package dev.dubhe.anvilcraft.api.power;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 当电网剩余电量大于等于消耗电量时才开始工作的电网组件
 */
public interface ILoadAwareConsumer extends IPowerConsumer {
    AtomicBoolean active = new AtomicBoolean(false);

    default AtomicBoolean getActive() {
        return active;
    }
}
