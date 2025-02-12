package dev.dubhe.anvilcraft.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Iterator;
import java.util.List;

/**
 * 特殊工具属性
 */
public interface ToolAttributes {
    record FireReforging() {
        public static final FireReforging INSTANCE = new FireReforging();
        public static final Codec<FireReforging> CODEC = Codec.unit(FireReforging::new);
        public static final StreamCodec<FriendlyByteBuf, FireReforging> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }

    record Tough() {
        public static final Tough INSTANCE = new Tough();
        public static final Codec<Tough> CODEC = Codec.unit(Tough::new);
        public static final StreamCodec<FriendlyByteBuf, Tough> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }

    record Morph(PatchedDataComponentMap spaceA, PatchedDataComponentMap spaceB) {
        public static final DataComponentMap PROTOTYPE = DataComponentMap.builder()
            .set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
            .build();
        public static final Morph DEFAULT = new Morph(new PatchedDataComponentMap(PROTOTYPE), new PatchedDataComponentMap(PROTOTYPE));

        public Morph switchSpaces() {
            return new Morph(spaceB, spaceA);
        }

        public static final Codec<Morph> CODEC = Codec.pair(PatchedDataComponentMap.CODEC, PatchedDataComponentMap.CODEC).xmap(
            spaces -> new Morph((PatchedDataComponentMap) spaces.getFirst(), (PatchedDataComponentMap) spaces.getSecond()),
            morph -> new Pair<>(morph.spaceA, morph.spaceB)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Morph> STREAM_CODEC = StreamCodec.of(Morph::encode, Morph::decode);

        private static void encode(RegistryFriendlyByteBuf buf, Morph value) {
            List<ItemEnchantments> valuesA = Lists.newArrayList(new Iterator<>() {
                final Iterator<TypedDataComponent<?>> parent = value.spaceA.iterator();

                @Override
                public boolean hasNext() {
                    return this.parent.hasNext();
                }

                @Override
                public ItemEnchantments next() {
                    if (this.parent.next().value() instanceof ItemEnchantments itemEnchantments) {
                        return itemEnchantments;
                    } else {
                        return null;
                    }
                }
            });
            List<ItemEnchantments> valuesB = Lists.newArrayList(new Iterator<>() {
                final Iterator<TypedDataComponent<?>> parent = value.spaceB.iterator();

                @Override
                public boolean hasNext() {
                    return this.parent.hasNext();
                }

                @Override
                public ItemEnchantments next() {
                    if (this.parent.next().value() instanceof ItemEnchantments itemEnchantments) {
                        return itemEnchantments;
                    } else {
                        return null;
                    }
                }
            });

            buf.writeCollection(valuesA, (buf1, value1) -> ItemEnchantments.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf1, value1));
            buf.writeCollection(valuesB, (buf1, value1) -> ItemEnchantments.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf1, value1));
        }
        private static Morph decode(FriendlyByteBuf buf) {
            PatchedDataComponentMap spaceA = new PatchedDataComponentMap(PROTOTYPE);
            PatchedDataComponentMap spaceB = new PatchedDataComponentMap(PROTOTYPE);

            buf.readList(buf1 -> ItemEnchantments.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf1))
                .forEach(itemEnchantments -> spaceA.set(DataComponents.ENCHANTMENTS, itemEnchantments));
            buf.readList(buf1 -> ItemEnchantments.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf1))
                .forEach(itemEnchantments -> spaceB.set(DataComponents.ENCHANTMENTS, itemEnchantments));

            return new Morph(spaceA, spaceB);
        }
    }
}
