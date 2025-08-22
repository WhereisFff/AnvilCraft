package dev.dubhe.anvilcraft.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.anvilcraft.lib.config.ConfigData;
import dev.dubhe.anvilcraft.config.AnvilCraftClientConfig;
import dev.dubhe.anvilcraft.config.AnvilCraftServerConfig;
import org.jetbrains.annotations.NotNull;

public class ConfigScreenLang {
    /**
     * 初始化配置语言
     *
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateLangProvider provider) {
        ConfigData.readConfigClass(provider, AnvilCraftServerConfig.class);
        ConfigData.readConfigClass(provider, AnvilCraftClientConfig.class);
    }
}
