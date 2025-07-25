package dev.dubhe.anvilcraft.data.tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.dubhe.anvilcraft.init.ModDamageTypeTags;
import dev.dubhe.anvilcraft.init.ModDamageTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import org.jetbrains.annotations.NotNull;

public class DamageTypeTagLoader {
    /**
     * 初始化伤害类型标签
     *
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateTagsProvider<DamageType> provider) {
        provider.addTag(ModDamageTypeTags.AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.EMERALD_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.TOPAZ_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.RUBY_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.SAPPHIRE_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.ANVIL_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.COMRADE_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.FEATHER_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.CAT_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.DOG_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.SILENCE_AMULET_VALID)
            .addOptionalTag(ModDamageTypeTags.COGWHEEL_AMULET_VALID);

        provider.addTag(ModDamageTypeTags.EMERALD_AMULET_VALID);

        provider.addTag(ModDamageTypeTags.TOPAZ_AMULET_VALID)
            .addTag(DamageTypeTags.IS_LIGHTNING)
            .addOptional(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "wire_shock"));

        provider.addTag(ModDamageTypeTags.RUBY_AMULET_VALID)
            .addTag(DamageTypeTags.IS_FIRE)
            .addOptional(ModDamageTypes.LASER.location());

        provider.addTag(ModDamageTypeTags.SAPPHIRE_AMULET_VALID)
            .addTag(DamageTypeTags.IS_DROWNING)
            .add(DamageTypes.DRY_OUT);

        provider.addTag(ModDamageTypeTags.ANVIL_AMULET_VALID)
            .add(DamageTypes.FALLING_ANVIL);

        provider.addTag(ModDamageTypeTags.COMRADE_AMULET_VALID);

        provider.addTag(ModDamageTypeTags.FEATHER_AMULET_VALID)
            .addTag(DamageTypeTags.IS_FALL);

        provider.addTag(ModDamageTypeTags.CAT_AMULET_VALID);

        provider.addTag(ModDamageTypeTags.DOG_AMULET_VALID);

        provider.addTag(ModDamageTypeTags.SILENCE_AMULET_VALID);

        provider.addTag(ModDamageTypeTags.COGWHEEL_AMULET_VALID);
    }
}
