package dev.dubhe.anvilcraft.data.tags;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.dubhe.anvilcraft.init.ModEnchantmentTags;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnchantmentTagLoader {
    /**
     * 魔咒标签生成器初始化
     *
     * @param provider 提供器
     */
    public static void init(@NotNull RegistrateTagsProvider<Enchantment> provider) {
        provider.addTag(ModEnchantmentTags.MERCILESS_DAMAGE_PASSED)
            .add(Enchantments.SWEEPING_EDGE);

        provider.addTag(ModEnchantmentTags.MERCILESS_PASSED)
            .addTag(ModEnchantmentTags.MERCILESS_DAMAGE_PASSED)
            .add(Enchantments.UNBREAKING)
            .add(Enchantments.MENDING)
            .add(Enchantments.EFFICIENCY);
    }
}
