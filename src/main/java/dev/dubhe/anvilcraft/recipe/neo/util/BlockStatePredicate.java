package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.mixin.accessor.StateHolderAccessor;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlockStatePredicate implements Predicate<BlockState> {
    @Getter
    private final Block block;
    private final List<Map<Property<?>, Comparable<?>>> stateOr;
    public static final Codec<BlockStatePredicate> CODEC = codec(BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState);

    public BlockStatePredicate(Block block, List<Map<Property<?>, Comparable<?>>> stateOr) {
        this.block = block;
        this.stateOr = stateOr;
    }

    private static @NotNull BlockStatePredicate create(Block block, @NotNull List<BlockState> stateOr) {
        return new BlockStatePredicate(block, stateOr.stream().map(StateHolder::getValues).toList());
    }

    @Override
    public boolean test(BlockState blockState) {
        if (blockState == null) return false;
        if (!blockState.is(block)) return false;
        if (this.stateOr.isEmpty()) return true;
        for (Map<Property<?>, Comparable<?>> stateAnd : stateOr) {
            boolean flag = true;
            for (Property<?> key : stateAnd.keySet()) {
                if (!blockState.hasProperty(key)) {
                    flag = false;
                    break;
                }
                Comparable<?> value = stateAnd.get(key);
                Comparable<?> value1 = blockState.getValue(key);
                if (!value.equals(value1)) {
                    flag = false;
                    break;
                }
            }
            if (flag) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> List<BlockState> getStateOr(@NotNull BlockStatePredicate predicate) {
        return predicate.stateOr.stream().map(stateAnd -> {
            BlockState state = predicate.block.defaultBlockState();
            for (Property<?> key : stateAnd.keySet()) {
                Comparable<?> comparable = stateAnd.get(key);
                state = state.setValue((Property<T>) key, (T) comparable);
            }
            return state;
        }).toList();
    }

    protected static Codec<BlockStatePredicate> codec(@NotNull Codec<Block> propertyMap, @NotNull Function<Block, BlockState> holderFunction) {
        return propertyMap.dispatch("block", BlockStatePredicate::getBlock, (block) -> {
            BlockState state = holderFunction.apply(block);
            if (state.getValues().isEmpty()) return MapCodec.unit(new BlockStatePredicate(block, List.of()));
            @SuppressWarnings("unchecked")
            MapCodec<BlockState> propertiesCodec = ((StateHolderAccessor<Block, BlockState>) state).getPropertiesCodec();
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                propertiesCodec.codec()
                    .listOf()
                    .fieldOf("stateOr")
                    .forGetter(BlockStatePredicate::getStateOr)
            ).apply(instance, list -> BlockStatePredicate.create(block, list)));
        });
    }

    public static void encode(@NotNull FriendlyByteBuf buf, @NotNull BlockStatePredicate predicate) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(predicate.getBlock());
        buf.writeResourceLocation(key);
        buf.writeCollection(predicate.stateOr, (buf1, state) -> {
            buf1.writeMap(
                state,
                (buf2, property) -> buf2.writeUtf(property.getName()),
                (buf2, value) -> buf2.writeUtf(value.toString())
            );
        });
    }

    public static @NotNull BlockStatePredicate decode(@NotNull FriendlyByteBuf buf) {
        ResourceLocation key = buf.readResourceLocation();
        Block block = BuiltInRegistries.BLOCK.get(key);
        BlockState state = block.defaultBlockState();
        List<Map<String, String>> maps = buf.readList(buf1 -> buf1.readMap(
            FriendlyByteBuf::readUtf,
            buf2 -> buf2.readUtf()
        ));
        StateDefinition<Block, BlockState> definition = state.getBlock().getStateDefinition();
        List<Map<Property<?>, Comparable<?>>> list = maps.stream().map(map -> {
            Map<Property<?>, Comparable<?>> map1 = new HashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                Property<?> property = definition.getProperty(entry.getKey());
                if (property == null) throw new IllegalStateException();
                @SuppressWarnings("unchecked")
                Optional<Comparable<?>> value = (Optional<Comparable<?>>) property.getValue(entry.getValue());
                if (value.isEmpty()) throw new IllegalStateException();
                map1.put(property, value.get());
            }
            return Collections.unmodifiableMap(map1);
        }).toList();
        return new BlockStatePredicate(block, list);
    }
}
