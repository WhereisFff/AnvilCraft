package dev.dubhe.anvilcraft.integration.appeng.data;

import com.tterrag.registrate.providers.ProviderType;
import dev.anvilcraft.lib.integration.Integration;
import dev.anvilcraft.lib.integration.IntegrationType;
import dev.dubhe.anvilcraft.AnvilCraft;

@Integration(value = "ae2", type = IntegrationType.DATA)
public class AppEngDataCompat {
    public void applyData() {
        AnvilCraft.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, AppEngBlockTagLoader::init);
    }
}
