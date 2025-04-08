package dev.dubhe.anvilcraft.api.injections.menu;

import dev.dubhe.anvilcraft.api.injections.UnimplementedException;

public interface IAbstractContainerMenuExtension {
    default int anvilcraft$getData(int id) {
        throw new UnimplementedException("The method " + this.getClass().getName() + ".getData(int) is not implemented");
    }
}
