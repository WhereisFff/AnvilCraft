package dev.dubhe.anvilcraft.api.amulet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.util.predicate.DamageSourcePredicate;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.BiFunction;

@Getter
@Accessors(fluent = true, chain = false)
@MethodsReturnNonnullByDefault
public abstract class AmuletType {
    private final DamageSourcePredicate damagePredicate;
    private final ItemStack amulet;

    protected AmuletType(DamageSourcePredicate damagePredicate, ItemStack amulet) {
        this.damagePredicate = damagePredicate;
        this.amulet = amulet;
    }

    public static <T extends AmuletType> Codec<T> codec(BiFunction<DamageSourcePredicate, ItemStack, T> factory) {
        return RecordCodecBuilder.create(ins -> ins.group(
            DamageSourcePredicate.CODEC.fieldOf("damagePredicate").forGetter(AmuletType::damagePredicate),
            ItemStack.CODEC.fieldOf("amulet").forGetter(AmuletType::amulet)
        ).apply(ins, factory));
    }

    public abstract Codec<? extends AmuletType> codec();

    public boolean matchesByDamage(ServerPlayer player, DamageSource source) {
        return this.damagePredicate.matches(player, source);
    }

    public boolean matchesByItem(ItemLike itemLike) {
        return this.amulet.is(itemLike.asItem());
    }

    public abstract void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled);

    public abstract boolean shouldImmuneDamage(ServerPlayer player, DamageSource source);

    public static class Simple extends AmuletType {
        public static final Codec<Simple> CODEC = AmuletType.codec(Simple::new);

        public Simple(DamageSourcePredicate damagePredicate, ItemStack amulet) {
            super(damagePredicate, amulet);
        }

        @Override
        public Codec<? extends AmuletType> codec() {
            return CODEC;
        }

        @Override
        public void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled) {
        }

        @Override
        public boolean shouldImmuneDamage(ServerPlayer player, DamageSource source) {
            return false;
        }
    }

    public static class ImmuneDamageFromObtain extends AmuletType {
        public static final Codec<ImmuneDamageFromObtain> CODEC = AmuletType.codec(ImmuneDamageFromObtain::new);

        public ImmuneDamageFromObtain(DamageSourcePredicate damagePredicate, ItemStack amulet) {
            super(damagePredicate, amulet);
        }

        @Override
        public Codec<? extends AmuletType> codec() {
            return CODEC;
        }

        @Override
        public void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled) {
        }

        @Override
        public boolean shouldImmuneDamage(ServerPlayer player, DamageSource source) {
            return this.matchesByDamage(player, source);
        }
    }
}
