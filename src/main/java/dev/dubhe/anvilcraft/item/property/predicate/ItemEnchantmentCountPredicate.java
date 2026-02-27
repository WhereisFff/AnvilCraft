package dev.dubhe.anvilcraft.item.property.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record ItemEnchantmentCountPredicate(int min, int max) implements ItemSubPredicate {
    public static final MapCodec<ItemEnchantmentCountPredicate> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.INT
            .fieldOf("min")
            .forGetter(ItemEnchantmentCountPredicate::min),
        Codec.INT
            .fieldOf("max")
            .forGetter(ItemEnchantmentCountPredicate::max)
    ).apply(inst, ItemEnchantmentCountPredicate::new));
    public static final StreamCodec<ByteBuf, ItemEnchantmentCountPredicate> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ItemEnchantmentCountPredicate::min,
        ByteBufCodecs.VAR_INT,
        ItemEnchantmentCountPredicate::max,
        ItemEnchantmentCountPredicate::new
    );

    public static ItemEnchantmentCountPredicate count(int count) {
        return new ItemEnchantmentCountPredicate(count, count);
    }

    @Override
    public boolean matches(ItemStack stack) {
        int size = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).size();
        return size >= this.min && size <= this.max;
    }
}
