package dev.dubhe.anvilcraft.api.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.util.CodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 特殊工具属性
 */
public interface IToolProperties {
    /**
     * 多相
     *
     * @param phases 相
     */
    record Multiphase(ImmutableList<Phase> phases) {
        public static final Component[] phaseSuffixes = new Component[] {
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.alpha"),
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.beta"),
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.gamma"),
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.delta")
        };
        public static final Multiphase EMPTY = new Multiphase(Lists.newArrayList(new Iterator<>() {
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < phaseSuffixes.length;
            }

            @Override
            public Phase next() {
                return Phase.EMPTY.withCustomName(Component.literal("Empty").copy().append(phaseSuffixes[this.count++]));
            }
        }));

        public static final Codec<Multiphase> CODEC = Codec.list(Phase.CODEC).xmap(Multiphase::new, Multiphase::phases);
        public static final StreamCodec<RegistryFriendlyByteBuf, Multiphase> STREAM_CODEC =
            StreamCodec.of(Multiphase::encode, Multiphase::decode);

        public Multiphase(List<Phase> phases) {
            this(ImmutableList.copyOf(phases));
        }

        /**
         * 构建一个全新的多相<br>
         * 该方法目前仅用于多相工具
         *
         * @param customName 原始名称，不含后缀
         * @param enchantments 初始附魔，用于第一个相
         * @return 一个全新的多相
         */
        public static Multiphase make(Component customName, @Nullable ItemEnchantments enchantments) {
            MutableComponent customNameExtra = customName.copy();
            List<Phase> phases = Lists.newArrayList(
                Phase.make(customNameExtra.append(phaseSuffixes[0]), enchantments == null ? ItemEnchantments.EMPTY : enchantments)
            );
            for (int i = 1; i < phaseSuffixes.length; i++) {
                phases.add(Phase.EMPTY.withCustomName(customNameExtra.append(phaseSuffixes[i])));
            }
            return new Multiphase(phases);
        }

        /**
         * 使用输入的数据构建一个全新的多相
         *
         * @param original 原始物品
         * @param dataPairs 数据对，若长度大于{@code phaseSuffixes.length}，则超出的部分会被丢弃
         * @return 一个全新的多相
         */
        @SafeVarargs
        public static Multiphase make(ItemStack original, Pair<Component, @Nullable ItemEnchantments>... dataPairs) {
            List<Phase> phases = new ArrayList<>();
            for (int i = 0; i < phaseSuffixes.length; i++) {
                if (i < dataPairs.length) {
                    Pair<Component, ItemEnchantments> dataPair = dataPairs[i];
                    if (dataPair != null) {
                        MutableComponent customNameExtra = dataPair.getFirst().copy();
                        phases.add(Phase.make(
                            customNameExtra.append(phaseSuffixes[i]),
                            dataPair.getSecond() == null ? ItemEnchantments.EMPTY : dataPair.getSecond()
                        ));
                    } else {
                        phases.add(Phase.make(
                            original.getDisplayName().copy().append(phaseSuffixes[i]),
                            ItemEnchantments.EMPTY
                        ));
                    }
                } else {
                    phases.add(Phase.make(
                        original.getDisplayName().copy().append(phaseSuffixes[i]),
                        ItemEnchantments.EMPTY
                    ));
                }
            }
            return new Multiphase(phases);
        }

        public Component getCustomName() {
            return this.phases.getFirst().customName;
        }

        public ItemEnchantments getEnchantments() {
            return this.phases.getFirst().enchantments;
        }

        public Multiphase cyclePhases() {
            List<Phase> phases = new ArrayList<>(this.phases);
            Phase first = phases.removeFirst();
            phases.add(first);
            return new Multiphase(phases);
        }

        private static void encode(RegistryFriendlyByteBuf buf, Multiphase multiphase) {
            CodecUtil.writeCollectionWithRegistries(buf, multiphase.phases, Phase.STREAM_CODEC);
        }

        private static Multiphase decode(RegistryFriendlyByteBuf buf) {
            return new Multiphase(CodecUtil.readListWithRegistries(buf, Phase.STREAM_CODEC));
        }
    }

    record Phase(@NotNull Component customName, @NotNull ItemEnchantments enchantments) {
        public static final Phase EMPTY = new Phase(Component.empty(), ItemEnchantments.EMPTY);

        public static final Codec<Phase> CODEC = Codec.pair(ComponentSerialization.FLAT_CODEC, ItemEnchantments.CODEC)
            .xmap(
                pair -> new Phase(pair.getFirst(), pair.getSecond()),
                phase -> new Pair<>(phase.customName, phase.enchantments)
            );
        public static final StreamCodec<RegistryFriendlyByteBuf, Phase> STREAM_CODEC = StreamCodec.of(Phase::encode, Phase::decode);

        public static Phase make(Component customName, @Nullable ItemEnchantments enchantments) {
            return new Phase(customName, enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public Phase withCustomName(Component customName) {
            return new Phase(customName, this.enchantments);
        }

        public Phase withEnchantments(ItemEnchantments enchantments) {
            return make(this.customName, enchantments);
        }

        public Phase addEnchantments(ItemEnchantments enchantments) {
            ItemEnchantments original = this.enchantments;
            ItemEnchantments.Mutable originalMut = new ItemEnchantments.Mutable(original);
            for (Holder<Enchantment> enchantmentHolder : enchantments.keySet()) {
                if (original.keySet().contains(enchantmentHolder)) {
                    int originalLevel = original.getLevel(enchantmentHolder);
                    int newLevel = enchantments.getLevel(enchantmentHolder);
                    originalMut.set(enchantmentHolder, Math.max(originalLevel, newLevel));
                } else {
                    originalMut.set(enchantmentHolder, enchantments.getLevel(enchantmentHolder));
                }
            }
            return make(this.customName, originalMut.toImmutable());
        }

        private static void encode(RegistryFriendlyByteBuf buf, Phase phase) {
            ComponentSerialization.STREAM_CODEC.encode(buf, phase.customName);
            ItemEnchantments.STREAM_CODEC.encode(buf, phase.enchantments);
        }

        private static Phase decode(RegistryFriendlyByteBuf buf) {
            return new Phase(
                ComponentSerialization.STREAM_CODEC.decode(buf),
                ItemEnchantments.STREAM_CODEC.decode(buf)
            );
        }
    }
}
