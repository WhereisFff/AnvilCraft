package dev.dubhe.anvilcraft.api.item;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
    record Multiphase(List<Phase> phases) {
        public static final Component[] phaseSuffixes = new Component[] {
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.alpha"),
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.beta"),
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.gamma"),
            Component.translatable("tooltip.anvilcraft.attribute.multiphase.delta")
        };
        public static final Multiphase EMPTY = new Multiphase(Lists.newArrayList(new Iterator<>() {
            private int count = phaseSuffixes.length;

            @Override
            public boolean hasNext() {
                return count > 0;
            }

            @Override
            public Phase next() {
                this.count--;
                return Phase.EMPTY;
            }
        }));

        public static final Codec<Multiphase> CODEC = Codec.list(Phase.CODEC).xmap(Multiphase::new, Multiphase::phases);
        public static final StreamCodec<RegistryFriendlyByteBuf, Multiphase> STREAM_CODEC =
            StreamCodec.of(Multiphase::encode, Multiphase::decode);

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
            List<Phase> phases = Lists.newArrayList(Phase.make(customNameExtra.append(phaseSuffixes[0]), enchantments));
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
        public static Multiphase make(Item original, Pair<Component, @Nullable ItemEnchantments>... dataPairs) {
            List<Phase> phases = new ArrayList<>();
            for (int i = 0; i < phaseSuffixes.length; i++) {
                Pair<Component, ItemEnchantments> dataPair = dataPairs[i];
                if (dataPair != null) {
                    MutableComponent customNameExtra = dataPair.getFirst().copy();
                    phases.add(Phase.make(
                        customNameExtra.append(phaseSuffixes[i]),
                        dataPair.getSecond() == null ? ItemEnchantments.EMPTY : dataPair.getSecond()
                    ));
                } else {
                    phases.add(Phase.make(
                        original.getDescription().copy().append(phaseSuffixes[i]),
                        ItemEnchantments.EMPTY
                    ));
                }
            }
            return new Multiphase(phases);
        }

        public Multiphase switchSpaces() {
            List<Phase> allExceptFirst = this.phases.subList(1, this.phases.size() - 1);
            allExceptFirst.add(this.phases.getFirst());
            return new Multiphase(allExceptFirst);
        }

        private static void encode(RegistryFriendlyByteBuf buf, Multiphase value) {
            buf.writeCollection(value.phases, Phase.STREAM_CODEC);
        }

        private static Multiphase decode(RegistryFriendlyByteBuf buf) {
            return new Multiphase(buf.readCollection(ArrayList::new, Phase.STREAM_CODEC));
        }
    }

    record Phase(Component customName, PatchedDataComponentMap enchantments) {
        public static final DataComponentMap PROTOTYPE = DataComponentMap.builder()
            .set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
            .build();
        public static final Phase EMPTY = new Phase(Component.empty(), new PatchedDataComponentMap(PROTOTYPE));

        public static final Codec<Phase> CODEC = CompoundTag.CODEC.xmap(Phase::decode, Phase::encode);
        public static final StreamCodec<ByteBuf, Phase> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
            Phase::decode, Phase::encode
        );

        public static Phase make(Component customName, @Nullable ItemEnchantments enchantments) {
            PatchedDataComponentMap prototype = new PatchedDataComponentMap(PROTOTYPE);
            prototype.set(DataComponents.ENCHANTMENTS, enchantments == null ? ItemEnchantments.EMPTY : enchantments);
            return new Phase(customName, prototype);
        }

        public Phase withCustomName(Component customName) {
            return new Phase(customName, this.enchantments);
        }

        public Phase withEnchantments(ItemEnchantments enchantments) {
            return make(this.customName, enchantments);
        }

        public Phase addEnchantments(ItemEnchantments enchantments) {
            ItemEnchantments original = this.enchantments.get(DataComponents.ENCHANTMENTS);
            assert original != null;
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

        public static CompoundTag encode(Phase space) {
            CompoundTag root = new CompoundTag();

            root.putString("customName", space.customName.toString());

            CompoundTag enchantments = new CompoundTag();
            DataComponentPatch.CODEC.encode(space.enchantments.asPatch(), NbtOps.INSTANCE, enchantments);
            root.put("Enchantments", enchantments);

            return root;
        }

        public static Phase decode(CompoundTag root) {
            Component customName = ComponentSerialization.CODEC.decode(NbtOps.INSTANCE, root.get("customName"))
                .getOrThrow().getFirst();

            DataComponentPatch enchantmentsPatch = DataComponentPatch.CODEC.decode(NbtOps.INSTANCE, root.getCompound("Enchantments"))
                .getOrThrow().getFirst();
            PatchedDataComponentMap enchantments = PatchedDataComponentMap.fromPatch(PROTOTYPE, enchantmentsPatch);

            return new Phase(customName, enchantments);
        }
    }
}
