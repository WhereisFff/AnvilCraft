package dev.dubhe.anvilcraft.api.item.property;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * 多相
 *
 * @param first  第一相
 * @param second 第二相
 * @param third  第三相
 * @param forth  第四相
 */
public record Multiphase(Phase first, Phase second, Phase third, Phase forth) {
    public static final Component[] phaseSuffixes = {
        Component.translatable("tooltip.anvilcraft.property.multiphase.alpha"),
        Component.translatable("tooltip.anvilcraft.property.multiphase.beta"),
        Component.translatable("tooltip.anvilcraft.property.multiphase.gamma"),
        Component.translatable("tooltip.anvilcraft.property.multiphase.delta")
    };
    public static final Multiphase EMPTY = new Multiphase(
        Phase.EMPTY.withCustomName(Component.literal("Empty").append(phaseSuffixes[0])),
        Phase.EMPTY.withCustomName(Component.literal("Empty").append(phaseSuffixes[1])),
        Phase.EMPTY.withCustomName(Component.literal("Empty").append(phaseSuffixes[2])),
        Phase.EMPTY.withCustomName(Component.literal("Empty").append(phaseSuffixes[3]))
    );

    public static final Codec<Multiphase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Phase.CODEC.fieldOf("first").forGetter(multiphase -> multiphase.first),
        Phase.CODEC.fieldOf("second").forGetter(multiphase -> multiphase.second),
        Phase.CODEC.fieldOf("third").forGetter(multiphase -> multiphase.third),
        Phase.CODEC.fieldOf("forth").forGetter(multiphase -> multiphase.forth)
    ).apply(inst, Multiphase::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Multiphase> STREAM_CODEC =
        StreamCodec.of(Multiphase::encode, Multiphase::decode);

    /**
     * 构建一个全新的多相<br>
     * 该方法目前仅用于多相工具
     *
     * @param customName 原始名称，不含后缀
     *
     * @return 一个全新的多相
     */
    public static Multiphase make(Component customName) {
        return new Multiphase(
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[0])),
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[1])),
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[2])),
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[3]))
        );
    }

    /**
     * 构建一个全新的多相<br>
     *
     * @param customName   原始名称，不含后缀
     * @param enchantments 初始附魔，用于第一个相
     *
     * @return 一个全新的多相
     */
    public static Multiphase make(Component customName, @Nullable ItemEnchantments enchantments) {
        return new Multiphase(
            Phase.make(
                customName.copy().append(phaseSuffixes[0]), enchantments == null ? ItemEnchantments.EMPTY : enchantments),
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[1])),
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[2])),
            Phase.EMPTY.withCustomName(customName.copy().append(phaseSuffixes[3]))
        );
    }

    /**
     * 使用输入的数据构建一个全新的多相
     *
     * @param original  原始物品
     * @param dataPairs 数据对，若长度大于{@code phaseSuffixes.length}，则超出的部分会被丢弃
     *
     * @return 一个全新的多相
     */
    @SafeVarargs
    public static Multiphase make(ItemStack original, Pair<Component, @Nullable ItemEnchantments>... dataPairs) {
        Phase[] phases = new Phase[phaseSuffixes.length];
        for (int i = 0; i < phaseSuffixes.length; i++) {
            if (i < dataPairs.length) {
                Pair<Component, ItemEnchantments> dataPair = dataPairs[i];
                if (dataPair != null) {
                    phases[i] = Phase.make(
                        dataPair.getFirst().copy().append(phaseSuffixes[i]),
                        dataPair.getSecond() == null ? ItemEnchantments.EMPTY : dataPair.getSecond()
                    );
                } else {
                    phases[i] = Phase.make(
                        original.getHoverName().copy().append(phaseSuffixes[i]),
                        ItemEnchantments.EMPTY
                    );
                }
            } else {
                phases[i] = Phase.make(
                    original.getHoverName().copy().append(phaseSuffixes[i]),
                    ItemEnchantments.EMPTY
                );
            }
        }
        return new Multiphase(phases[0], phases[1], phases[2], phases[3]);
    }

    public Component getCustomName() {
        return this.first.customName;
    }

    public ItemEnchantments getEnchantments() {
        return this.first.enchantments;
    }

    public Multiphase applyCustomName(@NotNull Component customName) {
        return new Multiphase(first.withCustomName(customName), second, third, forth);
    }

    public Multiphase applyEnchantments(@NotNull ItemEnchantments enchantments) {
        return new Multiphase(first.withEnchantments(enchantments), second, third, forth);
    }

    public Multiphase cyclePhases() {
        return new Multiphase(second, third, forth, first);
    }

    private static void encode(RegistryFriendlyByteBuf buf, Multiphase multiphase) {
        Phase.STREAM_CODEC.encode(buf, multiphase.first);
        Phase.STREAM_CODEC.encode(buf, multiphase.second);
        Phase.STREAM_CODEC.encode(buf, multiphase.third);
        Phase.STREAM_CODEC.encode(buf, multiphase.forth);
    }

    private static Multiphase decode(RegistryFriendlyByteBuf buf) {
        return new Multiphase(
            Phase.STREAM_CODEC.decode(buf),
            Phase.STREAM_CODEC.decode(buf),
            Phase.STREAM_CODEC.decode(buf),
            Phase.STREAM_CODEC.decode(buf)
        );
    }

    public boolean equalsLoose(Object o) {
        if (o == this) return true;
        if (o instanceof Multiphase(
            Phase first1, Phase second1, Phase third1, Phase forth1
        )) {
            List<Phase> thiS = Lists.newArrayList(this.first, this.second, this.third, this.forth);
            List<Phase> that = Lists.newArrayList(first1, second1, third1, forth1);
            return Util.isEqualCollection(thiS, that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third, forth);
    }

    public record Phase(@NotNull Component customName, int repairCost, @NotNull ItemEnchantments enchantments) {
        public static final Phase EMPTY = new Phase(Component.empty(), 0, ItemEnchantments.EMPTY);

        public static final Codec<Phase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ComponentSerialization.FLAT_CODEC.fieldOf("customName").forGetter(phase -> phase.customName),
            Codec.INT.fieldOf("repairCost").forGetter(phase -> phase.repairCost),
            ItemEnchantments.CODEC.fieldOf("enchantments").forGetter(phase -> phase.enchantments)
        ).apply(inst, Phase::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Phase> STREAM_CODEC = StreamCodec.of(Phase::encode, Phase::decode);

        public static Phase make(Component customName, @Nullable ItemEnchantments enchantments) {
            return new Phase(customName, 0, enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public Phase withCustomName(Component customName) {
            return new Phase(customName, this.repairCost, this.enchantments);
        }

        public Phase withEnchantments(ItemEnchantments enchantments) {
            return new Phase(this.customName, this.repairCost, enchantments);
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
            return new Phase(this.customName, this.repairCost, originalMut.toImmutable());
        }

        private static void encode(RegistryFriendlyByteBuf buf, Phase phase) {
            ComponentSerialization.STREAM_CODEC.encode(buf, phase.customName);
            ByteBufCodecs.INT.encode(buf, phase.repairCost);
            ItemEnchantments.STREAM_CODEC.encode(buf, phase.enchantments);
        }

        private static Phase decode(RegistryFriendlyByteBuf buf) {
            return new Phase(
                ComponentSerialization.STREAM_CODEC.decode(buf),
                ByteBufCodecs.INT.decode(buf),
                ItemEnchantments.STREAM_CODEC.decode(buf)
            );
        }
    }
}
