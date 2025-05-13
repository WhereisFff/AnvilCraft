package dev.dubhe.anvilcraft.item;

import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.util.AabbUtil;
import dev.dubhe.anvilcraft.util.BreakBlockUtil;
import dev.dubhe.anvilcraft.util.InventoryUtil;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Objects;

import static dev.dubhe.anvilcraft.api.entity.player.AnvilCraftBlockPlacer.anvilCraftBlockPlacer;

@Slf4j
public class DragonRodItem extends Item {
    public static final int DEFAULT_RANGE = 3;

    public DragonRodItem(Properties properties) {
        super(properties.component(ModComponents.DEVOUR_RANGE, DEFAULT_RANGE));
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        InteractionResultHolder<ItemStack> superHolder = super.use(level, player, usedHand);
        ItemStack dragonRod = superHolder.getObject();
        if (!dragonRod.is(this)) return superHolder;
        switch (dragonRod.get(ModComponents.DEVOUR_RANGE)) {
            case 3 -> dragonRod.set(ModComponents.DEVOUR_RANGE, 5);
            case 5 -> dragonRod.set(ModComponents.DEVOUR_RANGE, 7);
            case 7 -> dragonRod.set(ModComponents.DEVOUR_RANGE, 9);
            case 9 -> dragonRod.set(ModComponents.DEVOUR_RANGE, 3);
            case null, default -> {
                log.warn("A dragon rod in player {} dose not have devour range, use default", player);
                dragonRod.set(ModComponents.DEVOUR_RANGE, 3);
            }
        }
        return new InteractionResultHolder<>(superHolder.getResult(), dragonRod);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void devourBlock(
        ServerLevel level, Player player, ItemStack dragonRod,
        BlockPos centerPos, BlockState centerState, Direction clickedSide
    ) {
        if (centerState.getDestroySpeed(level, centerPos) != 0.0F) return;
        int range = dragonRod.getOrDefault(ModComponents.DEVOUR_RANGE, -1);
        if (range == -1) return;
        AABB devouringBox;
        switch (clickedSide) {
            case DOWN, UP -> devouringBox = AabbUtil.create(
                centerPos.relative(Direction.NORTH, range).relative(Direction.WEST, range),
                centerPos.relative(Direction.SOUTH, range).relative(Direction.EAST, range)
            );
            case NORTH, SOUTH -> devouringBox = AabbUtil.create(
                centerPos.relative(Direction.UP, range).relative(Direction.WEST, range),
                centerPos.relative(Direction.DOWN, range).relative(Direction.EAST, range)
            );
            case WEST, EAST -> devouringBox = AabbUtil.create(
                centerPos.relative(Direction.UP, range).relative(Direction.NORTH, range),
                centerPos.relative(Direction.DOWN, range).relative(Direction.SOUTH, range)
            );
            default -> devouringBox = new AABB(centerPos);
        }
        final List<BlockPos> devouringPoses = BlockPos.betweenClosedStream(devouringBox)
            .map(BlockPos::immutable)
            .toList();

        for (BlockPos devouringPos : devouringPoses) {
            BlockState devouringState = level.getBlockState(devouringPos);
            if (devouringState.isAir()) return;
            if (devouringState.getBlock().defaultDestroyTime() < 0) return;
            if (devouringState.is(ModBlockTags.BLOCK_DEVOURER_PROBABILITY_DROPPING)
                && level.random.nextDouble() > 0.05) {
                level.destroyBlock(devouringPos, false);
                continue;
            }

            // TODO: 同步一点六的多方块兼容
            //devouringPos = getMainPartPosToRemove(level, devouringPos);
            devouringState = level.getBlockState(devouringPos);

            List<ItemStack> dropList = BreakBlockUtil.drop(level, devouringPos);
            Inventory inventory = player.getInventory();
            for (ItemStack drop : dropList) {
                if (drop.isEmpty()) continue;
                ItemStack remaining = InventoryUtil.insertItem(inventory, drop);
                if (!remaining.isEmpty()) {
                    Block.popResource(level, devouringPos, remaining);
                }
            }

            //特判雕纹书架一类
            IItemHandler source = level.getCapability(Capabilities.ItemHandler.BLOCK, devouringPos, null);
            if (source != null && dropList.isEmpty()) {
                for (IntListIterator it = IntIterators.fromTo(0, source.getSlots()); it.hasNext(); ) {
                    int slot = it.nextInt();
                    ItemStack stack = source.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    stack = InventoryUtil.insertItem(inventory, stack);
                    if (!stack.isEmpty()) {
                        Block.popResource(level, devouringPos, stack);
                    }
                }
            }
            //特判讲台
            BlockEntity devouringBlockEntity = level.getBlockEntity(devouringPos);
            if (devouringBlockEntity instanceof LecternBlockEntity lectern) {
                ItemStack bookStack = lectern.getBook();
                bookStack = InventoryUtil.insertItem(inventory, bookStack);
                lectern.setBook(bookStack);
                if (!bookStack.isEmpty()) {
                    Block.popResource(level, devouringPos, bookStack);
                    lectern.setBook(ItemStack.EMPTY);
                }
            }
            if (!(devouringState.getBlock() instanceof DoublePlantBlock)) {
                devouringState.getBlock().playerWillDestroy(level, devouringPos, devouringState, anvilCraftBlockPlacer.getPlayer());
            }
            level.destroyBlock(devouringPos, false);
        }
        player.getCooldowns().addCooldown(dragonRod.getItem(), calculateDragonRodCooldown(player));
    }

    public static int calculateDragonRodCooldown(Player player) {
        int cooldown = 20;
        if (player.hasEffect(MobEffects.DIG_SPEED)) {
            cooldown -= Objects.requireNonNull(player.getEffect(MobEffects.DIG_SPEED)).getAmplifier() * 4;
        }
        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            cooldown += Objects.requireNonNull(player.getEffect(MobEffects.DIG_SLOWDOWN)).getAmplifier() * 60;
        }
        return Math.max(cooldown, 4);
    }
}
