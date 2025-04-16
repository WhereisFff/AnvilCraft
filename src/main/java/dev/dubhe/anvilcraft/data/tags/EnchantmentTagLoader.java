package dev.dubhe.anvilcraft.data.tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.dubhe.anvilcraft.init.ModEnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class EnchantmentTagLoader {
    /**
     * 魔咒标签生成器初始化
     *
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateTagsProvider<Enchantment> provider) {
        provider.addTag(ModEnchantmentTags.TARGETED_DAMAGE)
            .add(Enchantments.SMITE)
            .add(Enchantments.BANE_OF_ARTHROPODS)
            .add(Enchantments.IMPALING);

        provider.addTag(ModEnchantmentTags.MODIFY_BLOCK_LOOT)
            .add(Enchantments.SILK_TOUCH)
            .add(Enchantments.FORTUNE);
    }
}
