package dev.dubhe.anvilcraft.integration.curios.data;

import dev.anvilcraft.lib.integration.Integration;
import dev.anvilcraft.lib.integration.IntegrationHook;
import dev.anvilcraft.lib.integration.IntegrationType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@Integration(value = "curios", type = IntegrationType.DATA)
public class CuriosData {
    public void applyData() {
        GatherDataEvent event = IntegrationHook.getEvent();
        DataGenerator generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeServer(), new ModCuriosProvider(packOutput, existingFileHelper, lookupProvider));
    }
}
