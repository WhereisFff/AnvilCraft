package dev.dubhe.anvilcraft.data.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.jetbrains.annotations.NotNull;

public class PatchouliLang {

    /**
     * 初始化 Patchouli 语言
     *
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateLangProvider provider) {
        provider.add("message.anvilcraft.need_patchouli_installed", "Patchouli needs to be installed");

        provider.add("gui.anvilcraft.category.anvil_collision_craft_speed", "Speed: %d m/tick");

        provider.add("patchouli.anvilcraft.landing_text", "Welcome to AnvilCraft.");

        provider.add("title.anvilcraft.patchouli.advanced_features", "Advanced Features");
        provider.add("intro.anvilcraft.patchouli.advanced_features", "More complex, more stronger.");
        provider.add("title.anvilcraft.patchouli.basic_gameplay", "Basic Gameplay");
        provider.add("intro.anvilcraft.patchouli.basic_gameplay", "The basic gameplay of AnvilCraft mainly includes changes caused by the falling anvil, which can transform blocks, process items, affect mobs, etc.$(br)This mod adds some blocks to facilitate early automation, including block placer, chute, etc., which can be viewed in the $(thing)Basic More Device$() entry.");
        provider.add("title.anvilcraft.patchouli.machine_example", "Machine Example");
        provider.add("intro.anvilcraft.patchouli.machine_example", "Here are some examples of machines that might be used to help you better understand the components of Anvilcraft and design your own automated production line. Have fun! $(br2) This part of the manual was written and maintained by $(l:https://space.bilibili.com/347845823)Bilibili@毛绒绒Fff$(). If you have any issues with the machines or any suggestions, feel free to send a private message.");
        provider.add("title.anvilcraft.patchouli.power_system", "Power System");
        provider.add("intro.anvilcraft.patchouli.power_system", "Stronger automation.");
        provider.add("title.anvilcraft.patchouli.process", "Game Flow");
        provider.add("intro.anvilcraft.patchouli.process", "A general guide to the survival process, for reference only. In the case of a modpack, you will need to refer to the tasks or tutorials of the modpack itself. The mod process is not fixed, and this article is only the most general one.");
        provider.add("title.anvilcraft.patchouli.smithing_system", "Forging System");
        provider.add("intro.anvilcraft.patchouli.smithing_system", "Stronger anvil, forging and enchantments.");
        provider.add("title.anvilcraft.patchouli.special_props", "Special props");
        provider.add("intro.anvilcraft.patchouli.special_props", "Items and blocks with special functions.");
        provider.add("title.anvilcraft.patchouli.structural_engineering", "Structural Engineering");
        provider.add("intro.anvilcraft.patchouli.structural_engineering", "By building specific multi-block structures, we can create engineering machines with special functions. $(br2)$(#666666)Technology is not a rigid knowledge with only one solution, but a product that elevates us beyond the ordinary. Common people simply cannot understand our progress.$()");
        provider.add("title.anvilcraft.patchouli.technology_application", "Technology application");
        provider.add("intro.anvilcraft.patchouli.technology_application", "The production or transformation of new substances through the Anvilcraft technology.");


    }
}
