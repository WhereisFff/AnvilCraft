package dev.dubhe.anvilcraft.api.item;

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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

/**
 * 特殊工具属性
 */
public interface IToolProperties {
    /**
     * 多相
     *
     * @param alpha
     * @param beta
     */
    record Multiphase(Phase alpha, Phase beta) {
        public static final Multiphase EMPTY = new Multiphase(Phase.EMPTY, Phase.EMPTY);

        public static final Codec<Multiphase> CODEC = Codec.pair(Phase.CODEC, Phase.CODEC).xmap(
            spaces -> new Multiphase(spaces.getFirst(), spaces.getSecond()),
            multiphase -> new Pair<>(multiphase.alpha, multiphase.beta)
        );
        public static final StreamCodec<ByteBuf, Multiphase> STREAM_CODEC = StreamCodec.of(Multiphase::encode, Multiphase::decode);

        public static Multiphase make(Component customName, @Nullable ItemEnchantments enchantments) {
            MutableComponent customNameExtra = customName.copy();
            Phase alpha = Phase.make(customNameExtra.append("-α"), enchantments);
            Phase beta = Phase.EMPTY.withCustomName(customNameExtra.append("-β"));
            return new Multiphase(alpha, beta);
        }

        public Multiphase switchSpaces() {
            return new Multiphase(beta, alpha);
        }

        private static void encode(ByteBuf buf, Multiphase value) {
            Phase.STREAM_CODEC.encode(buf, value.alpha);
            Phase.STREAM_CODEC.encode(buf, value.beta);
        }

        private static Multiphase decode(ByteBuf buf) {
            Phase spaceA = Phase.STREAM_CODEC.decode(buf);
            Phase spaceB = Phase.STREAM_CODEC.decode(buf);

            return new Multiphase(spaceA, spaceB);
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
