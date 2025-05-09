package dev.dubhe.anvilcraft.integration.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.AnvilCraft;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class AnvilCraftPonderTags {
    public static final ResourceLocation CRAFTING = AnvilCraft.of("crafting");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> tagHelper = helper.withKeyFunction(RegistryEntry::getId);

        tagHelper.registerTag(CRAFTING)
                .addToIndex()
                .item(Items.ANVIL)
                .title("crafting")
                .description("Use anvil to craft")
                .register();
    }
}
