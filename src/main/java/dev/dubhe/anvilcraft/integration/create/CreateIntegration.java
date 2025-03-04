package dev.dubhe.anvilcraft.integration.create;

import com.simibubi.create.AllItems;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.anvilcraft.lib.integration.Integrations;
import dev.dubhe.anvilcraft.block.GlowingMetalBlock;
import dev.dubhe.anvilcraft.block.HeaterBlock;
import dev.dubhe.anvilcraft.block.IncandescentMetalBlock;
import dev.dubhe.anvilcraft.block.RedhotMetalBlock;
import dev.dubhe.anvilcraft.init.ModBlocks;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.item.amulet.CogwheelAmuletItem;
import dev.dubhe.anvilcraft.recipe.JewelCraftingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.dubhe.anvilcraft.AnvilCraft.REGISTRATE;

@Integrations("create")
public class CreateIntegration {
    private static final BoilerHeater HEATER = CreateIntegration::heater;
    private static final BoilerHeater REDHOT_METAL = new ConstantValueHeater(1);
    private static final BoilerHeater GLOWING_METAL = new ConstantValueHeater(2);
    private static final BoilerHeater INCANDESCENT_METAL = new ConstantValueHeater(3);

    public void apply() {
        BoilerHeater.REGISTRY.registerProvider(new MyProvider());
    }

    private static float heater(Level level, BlockPos blockPos, @NotNull BlockState blockState) {
        if (blockState.is(ModBlocks.HEATER) && !blockState.getValue(HeaterBlock.OVERLOAD)) {
            return 1;
        }
        return -1;
    }

    private static class MyProvider implements SimpleRegistry.Provider<Block, BoilerHeater> {

        @Override
        public @Nullable BoilerHeater get(Block block) {
            if (block == ModBlocks.HEATER.get()) {
                return HEATER;
            }
            if (block instanceof IncandescentMetalBlock) {
                return INCANDESCENT_METAL;
            }
            if (block instanceof GlowingMetalBlock) {
                return GLOWING_METAL;
            }
            if (block instanceof RedhotMetalBlock) {
                return REDHOT_METAL;
            }
            return null;
        }
    }

    private record ConstantValueHeater(float level) implements BoilerHeater {
        @Override
        public float getHeat(Level level, BlockPos blockPos, BlockState blockState) {
            return this.level;
        }
    }

    public static final ItemEntry<CogwheelAmuletItem> COGWHEEL_AMULET = REGISTRATE
        .item("cogwheel_amulet", CogwheelAmuletItem::new)
        .properties(properties -> properties.stacksTo(1))
        .recipe((ctx, provider) -> JewelCraftingRecipe.builder()
            .requires(ModBlocks.SILVER_BLOCK, 4)
            .requires(AllItems.PRECISION_MECHANISM, 16)
            .requires(ModItems.ROYAL_STEEL_INGOT)
            .result(new ItemStack(ctx.get()))
            .save(provider))
        .register();
}
