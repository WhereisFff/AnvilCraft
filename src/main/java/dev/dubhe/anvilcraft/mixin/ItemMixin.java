package dev.dubhe.anvilcraft.mixin;

import dev.dubhe.anvilcraft.init.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
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
            put(Enchantments.SOUL_SPEED, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.FIRE_PROTECTION, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.FIRE_ASPECT, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.FLAME, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.BLAST_PROTECTION, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.SWIFT_SNEAK, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.PROTECTION, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.MENDING, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.INFINITY, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.DENSITY, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.BREACH, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.WIND_BURST, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.PROJECTILE_PROTECTION, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.FORTUNE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.LOOTING, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.LUCK_OF_THE_SEA, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.LURE, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.DEPTH_STRIDER, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.RESPIRATION, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.AQUA_AFFINITY, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.IMPALING, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
            put(Enchantments.RIPTIDE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE);
        }
    };

    @Unique
    private final List<Item> anvilcraft$otherTemplate = new ArrayList<>() {
        {
            add(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
            add(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE);
            add(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE);
            add(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE);
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
                int count = enchantments.size();
                if (enchantments.size() > 4) {
                    count = 4;
                }
                for (int i = 0; i < count; i++) {
                    Holder<Enchantment> randomEnchantment = anvilcraft$getRandom(enchantments);
                    boolean selected = false;
                    for (ResourceKey<Enchantment> enchantmentResourceKey : anvilcraft$enchantmentMappings.keySet()) {
                        if (randomEnchantment.is(enchantmentResourceKey)) {
                            result.add(anvilcraft$enchantmentMappings.get(enchantmentResourceKey));
                            selected = true;
                            break;
                        }
                    }
                    if (!selected) {
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
