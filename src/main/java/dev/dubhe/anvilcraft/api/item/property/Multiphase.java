package dev.dubhe.anvilcraft.api.item.property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModComponents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

/**
 * 多相
 *
 * @param alpha α相
 * @param beta  β相
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record Multiphase(Phase alpha, Phase beta) {
    public static final Multiphase EMPTY = make(Component.literal("Empty"));

    public static final Component ALPHA_NAME_SUFFIX = makeSuffix("alpha");
    public static final Component BETA_NAME_SUFFIX = makeSuffix("beta");

    private static Component makeSuffix(String name) {
        return Component.translatable("tooltip.anvilcraft.property.multiphase.suffix." + name);
    }

    public static final Codec<Multiphase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Phase.CODEC.fieldOf("alpha").forGetter(Multiphase::alpha),
        Phase.CODEC.fieldOf("beta").forGetter(Multiphase::beta)
    ).apply(inst, Multiphase::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Multiphase> STREAM_CODEC = StreamCodec.composite(
        Phase.STREAM_CODEC, Multiphase::alpha,
        Phase.STREAM_CODEC, Multiphase::beta,
        Multiphase::new
    );

    /**
     * 构建一个全新的多相<br>
     * 该方法用于工具初始化
     *
     * @param name 原始名称，不含后缀
     * @return 一个全新的多相
     */
    @SuppressWarnings("DataFlowIssue")
    public static Multiphase make(Component name) {
        Component newNameAlpha = name.copy().append(ALPHA_NAME_SUFFIX);
        Phase alpha = Phase.EMPTY.withItemName(newNameAlpha);
        Component newNameBeta = name.copy().append(BETA_NAME_SUFFIX);
        Phase beta = Phase.EMPTY.withItemName(newNameBeta);
        return new Multiphase(alpha, beta);
    }

    /**
     * 构建一个全新的多相<br>
     *
     * @param name         原始名称，不含后缀
     * @param enchantments 初始附魔，用于α相
     * @return 一个全新的多相
     */
    public static Multiphase make(Component name, @Nullable ItemEnchantments enchantments) {
        Component newNameAlpha = name.copy().append(ALPHA_NAME_SUFFIX);
        Phase alpha = Phase.EMPTY.withItemName(newNameAlpha).withEnchantments(enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        Component newNameBeta = name.copy().append(BETA_NAME_SUFFIX);
        Phase beta = Phase.EMPTY.withItemName(newNameBeta);
        return new Multiphase(alpha, beta);
    }

    /**
     * 使用输入的数据构建一个全新的多相，并传入物品
     *
     * @param original 原始物品
     * @param dataS    数据组，只取前两个非null数据作α相和β相
     * @return 一个全新的多相
     */
    public static Multiphase make(Item original, PhaseData... dataS) {
        Phase[] phases = new Phase[2];
        for (int i = 0; i < 2; i++) {
            PhaseData data = dataS[i];
            if (data != null) {
                phases[i] = Phase.EMPTY
                    .withRepairCost(data.repairCost())
                    .withEnchantments(data.enchantments());
                if (data.customName() != null && !data.customName().equals(Component.empty())) {
                    phases[i] = phases[i]
                        .withCustomName(data.customName().copy());
                }
                if (data.itemName() != null && !data.itemName().equals(Component.empty())) {
                    phases[i] = phases[i]
                        .withItemName(data.itemName().copy());
                } else {
                    phases[i] = phases[i]
                        .withItemName(original.getDescription().copy().append(i == 0 ? ALPHA_NAME_SUFFIX : BETA_NAME_SUFFIX));
                }
            } else {
                phases[i] = Phase.EMPTY
                    .withCustomName(original.getDescription().copy().append(i == 0 ? ALPHA_NAME_SUFFIX : BETA_NAME_SUFFIX));
            }
        }
        return new Multiphase(phases[0], phases[1]);
    }

    public void applyToStack(ItemStack stack) {
        this.alpha.applyToStack(stack);
    }

    public void cyclePhases(ItemStack stack) {
        final Phase[] storing = {this.alpha};

        Optional.ofNullable(
            this.beta.customName.map(presentValue -> stack.set(DataComponents.CUSTOM_NAME, presentValue))
                .orElseGet(() -> {
                    Component c = stack.get(DataComponents.CUSTOM_NAME);
                    stack.remove(DataComponents.CUSTOM_NAME);
                    return c;
                })
        ).ifPresentOrElse(
            name -> storing[0] = storing[0].withCustomName(name),
            () -> storing[0] = storing[0].clearCustomName());
        Optional.ofNullable(
            this.beta.itemName.map(presentValue -> stack.set(DataComponents.ITEM_NAME, presentValue))
                .orElseGet(() -> {
                    Component c = stack.get(DataComponents.ITEM_NAME);
                    stack.remove(DataComponents.ITEM_NAME);
                    return c;
                })
        ).ifPresentOrElse(
            name -> storing[0] = storing[0].withItemName(name),
            () -> storing[0] = storing[0].clearItemName());

        Integer repairCost = stack.set(DataComponents.REPAIR_COST, this.beta.repairCost);
        if (repairCost == null || repairCost < 0) repairCost = 0;
        storing[0] = storing[0].withRepairCost(repairCost);

        ItemEnchantments enchantments = stack.set(EnchantmentHelper.getComponentType(stack), this.beta.enchantments);
        if (enchantments == null) enchantments = ItemEnchantments.EMPTY;
        storing[0] = storing[0].withEnchantments(enchantments);

        stack.set(ModComponents.MULTIPHASE, new Multiphase(this.beta, storing[0]));
    }

    public Multiphase copy() {
        return new Multiphase(this.alpha.copy(), this.beta.copy());
    }

    public Multiphase withAlpha(Phase alpha) {
        return new Multiphase(alpha, this.beta);
    }

    public Multiphase withBeta(Phase beta) {
        return new Multiphase(this.alpha, beta);
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    public record Phase(
        Optional<Component> customName, Optional<Component> itemName,
        int repairCost, @NotNull ItemEnchantments enchantments
    ) {
        public static final Phase EMPTY = new Phase(
            Optional.empty(), Optional.empty(), 0, ItemEnchantments.EMPTY
        );

        public static final Codec<Phase> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ComponentSerialization.FLAT_CODEC.optionalFieldOf("customName")
                .forGetter(Phase::customName),
            ComponentSerialization.FLAT_CODEC.optionalFieldOf("itemName")
                .forGetter(Phase::itemName),
            Codec.INT.fieldOf("repairCost").forGetter(Phase::repairCost),
            ItemEnchantments.CODEC.fieldOf("enchantments").forGetter(Phase::enchantments)
        ).apply(inst, Phase::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Phase> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.OPTIONAL_STREAM_CODEC, Phase::customName,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, Phase::itemName,
            ByteBufCodecs.INT, Phase::repairCost,
            ItemEnchantments.STREAM_CODEC, Phase::enchantments,
            Phase::new
        );

        public Phase(@Nullable Component name, @Nullable ItemEnchantments enchantments) {
            this(name, 0, enchantments);
        }

        public Phase(@Nullable Component name, int repairCost, @Nullable ItemEnchantments enchantments) {
            this(name, name, repairCost, enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public Phase(@Nullable Component customName, @Nullable Component itemName, int repairCost, @NotNull ItemEnchantments enchantments) {
            this(
                Objects.equals(customName, Component.empty()) ? Optional.empty() : Optional.ofNullable(customName),
                Objects.equals(itemName, Component.empty()) ? Optional.empty() : Optional.ofNullable(itemName),
                repairCost, enchantments
            );
        }

        public @Nullable Component getCustomName() {
            return this.customName.map(Component::copy).orElse(null);
        }

        public @Nullable Component getItemName() {
            return this.itemName.map(Component::copy).orElse(null);
        }

        public static Phase make(Component name, @Nullable ItemEnchantments enchantments) {
            return EMPTY
                .withCustomName(name)
                .withEnchantments(enchantments == null ? ItemEnchantments.EMPTY : enchantments);
        }

        public Phase withCustomName(Component customName) {
            return new Phase(Optional.of(customName), this.itemName, this.repairCost, this.enchantments);
        }

        public Phase withItemName(Component itemName) {
            return new Phase(this.customName, Optional.of(itemName), this.repairCost, this.enchantments);
        }

        public Phase clearCustomName() {
            return new Phase(Optional.empty(), this.itemName, this.repairCost, this.enchantments);
        }

        public Phase clearItemName() {
            return new Phase(this.customName, Optional.empty(), this.repairCost, this.enchantments);
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
            if (this.customName.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_NAME);
            } else {
                stack.set(DataComponents.CUSTOM_NAME, this.customName.get());
            }
            if (this.itemName.isEmpty()) {
                stack.remove(DataComponents.ITEM_NAME);
            } else {
                stack.set(DataComponents.ITEM_NAME, this.itemName.get());
            }
            stack.set(DataComponents.REPAIR_COST, this.repairCost());
            stack.set(DataComponents.ENCHANTMENTS, this.enchantments());
        }

        public Phase copy() {
            return new Phase(
                this.customName.map(Component::copy),
                this.itemName.map(Component::copy),
                this.repairCost, this.enchantments);
        }

        @Override
        public String toString() {
            return "Phase{customName: %s, itemName: %s, repairCost: %s, enchantments: %s}"
                .formatted(this.customName, this.itemName, this.repairCost, this.enchantments);
        }
    }

    public record PhaseData(
        @Nullable Component customName, @Nullable Component itemName,
        int repairCost, @Nullable ItemEnchantments enchantments
    ) {
        public static PhaseData of(
            @Nullable Component customName, @Nullable Component itemName,
            int repairCost, @Nullable ItemEnchantments enchantments
        ) {
            return new PhaseData(customName, itemName, repairCost, enchantments);
        }

        @Override
        public ItemEnchantments enchantments() {
            return this.enchantments == null ? ItemEnchantments.EMPTY : this.enchantments;
        }

        public PhaseData withCustomName(@Nullable Component customName) {
            return new PhaseData(customName, itemName, repairCost, enchantments);
        }

        public PhaseData withItemName(@Nullable Component itemName) {
            return new PhaseData(customName, itemName, repairCost, enchantments);
        }

        public PhaseData withRepairCost(int repairCost) {
            return new PhaseData(customName, itemName, repairCost, enchantments);
        }

        public PhaseData withEnchantments(@Nullable ItemEnchantments enchantments) {
            return new PhaseData(customName, itemName, repairCost, enchantments);
        }
    }
}
