package dev.dubhe.anvilcraft.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

public class AdvancementLang {
    /**
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateLangProvider provider) {
        // region root
        provider.add("advancements.anvilcraft.root.title", "Welcome to AnvilCraft");
        provider.add("advancements.anvilcraft.root.description", "Pick up the anvil, start from the vanilla, and enter the world of technology and magic");
        // endregion

        // region crab claw line
        provider.add("advancements.anvilcraft.crab_claw.title", "Win half of the resurrection race");
        provider.add("advancements.anvilcraft.crab_claw.description", "Obtain crab claws");

        provider.add("advancements.anvilcraft.placer.title", "Placer place placer");
        provider.add("advancements.anvilcraft.placer.description", "Use the block placer to place the block placer");

        provider.add("advancements.anvilcraft.devourer.title", "Too many tongue twisters");
        provider.add("advancements.anvilcraft.devourer.description", "Use the block devourer to devour the block devourer");
        // endregion

        // region geode line
        provider.add("advancements.anvilcraft.geode.title", "A better start");
        provider.add("advancements.anvilcraft.geode.description", "Obtain geode, exploring amethyst geode with geode");

        provider.add("advancements.anvilcraft.amethyst_pickaxe.title", "Lucky pickaxe");
        provider.add("advancements.anvilcraft.amethyst_pickaxe.description", "Craft a amethyst pickaxe");

        provider.add("advancements.anvilcraft.topaz.title", "The power of lightning");
        provider.add("advancements.anvilcraft.topaz.description", "Summon lightning on the lightning rod with topaz");

        provider.add("advancements.anvilcraft.lifting_anvil.title", "Enjoyment from top to bottom");
        provider.add("advancements.anvilcraft.lifting_anvil.description", "Use a magnet block to lift and lower the anvil");
        // endregion

        // region anvil processing line
        provider.add("advancements.anvilcraft.dang.title", "Dang!");
        provider.add("advancements.anvilcraft.dang.description", "Perform anything anvil crafting");

        provider.add("advancements.anvilcraft.stone_crusher.title", "Stone crusher");
        provider.add("advancements.anvilcraft.stone_crusher.description", "Use an anvil to cobblestone to obtain gravel, then smash gravel to obtain sand");

        provider.add("advancements.anvilcraft.fossick.title", "Fossick");
        provider.add("advancements.anvilcraft.fossick.description", "Use an anvil to smash the sand on the scaffold to obtain gold nugget");

        provider.add("advancements.anvilcraft.ice_maker.title", "Ice maker");
        provider.add("advancements.anvilcraft.ice_maker.description", "Use an anvil to crush snow block on a medicine pot to make ice");

        provider.add("advancements.anvilcraft.four281.title", "4 to 81");
        provider.add("advancements.anvilcraft.four281.description", "Use an anvil to compress two pieces of ice to make packed ice, and then use an anvil to compress two pieces of packed ice to make blue ice");

        provider.add("advancements.anvilcraft.vanilla_iron_plate.title", "Vanilla iron plate");
        provider.add("advancements.anvilcraft.vanilla_iron_plate.description", "Smash iron ingots on the stamping platform to obtain heavy weighted pressure plate");

        provider.add("advancements.anvilcraft.recycling_diamonds.title", "Recycling diamonds");
        provider.add("advancements.anvilcraft.recycling_diamonds.description", "Tools and equipment for smashing diamonds on the crushing platform");

        provider.add("advancements.anvilcraft.all_in_one.title", "All in one");
        provider.add("advancements.anvilcraft.all_in_one.description", "Craft a hammer and try its various functions");

        provider.add("advancements.anvilcraft.hearts_of_iron.title", "Hearts of Iron");
        provider.add("advancements.anvilcraft.hearts_of_iron.description", "Craft a magnetoelectric core");

        provider.add("advancements.anvilcraft.not_beacon.title", "This is not a beacon");
        provider.add("advancements.anvilcraft.not_beacon.description", "Craft and place a charge collector");

        provider.add("advancements.anvilcraft.lighter.title", "Have you ever dismantled a lighter");
        provider.add("advancements.anvilcraft.lighter.description", "Use an anvil to smash a piezoelectric crystal and generate electricity");

        provider.add("advancements.anvilcraft.hammer.title", "When you have a hammer, you see everyone wants a nail");
        provider.add("advancements.anvilcraft.hammer.description", "Use an anvil hammer to kill mobs");

        provider.add("advancements.anvilcraft.super_kill.title", "Super kill... almost");
        provider.add("advancements.anvilcraft.super_kill.description", "Using a royal anvil hammer to deal 80 damage with one strike");
        // endregion

        // region automation line
        provider.add("advancements.anvilcraft.redstone_milker.title", "Redstone milker");
        provider.add("advancements.anvilcraft.redstone_milker.description", "Using a dispenser to milk cows");

        provider.add("advancements.anvilcraft.real_looting.title", "The real looting");
        provider.add("advancements.anvilcraft.real_looting.description", "Use an anvil to smash mob and obtain more drop item");

        provider.add("advancements.anvilcraft.iron_meter_reversal.title", "Iron meter reversal");
        provider.add("advancements.anvilcraft.iron_meter_reversal.description", "Use an anvil to smash the iron golem to obtain an iron ingot, and then use a dispenser to repair the iron golem");
        // endregion
    }
}
