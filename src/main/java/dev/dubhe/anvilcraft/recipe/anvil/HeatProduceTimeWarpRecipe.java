package dev.dubhe.anvilcraft.recipe.anvil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.heat.HeatTier;
import dev.dubhe.anvilcraft.init.ModRecipeTypes;
import dev.dubhe.anvilcraft.recipe.ChanceItemStack;
import dev.dubhe.anvilcraft.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeatProduceTimeWarpRecipe extends TimeWarpRecipe {
    private final List<HeatingInfo> infos;

    public HeatProduceTimeWarpRecipe(TimeWarpRecipe recipe, List<HeatingInfo> infos) {
        super(
            recipe.getIngredients(),
            Optional.of(recipe.getExactIngredients()),
            recipe.getCauldron(),
            recipe.getResults(),
            recipe.isProduceFluid(),
            recipe.isConsumeFluid(),
            recipe.getRequiredFluidLevel()
        );
        this.infos = infos;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public HeatProduceTimeWarpRecipe(
        List<Ingredient> ingredients,
        Optional<List<Ingredient>> exactIngredients,
        Block cauldron,
        List<ChanceItemStack> results,
        boolean produceFluid,
        boolean consumeFluid,
        int requiredFluidLevel,
        List<HeatingInfo> infos
    ) {
        super(NonNullList.copyOf(ingredients), exactIngredients, cauldron, results, produceFluid, consumeFluid, requiredFluidLevel);
        this.infos = infos;
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.HEAT_PRODUCING_TIME_WARP_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<HeatProduceTimeWarpRecipe> {
        private static final MapCodec<HeatProduceTimeWarpRecipe> CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            CodecUtil.createIngredientListCodec("ingredients", 64, "heat_produce_time_warp")
                .forGetter(HeatProduceTimeWarpRecipe::getIngredients),
            Ingredient.CODEC_NONEMPTY
                .listOf()
                .optionalFieldOf("exactIngredients")
                .forGetter(o -> o.getExactIngredients().isEmpty() ? Optional.empty() : Optional.of(o.getExactIngredients())),
            CodecUtil.BLOCK_CODEC.fieldOf("cauldron").forGetter(HeatProduceTimeWarpRecipe::getCauldron),
            ChanceItemStack.CODEC.listOf()
                .optionalFieldOf("results", List.of())
                .forGetter(HeatProduceTimeWarpRecipe::getResults),
            Codec.BOOL.fieldOf("produce_fluid").forGetter(HeatProduceTimeWarpRecipe::isProduceFluid),
            Codec.BOOL.fieldOf("consume_fluid").forGetter(HeatProduceTimeWarpRecipe::isConsumeFluid),
            Codec.INT.optionalFieldOf("requiredFluidLevel", 0).forGetter(HeatProduceTimeWarpRecipe::getRequiredFluidLevel),
            HeatingInfo.CODEC.listOf().fieldOf("heating").forGetter(HeatProduceTimeWarpRecipe::getInfos)
        ).apply(ins, HeatProduceTimeWarpRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, HeatProduceTimeWarpRecipe> STREAM_CODEC = StreamCodec.composite(
            TimeWarpRecipe.Serializer.STREAM_CODEC, recipe -> recipe,
            HeatingInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), HeatProduceTimeWarpRecipe::getInfos,
            HeatProduceTimeWarpRecipe::new
        );

        @Override
        public MapCodec<HeatProduceTimeWarpRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HeatProduceTimeWarpRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public static class Builder extends TimeWarpRecipe.Builder {
        private final List<HeatingInfo> infos = new ArrayList<>();

        public Builder heating(HeatTier tier, int duration) {
            this.infos.add(new HeatingInfo(tier, duration));
            return this;
        }

        @Override
        public HeatProduceTimeWarpRecipe buildRecipe() {
            if (consumeFluid) {
                requiredFluidLevel = Math.max(requiredFluidLevel, 1);
            }
            return new HeatProduceTimeWarpRecipe(
                ingredients,
                Optional.of(exactIngredients),
                cauldron,
                results,
                produceFluid,
                consumeFluid,
                requiredFluidLevel,
                infos
            );
        }

        @Override
        public void validate(ResourceLocation pId) {
            super.validate(pId);
            for (HeatingInfo info : this.infos) {
                if (info.tier == null) {
                    throw new IllegalArgumentException("Recipe produced heat tier must not be null, RecipeId: " + pId);
                }
                if (info.duration < -24000) {
                    throw new IllegalArgumentException("Recipe produced duration must not be lesser than -24000, RecipeId: " + pId);
                }
                if (info.duration > 24000) {
                    throw new IllegalArgumentException("Recipe produced duration must not be bigger than 24000, RecipeId: " + pId);
                }
            }
        }

        @Override
        public String getType() {
            return "heat_produce_time_warp";
        }
    }

    public record HeatingInfo(HeatTier tier, int duration) {
        public static final Codec<HeatingInfo> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            HeatTier.LOWER_NAME_CODEC.fieldOf("tier").forGetter(HeatingInfo::tier),
            Codec.INT.fieldOf("duration").forGetter(HeatingInfo::duration)
        ).apply(ins, HeatingInfo::new));
        public static final StreamCodec<ByteBuf, HeatingInfo> STREAM_CODEC = StreamCodec.composite(
            HeatTier.STREAM_CODEC, HeatingInfo::tier,
            ByteBufCodecs.VAR_INT, HeatingInfo::duration,
            HeatingInfo::new
        );
    }
}
