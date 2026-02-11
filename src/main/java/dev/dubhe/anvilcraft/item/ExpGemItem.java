package dev.dubhe.anvilcraft.item;

import com.google.common.collect.Lists;
import dev.dubhe.anvilcraft.block.ExpFluidBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;

public class ExpGemItem extends Item {
    public static final int VILLAGER_XP = 20;
    public static final int AGE_ADDITION = 2 * 60 * 20;

    public ExpGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemInHand = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        int count = player.isShiftKeyDown() ? itemInHand.getCount() : 1;
        player.giveExperiencePoints(ExpFluidBlock.XP_POINTS * count);
        itemInHand.shrink(count);
        return InteractionResult.SUCCESS;
    }

    /**
     * 右键实体
     */
    public static InteractionResult useEntity(Player player, Entity target, ItemStack stack) {
        if (!(target instanceof Villager villager)) return InteractionResult.PASS;
        System.out.println("villagerAge: " + villager.getAge());
        if (villager.getAge() >= 0) {
            // 只对有职业且不满级的村民生效
            int villagerXp = villager.getVillagerXp() + VILLAGER_XP;
            VillagerData villagerData = villager.getVillagerData();
            int villagerLevel = villagerData.getLevel();
            if (villagerData.getProfession() != VillagerProfession.NONE) return InteractionResult.PASS;
            if (!VillagerData.canLevelUp(villagerLevel)) return InteractionResult.PASS;

            villager.setVillagerXp(villagerXp);
            stack.shrink(1);
            updateVillager(villager, villagerXp, villagerLevel, villagerData);
            return InteractionResult.SUCCESS;
        } else {
            int villagerAge = villager.getAge() + AGE_ADDITION;
            if (villagerAge > 0) villagerAge = 0;
            villager.setAge(villagerAge);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }

    public static void updateVillager(
        Villager villager,
        int villagerXp,
        int villagerLevel,
        VillagerData villagerData
    ) {
        // 检测经验值是否足够，足够则升级（我好想直接用Villager::increaseMerchantCareer啊，可惜protect）
        if (villagerXp >= VillagerData.getMaxXpPerLevel(villagerLevel)) {
            villagerLevel++;
            villagerData.setLevel(villagerLevel);
            villager.setVillagerData(villagerData);
            villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            villager.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.9f + 0.2f * villager.getRandom().nextFloat());

            // 获取对应职业的交易列表
            Int2ObjectMap<VillagerTrades.ItemListing[]> trades = VillagerTrades.TRADES.get(villagerData.getProfession());
            if (villager.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap1 = VillagerTrades.EXPERIMENTAL_TRADES.get(villagerData.getProfession());
                if (int2objectmap1 != null) {
                    trades = VillagerTrades.TRADES.get(villagerData.getProfession());
                }
            }
            if (trades == null || trades.isEmpty()) return;
            VillagerTrades.ItemListing[] itemlisting = trades.get(villagerLevel);
            if (itemlisting == null) return;
            ArrayList<VillagerTrades.ItemListing> arraylist = Lists.newArrayList(itemlisting);

            // 获取村民当前的交易列表（MerchantOffers）
            MerchantOffers merchantoffers = villager.getOffers();
            for (int i = 0; i < itemlisting.length; i++) {
                if (arraylist.isEmpty()) break;
                MerchantOffer merchantoffer = arraylist.remove(villager.getRandom().nextInt(arraylist.size()))
                    .getOffer(villager, villager.getRandom());
                if (merchantoffer != null) {
                    merchantoffers.add(merchantoffer);
                }

            }
        }
    }
}
