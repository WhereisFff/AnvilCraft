package dev.dubhe.anvilcraft.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.integration.ponder.AnvilCraftPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

public class PonderLang {
    public static void init(RegistrateLangProvider provider){
        PonderIndex.addPlugin(new AnvilCraftPonderPlugin());
        PonderIndex.getLangAccess().provideLang(AnvilCraft.MOD_ID, provider::add);
    }
}
