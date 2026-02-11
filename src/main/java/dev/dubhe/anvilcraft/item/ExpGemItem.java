package dev.dubhe.anvilcraft.item;

import com.google.common.collect.Lists;
import dev.dubhe.anvilcraft.block.ExpFluidBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class ExpGemItem extends Item {
    public static final int VILLAGER_XP = 20;
    public static final int AGE_ADDITION = 2 * 60;

    public ExpGemItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
        Level level,
        Player player,
        InteractionHand usedHand
    ) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        int count = player.isShiftKeyDown() ? itemStack.getCount() : 1;
        player.giveExperiencePoints(ExpFluidBlock.XP_POINTS * count);
        itemStack.shrink(count);
        player.getCooldowns().addCooldown(this, 5);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    /**
     * 右键实体
     */
    public static InteractionResult useEntity(Player player, Entity target, ItemStack stack) {
        if (!(target instanceof Villager villager)) return InteractionResult.PASS;
        if (villager.level().isClientSide()) return InteractionResult.PASS;
        if (villager.getAge() >= 0) {
            // 只对有职业且不满级的村民生效
            VillagerData villagerData = villager.getVillagerData();
            int villagerLevel = villagerData.getLevel();
            if (villagerData.getProfession() == VillagerProfession.NONE) return InteractionResult.PASS;
            if (!VillagerData.canLevelUp(villagerLevel)) return InteractionResult.PASS;

            updateVillager(villager);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else {
            villager.ageUp(AGE_ADDITION, true);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
    }

    public static void updateVillager(Villager villager) {
        VillagerData villagerData = villager.getVillagerData();
        int villagerLevel = villagerData.getLevel();
        int villagerXp = villager.getVillagerXp() + VILLAGER_XP;
        villager.setVillagerXp(villagerXp);
        // 检测经验值是否足够，足够则升级（我好想直接用Villager::increaseMerchantCareer啊，可惜protect）
        if (villagerXp >= VillagerData.getMaxXpPerLevel(villagerLevel)) {
            villager.setVillagerXp(villagerXp);
            ++villagerLevel;
            villagerData = villagerData.setLevel(villagerLevel);
            villager.setVillagerData(villagerData);

            villager.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            villager.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 0.9f + 0.2f * villager.getRandom().nextFloat());

            // 获取对应职业的交易列表
            Int2ObjectMap<VillagerTrades.ItemListing[]> trades = VillagerTrades.TRADES.get(villagerData.getProfession());
            if (villager.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
                Int2ObjectMap<VillagerTrades.ItemListing[]> rebalance_trades =
                    VillagerTrades.EXPERIMENTAL_TRADES.get(villagerData.getProfession());
                if (rebalance_trades != null) {
                    trades = rebalance_trades;
                }
            }
            if (trades == null || trades.isEmpty()) return;
            VillagerTrades.ItemListing[] itemlisting = trades.get(villagerLevel);
            if (itemlisting == null) return;
            ArrayList<VillagerTrades.ItemListing> arraylist = Lists.newArrayList(itemlisting);

            // 更新村民当前的交易列表（MerchantOffers）
            MerchantOffers merchantoffers = villager.getOffers();
            for (int i = 0; i < itemlisting.length; i++) {
                if (arraylist.isEmpty() || i >= 2) break;
                MerchantOffer merchantoffer = arraylist.remove(villager.getRandom().nextInt(arraylist.size()))
                    .getOffer(villager, villager.getRandom());
                if (merchantoffer != null) {
                    merchantoffers.add(merchantoffer);
                }
            }
        }
    }
}
