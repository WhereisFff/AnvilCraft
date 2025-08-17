package dev.dubhe.anvilcraft.integration.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class AnvilCraftPonderTags {
    public static final ResourceLocation ANVIL = AnvilCraft.of("anvil");

    public static void register(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> tagHelper = helper.withKeyFunction(RegistryEntry::getId);

        tagHelper.registerTag(ANVIL)
            .addToIndex()
            .item(Items.ANVIL)
            .item(Items.CHIPPED_ANVIL)
            .item(Items.DAMAGED_ANVIL)
            .item(ModBlocks.EMBER_ANVIL)
            .item(ModBlocks.GIANT_ANVIL)
            .item(ModBlocks.ROYAL_ANVIL)
            .item(ModBlocks.SPECTRAL_ANVIL)
            .item(ModBlocks.TRANSCENDENCE_ANVIL)
            .title("Anvil")
            .description("Use anvil to craft")
            .register();
    }
}
