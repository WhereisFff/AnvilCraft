package dev.dubhe.anvilcraft.recipe.neo.outcome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.api.heat.HeatRecorder;
import dev.dubhe.anvilcraft.api.heat.HeatTier;
import dev.dubhe.anvilcraft.block.entity.heatable.HeatableBlockEntity;
import dev.dubhe.anvilcraft.init.ModBlockTags;
import dev.dubhe.anvilcraft.init.ModRecipeOutcomeTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.Distance;
import dev.dubhe.anvilcraft.util.Util;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter(AccessLevel.PRIVATE)
public class ProduceHeat implements IRecipeOutcome<ProduceHeat> {
    private final List<HeatData> heatData;
    private final Distance distance;

    private ProduceHeat(List<HeatData> heatData, Distance distance) {
        this.heatData = heatData;
        this.distance = distance;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Type getType() {
        return ModRecipeOutcomeTypes.PRODUCE_HEAT.get();
    }

    @Override
    public void accept(InWorldRecipeContext context) {
        ServerLevel level = context.getLevel();
        Vec3 center = context.getPos();
        for (BlockPos pos : this.distance.getAllPosesInRange(center)) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlockTags.HEATABLE_BLOCKS)) continue;

            HeatableBlockEntity heatable = Util.castSafely(level.getBlockEntity(pos), HeatableBlockEntity.class).orElse(null);
            Optional<ResourceLocation> idOp = HeatRecorder.getId(level, pos, state);
            if (idOp.isEmpty()) continue;
            HeatTier currentTier = HeatRecorder.getTier(level, pos, state)
                .orElseThrow(() -> new IllegalStateException("Unexpected non tier heatable block!"));
            for (var info : this.heatData) {
                HeatTier tier = info.tier();
                int durationDelta = info.duration;
                if (tier.compareTo(currentTier) > 0) {
                    Block deltaBlock = HeatRecorder.getHeatableBlock(idOp.get(), tier).orElse(null);
                    if (deltaBlock == null) continue;
                    level.setBlockAndUpdate(pos, deltaBlock.defaultBlockState());
                    if (!(deltaBlock instanceof EntityBlock)) continue;
                    BlockEntity deltaBlockEntity = level.getBlockEntity(pos);
                    if (!(deltaBlockEntity instanceof HeatableBlockEntity heatableEntity)) continue;
                    heatable = heatableEntity;
                } else if (tier.compareTo(currentTier) < 0) {
                    durationDelta = 0;
                }
                if (heatable == null) continue;

                if (durationDelta > 0) {
                    heatable.addDurationInTick(durationDelta);
                }
                heatable = null;
            }
        }
    }

    public static class Type implements IRecipeOutcome.Type<ProduceHeat> {
        public static final MapCodec<ProduceHeat> MAP_CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            HeatData.CODEC.listOf().fieldOf("heatData").forGetter(ProduceHeat::getHeatData),
            Distance.CODEC.fieldOf("distance").forGetter(ProduceHeat::getDistance)
        ).apply(ins, ProduceHeat::new));

        @Override
        public @NotNull MapCodec<ProduceHeat> codec() {
            return MAP_CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ProduceHeat> streamCodec() {
            return StreamCodec.of(Type::encode, Type::decode);
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ProduceHeat produceHeat) {
            var heatData = produceHeat.heatData;
            buf.writeVarInt(heatData.size());
            for (var data : heatData) {
                HeatData.STREAM_CODEC.encode(buf, data);
            }
            Distance.STREAM_CODEC.encode(buf, produceHeat.distance);
        }

        public static @NotNull ProduceHeat decode(@NotNull RegistryFriendlyByteBuf buf) {
            int i = buf.readVarInt();
            List<HeatData> heatData = new ArrayList<>();
            for (; i > 0; i--) {
                heatData.add(new HeatData(HeatTier.STREAM_CODEC.decode(buf), buf.readVarInt()));
            }
            return new ProduceHeat(List.copyOf(heatData), Distance.STREAM_CODEC.decode(buf));
        }
    }

    public record HeatData(HeatTier tier, int duration) {
        public static final Codec<HeatData> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            HeatTier.LOWER_NAME_CODEC.fieldOf("tier").forGetter(HeatData::tier),
            Codec.INT.fieldOf("duration").forGetter(HeatData::duration)
        ).apply(ins, HeatData::new));
        public static final StreamCodec<ByteBuf, HeatData> STREAM_CODEC = StreamCodec.composite(
            HeatTier.STREAM_CODEC, HeatData::tier,
            ByteBufCodecs.VAR_INT, HeatData::duration,
            HeatData::new
        );
    }

    public static class Builder {
        private final List<HeatData> canReach = new ArrayList<>();
        private Distance distance = Distance.DEFAULT;

        public Builder heat(HeatTier tier, int duration) {
            this.canReach.add(new HeatData(tier, duration));
            return this;
        }

        public Builder distance(Distance distance) {
            this.distance = distance;
            return this;
        }

        public Builder distance(Distance.Type type, int distance, boolean isHorizontal) {
            return this.distance(new Distance(type, distance, isHorizontal));
        }

        public Builder distanceEuclidean(int distance, boolean isHorizontal) {
            return this.distance(Distance.Type.EUCLIDEAN, distance, isHorizontal);
        }

        public Builder distanceEuclidean(boolean isHorizontal) {
            return this.distance(Distance.Type.EUCLIDEAN, 1, isHorizontal);
        }

        public Builder distanceEuclidean(int distance) {
            return this.distance(Distance.Type.EUCLIDEAN, distance, true);
        }

        public Builder distanceEuclidean() {
            return this.distance(Distance.Type.EUCLIDEAN, 1, true);
        }

        public Builder distanceManhattan(int distance, boolean isHorizontal) {
            return this.distance(Distance.Type.MANHATTAN, distance, isHorizontal);
        }

        public Builder distanceManhattan(boolean isHorizontal) {
            return this.distance(Distance.Type.MANHATTAN, 1, isHorizontal);
        }

        public Builder distanceManhattan(int distance) {
            return this.distance(Distance.Type.MANHATTAN, distance, true);
        }

        public Builder distanceManhattan() {
            return this.distance(Distance.Type.MANHATTAN, 1, true);
        }

        public Builder distanceChebyshev(int distance, boolean isHorizontal) {
            return this.distance(Distance.Type.CHEBYSHEV, distance, isHorizontal);
        }

        public Builder distanceChebyshev(boolean isHorizontal) {
            return this.distance(Distance.Type.CHEBYSHEV, 1, isHorizontal);
        }

        public Builder distanceChebyshev(int distance) {
            return this.distance(Distance.Type.CHEBYSHEV, distance, true);
        }

        public Builder distanceChebyshev() {
            return this.distance(Distance.Type.CHEBYSHEV, 1, true);
        }

        public ProduceHeat build() {
            return new ProduceHeat(this.canReach, this.distance);
        }
    }
}
