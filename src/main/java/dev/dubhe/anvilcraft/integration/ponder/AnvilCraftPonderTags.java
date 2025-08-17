package dev.dubhe.anvilcraft.integration.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class AnvilCraftPonderTags {
    public static final ResourceLocation ANVIL = AnvilCraft.of("anvil");
    public static final ResourceLocation MAGNET_BLOCK = AnvilCraft.of("magnet_block");

    public static void register(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> registryTagHelper = helper.withKeyFunction(RegistryEntry::getId);
        PonderTagRegistrationHelper<Item> itemTagHelper = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

        helper.registerTag(ANVIL)
            .addToIndex()
            .item(Items.ANVIL, true, false)
            .title("Anvil")
            .description("Use anvil to craft")
            .register();

        helper.registerTag(MAGNET_BLOCK)
            .addToIndex()
            .item(ModBlocks.MAGNET_BLOCK, true, false)
            .title("Magnet")
            .description("Use magnet to attract the anvil")
            .register();


        itemTagHelper.addToTag(ANVIL)
            .add(Items.ANVIL)
            .add(Items.CHIPPED_ANVIL)
            .add(Items.DAMAGED_ANVIL);

        registryTagHelper.addToTag(ANVIL)
            .add(ModBlocks.ROYAL_ANVIL)
            .add(ModBlocks.EMBER_ANVIL)
            .add(ModBlocks.TRANSCENDENCE_ANVIL)
            .add(ModBlocks.SPECTRAL_ANVIL)
            .add(ModBlocks.GIANT_ANVIL);

        registryTagHelper.addToTag(MAGNET_BLOCK)
            .add(ModBlocks.MAGNET_BLOCK)
            .add(ModBlocks.HOLLOW_MAGNET_BLOCK)
            .add(ModBlocks.FERRITE_CORE_MAGNET_BLOCK);
    }
}
