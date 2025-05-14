package dev.dubhe.anvilcraft.api.amulet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.util.predicate.DamageSourcePredicate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
public class SimpleAmuletType extends AmuletType {
    public static final Codec<SimpleAmuletType> CODEC = RecordCodecBuilder.create(ins -> ins.group(
        DamageSourcePredicate.CODEC.fieldOf("sourcePredicate").forGetter(SimpleAmuletType::damagePredicate),
        ItemStack.CODEC.fieldOf("amulet").forGetter(SimpleAmuletType::amulet)
    ).apply(ins, SimpleAmuletType::new));

    public SimpleAmuletType(DamageSourcePredicate sourcePredicate, ItemStack amulet) {
        super(sourcePredicate, amulet);
    }

    @Override
    public Codec<? extends AmuletType> codec() {
        return CODEC;
    }

    @Override
    public boolean shouldIgnoreDamage(ServerPlayer player, DamageSource source) {
        return this.matchesByDamage(player, source);
    }
}
