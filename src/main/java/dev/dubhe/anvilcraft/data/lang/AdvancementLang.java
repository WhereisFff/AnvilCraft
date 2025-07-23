package dev.dubhe.anvilcraft.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

public class AdvancementLang {
    /**
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateLangProvider provider) {
        provider.add("advancements.anvilcraft.root.title", "Welcome to AnvilCraft");
        provider.add("advancements.anvilcraft.root.description", "Pick up the anvil, start from the vanilla, and enter the world of technology and magic");

        provider.add("advancements.anvilcraft.crab_claw.title", "Win half of the resurrection race");
        provider.add("advancements.anvilcraft.crab_claw.description", "Obtain crab claws from the crab trap");

        provider.add("advancements.anvilcraft.placer.title", "Placer place placer");
        provider.add("advancements.anvilcraft.placer.description", "Use the block placer to place the block placer");

        provider.add("advancements.anvilcraft.devourer.title", "Too many tongue twisters");
        provider.add("advancements.anvilcraft.devourer.description", "Use the block devourer to devour the block devourer");
    }
}
