package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.init.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {
    @Unique
    private final Map<ResourceKey<Enchantment>, Item> anvilcraft$enchantmentMappings = new Object2ObjectOpenHashMap<>() {
        {
            put(Enchantments.SOUL_SPEED, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);// 灵魂疾行 猪鼻盔甲纹饰
            put(Enchantments.FIRE_PROTECTION, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);// 火焰保护 肋骨盔甲纹饰
            put(Enchantments.FIRE_ASPECT, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);// 火焰附加 肋骨盔甲纹饰
            put(Enchantments.FLAME, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);// 火矢 肋骨盔甲纹饰
            put(Enchantments.BLAST_PROTECTION, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);// 爆炸保护 沙丘盔甲纹饰
            put(Enchantments.SWIFT_SNEAK, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE);// 迅捷潜行 幽静盔甲纹饰
            put(Enchantments.PROTECTION, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);// 保护 监守盔甲纹饰
            put(Enchantments.MENDING, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);// 经验修补 恼鬼盔甲纹饰
            put(Enchantments.INFINITY, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);// 无限 哨兵盔甲纹饰
            put(Enchantments.DENSITY, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE);// 致密 镶铆盔甲纹饰
            put(Enchantments.BREACH, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE);// 破甲 镶铆盔甲纹饰
            put(Enchantments.WIND_BURST, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE);// 风暴 涡流盔甲纹饰
            put(Enchantments.PROJECTILE_PROTECTION, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);// 弹射物保护 荒野盔甲纹饰
            put(Enchantments.FORTUNE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);// 时运 尖塔盔甲纹饰
            put(Enchantments.LOOTING, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);// 抢夺 眼眸盔甲纹饰
            put(Enchantments.LUCK_OF_THE_SEA, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);// 海之眷顾 海岸盔甲纹饰
            put(Enchantments.LURE, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);// 饵钓 海岸盔甲纹饰
            put(Enchantments.DEPTH_STRIDER, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);// 深海探索者 潮汐盔甲纹饰
            put(Enchantments.RESPIRATION, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);// 水下呼吸 潮汐盔甲纹饰
            put(Enchantments.AQUA_AFFINITY, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);// 水下速掘 潮汐盔甲纹饰
            put(Enchantments.IMPALING, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);// 穿刺 潮汐盔甲纹饰
            put(Enchantments.RIPTIDE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);// 激流 潮汐盔甲纹饰
        }
    };

    @Unique
    private final List<Item> anvilcraft$otherTemplate = new ArrayList<>() {
        {
            add(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);// 向导盔甲纹饰
            add(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE);// 牧民盔甲纹饰
            add(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE);// 雇主盔甲纹饰
            add(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);// 塑造盔甲纹饰
        }
    };

    @Inject(
        method = "onDestroyed",
        at = @At("HEAD")
    )
    public void onDestroyed(ItemEntity itemEntity, CallbackInfo ci) {
        Level level = itemEntity.level();
        List<Item> result = new ArrayList<>();
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.is(ModItems.EIGHT_TO_ONE_SMITHING_TEMPLATE)) {
            ItemEnchantments itemEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
            if (itemEnchantments != null && !itemEnchantments.isEmpty()) {
                List<Holder<Enchantment>> enchantments = new ArrayList<>(itemEnchantments.keySet().stream().toList());
                int count = 4;
                if (count > enchantments.size()) {
                    count = enchantments.size();
                }
                for (int i = 0; i < count; i++) {
                    Holder<Enchantment> randomEnchantment = anvilcraft$getRandom(enchantments);
                    boolean isSuccess = false;
                    for (ResourceKey<Enchantment> enchantmentResourceKey : anvilcraft$enchantmentMappings.keySet()) {
                        if (randomEnchantment.is(enchantmentResourceKey)) {
                            result.add(anvilcraft$enchantmentMappings.get(enchantmentResourceKey));
                            isSuccess = true;
                            break;
                        }
                    }
                    if (!isSuccess) {
                        result.add(anvilcraft$getRandom(anvilcraft$otherTemplate));
                    }
                }

                if (new HashSet<>(result).size() == 4) {
                    result.add(ModItems.TRANSCENDIUM_UPGRADE_SMITHING_TEMPLATE.get());
                }

                for (Item item : result) {
                    level.addFreshEntity(new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), new ItemStack(item)));
                }
            }
        }
    }

    @Unique
    private <E> E anvilcraft$getRandom(List<E> collection) {
        Collections.shuffle(collection);
        return collection.getFirst();
    }
}
