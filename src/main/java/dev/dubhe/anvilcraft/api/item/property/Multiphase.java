package dev.dubhe.anvilcraft.api.item.property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.util.CodecUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * 多相
 *
 * @param current 当前生效的相名称
 * @param phases 所有相
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record Multiphase(int current, Phase[] phases) {
    public static final Multiphase EMPTY = make(Component.literal("Empty"));

    public static final Codec<Phase[]> PHASES_CODEC = CodecUtil.array(Phase.CODEC, () -> new Phase[0]);
    public static final Codec<Multiphase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.INT.fieldOf("current").forGetter(Multiphase::current),
        PHASES_CODEC.fieldOf("phases").forGetter(Multiphase::phases)
    ).apply(inst, Multiphase::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Multiphase> STREAM_CODEC =
        StreamCodec.of(Multiphase::encode, Multiphase::decode);

    /**
     * 构建一个全新的多相<br>
     * 该方法目前仅用于多相工具
     *
     * @param name 原始名称，不含后缀
     *
     * @return 一个全新的多相
     */
    public static Multiphase make(Component name) {
        Phase[] results = new Phase[PhasePattern.values().length];
        for (int i = 0; i < PhasePattern.values().length; i++) {
            PhasePattern pattern = PhasePattern.values()[i];
            Component newName = name.copy().append(pattern.nameSuffix);
            results[i] = pattern.phase.withItemName(newName);
        }
        return new Multiphase(0, results);
    }

    /**
     * 构建一个全新的多相<br>
     *
     * @param name   原始名称，不含后缀
     * @param enchantments 初始附魔，用于第一个相
     *
     * @return 一个全新的多相
     */
    public static Multiphase make(Component name, @Nullable ItemEnchantments enchantments) {
        Phase[] results = new Phase[PhasePattern.values().length];
        PhasePattern patternFirst = PhasePattern.values()[0];
        Component newNameFirst = name.copy().append(patternFirst.nameSuffix);
        results[0] = patternFirst.phase.withItemName(newNameFirst).withEnchantments(enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        for (int i = 1; i < PhasePattern.values().length; i++) {
            PhasePattern pattern = PhasePattern.values()[i];
            Component newName = name.copy().append(pattern.nameSuffix);
            results[i] = patternFirst.phase.withItemName(newName);
        }
        return new Multiphase(0, results);
    }

    /**
     * 使用输入的数据构建一个全新的多相，并传入物品
     *
     * @param original  原始物品
     * @param dataGroups 数据组，若数量大于{@link PhasePattern#values()}的数量，则超出的部分会被丢弃
     *
     * @return 一个全新的多相
     */
    @SafeVarargs
    public static Multiphase make(ItemStack original, Triple<Component, Integer, @Nullable ItemEnchantments>... dataGroups) {
        Phase[] phases = new Phase[PhasePattern.values().length];
        for (int i = 0; i < PhasePattern.values().length; i++) {
            PhasePattern pattern = PhasePattern.values()[i];
            if (i < dataGroups.length) {
                Triple<Component, Integer, ItemEnchantments> dataGroup = dataGroups[i];
                if (dataGroup != null) {
                    phases[i] = pattern.phase
                        .withCustomName(dataGroup.getLeft().copy().append(pattern.nameSuffix))
                        .withRepairCost(dataGroup.getMiddle())
                        .withEnchantments(dataGroup.getRight() == null ? ItemEnchantments.EMPTY : dataGroup.getRight());
                } else {
                    phases[i] = pattern.phase.withCustomName(original.getHoverName().copy().append(pattern.nameSuffix));
                }
            } else {
                phases[i] = pattern.phase.withCustomName(original.getHoverName().copy().append(pattern.nameSuffix));
            }
        }
        return new Multiphase(0, phases);
    }

    public int size() {
        return this.phases.length;
    }

    public void cyclePhases(ItemStack stack) {
        this.cyclePhases(stack, (this.current + 1) % this.size());
    }

    public void cyclePhases(ItemStack stack, int nextIndex) {
        Phase current = this.phases[this.current];
        Phase next = this.phases[nextIndex];

        Component customName;
        if (next.customName == null) {
            customName = stack.get(DataComponents.CUSTOM_NAME);
            stack.remove(DataComponents.CUSTOM_NAME);
        } else {
            customName = stack.set(DataComponents.CUSTOM_NAME, next.customName);
        }

        Component itemName;
        if (next.itemName == null) {
            itemName = stack.get(DataComponents.ITEM_NAME);
            stack.remove(DataComponents.ITEM_NAME);
        } else {
            itemName = stack.set(DataComponents.ITEM_NAME, next.itemName);
        }

        Integer repairCost = stack.set(DataComponents.REPAIR_COST, next.repairCost);
        if (repairCost == null || repairCost < 0) repairCost = 0;

        ItemEnchantments enchantments = stack.set(DataComponents.ENCHANTMENTS, next.enchantments);
        if (enchantments == null) enchantments = ItemEnchantments.EMPTY;

        this.phases[this.current] = current
            .withCustomName(customName)
            .withItemName(itemName)
            .withRepairCost(repairCost)
            .withEnchantments(enchantments);
        stack.set(ModComponents.MULTIPHASE, new Multiphase(nextIndex, this.phases));
    }

    private static void encode(RegistryFriendlyByteBuf buf, Multiphase multiphase) {
        buf.writeVarInt(multiphase.current);
        buf.writeArray(
            multiphase.phases,
            (buffer, value) -> Phase.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, value)
        );
    }

    private static Multiphase decode(RegistryFriendlyByteBuf buf) {
        return new Multiphase(
            buf.readVarInt(),
            buf.readArray(Phase[]::new, buffer -> Phase.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer))
        );
    }

    public Multiphase copy() {
        return new Multiphase(this.current, this.phases);
    }

    public record Phase(
        @Nullable Component customName, @Nullable Component itemName,
        int repairCost, @NotNull ItemEnchantments enchantments
    ) {
        public static final Phase DEFAULT = new Phase(
            Component.literal("Default"), null, 0, ItemEnchantments.EMPTY
        );

        public static final Codec<Phase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ComponentSerialization.FLAT_CODEC.lenientOptionalFieldOf("customName")
                .forGetter(phase -> Optional.ofNullable(phase.customName())),
            ComponentSerialization.FLAT_CODEC.lenientOptionalFieldOf("itemName")
                .forGetter(phase -> Optional.ofNullable(phase.itemName())),
            Codec.INT.fieldOf("repairCost").forGetter(Phase::repairCost),
            ItemEnchantments.CODEC.fieldOf("enchantments").forGetter(Phase::enchantments)
        ).apply(inst, (customName, itemName, repairCost, enchantments) ->
            new Phase(customName.orElse(null), itemName.orElse(null), repairCost, enchantments)));
        public static final StreamCodec<RegistryFriendlyByteBuf, Phase> STREAM_CODEC = StreamCodec.of(Phase::encode, Phase::decode);

        public Phase(@Nullable Component name, @Nullable ItemEnchantments enchantments) {
            this(name, 0, enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public Phase(@Nullable Component name, int repairCost, @Nullable ItemEnchantments enchantments) {
            this(name, name, repairCost, enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public static Phase make(Component name, @Nullable ItemEnchantments enchantments) {
            return DEFAULT
                .withCustomName(name)
                .withEnchantments(enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public Phase withCustomName(@Nullable Component customName) {
            return new Phase(customName, this.itemName, this.repairCost, this.enchantments);
        }

        public Phase withItemName(@Nullable Component itemName) {
            return new Phase(this.customName, itemName, this.repairCost, this.enchantments);
        }

        public Phase withRepairCost(int repairCost) {
            return new Phase(this.customName, this.itemName, repairCost, this.enchantments);
        }

        public Phase withEnchantments(ItemEnchantments enchantments) {
            return new Phase(this.customName, this.itemName, this.repairCost, enchantments);
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
            return new Phase(this.customName, this.itemName, this.repairCost, originalMut.toImmutable());
        }

        public void applyToStack(ItemStack stack) {
            stack.set(DataComponents.CUSTOM_NAME, this.customName());
            stack.set(DataComponents.ITEM_NAME, this.itemName());
            stack.set(DataComponents.REPAIR_COST, this.repairCost());
            stack.set(DataComponents.ENCHANTMENTS, this.enchantments());
        }

        private static void encode(RegistryFriendlyByteBuf buf, Phase phase) {
            ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, Optional.ofNullable(phase.customName));
            ComponentSerialization.OPTIONAL_STREAM_CODEC.encode(buf, Optional.ofNullable(phase.itemName));
            ByteBufCodecs.INT.encode(buf, phase.repairCost);
            ItemEnchantments.STREAM_CODEC.encode(buf, phase.enchantments);
        }

        private static Phase decode(RegistryFriendlyByteBuf buf) {
            return new Phase(
                ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf).orElse(null),
                ComponentSerialization.OPTIONAL_STREAM_CODEC.decode(buf).orElse(null),
                ByteBufCodecs.INT.decode(buf),
                ItemEnchantments.STREAM_CODEC.decode(buf)
            );
        }

        @Override
        public String toString() {
            return "Phase{customName: %s, itemName: %s, repairCost: %s, enchantments: %s}"
                .formatted(this.customName, this.itemName, this.repairCost, this.enchantments);
        }
    }

    public enum PhasePattern {
        ALPHA(
            new Phase(null, null, 0, ItemEnchantments.EMPTY),
            Component.translatable("tooltip.anvilcraft.property.multiphase.suffix.alpha")),
        BETA(
            new Phase(null, null, 0, ItemEnchantments.EMPTY),
            Component.translatable("tooltip.anvilcraft.property.multiphase.suffix.beta")),
        GAMMA(
            new Phase(null, null, 0, ItemEnchantments.EMPTY),
            Component.translatable("tooltip.anvilcraft.property.multiphase.suffix.gamma")),
        DELTA(
            new Phase(null, null, 0, ItemEnchantments.EMPTY),
            Component.translatable("tooltip.anvilcraft.property.multiphase.suffix.delta")),
        ;

        private final Phase phase;
        private final Component nameSuffix;

        PhasePattern(Phase phase, Component nameSuffix) {
            this.phase = phase;
            this.nameSuffix = nameSuffix;
        }
    }
}
