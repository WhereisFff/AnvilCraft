package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SpawnItem;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChanceItemStack {
    ItemStack stack;
    double chance;

    public ChanceItemStack(ItemStack stack, double chance) {
        this.stack = stack;
        this.chance = chance;
    }

    public ChanceItemStack(Holder<Item> tag, int count, DataComponentPatch components, double chance) {
        this.stack = new ItemStack(tag, count, components);
        this.chance = chance;
    }

    public static @NotNull ChanceItemStack of(ItemStack stack, double chance) {
        return new ChanceItemStack(stack, chance);
    }

    public static final Codec<ChanceItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.ITEM_NON_AIR_CODEC
            .fieldOf("id")
            .forGetter(ChanceItemStack::getItemHolder),
        ExtraCodecs.POSITIVE_INT
            .optionalFieldOf("count", 1)
            .forGetter(ChanceItemStack::getCount),
        DataComponentPatch.CODEC
            .optionalFieldOf("components", DataComponentPatch.EMPTY)
            .forGetter(ChanceItemStack::getComponentsPatch),
        Codec.DOUBLE
            .optionalFieldOf("chance", 1.0)
            .forGetter(ChanceItemStack::getChance)
    ).apply(instance, ChanceItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceItemStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ChanceItemStack value) {
            ItemStack.STREAM_CODEC.encode(buffer, value.stack);
            buffer.writeDouble(value.chance);
        }

        @Override
        public @NotNull ChanceItemStack decode(@NotNull RegistryFriendlyByteBuf buffer) {
            ItemStack decode = ItemStack.STREAM_CODEC.decode(buffer);
            return new ChanceItemStack(decode, buffer.readDouble());
        }
    };

    public @NotNull Item getItem() {
        return this.stack.getItem();
    }

    public Holder<Item> getItemHolder() {
        return this.stack.getItemHolder();
    }

    public int getCount() {
        return this.stack.getCount();
    }

    public DataComponentPatch getComponentsPatch() {
        DataComponentMap components = this.stack.getComponents();
        if (components instanceof PatchedDataComponentMap patched) return patched.asPatch();
        else return DataComponentPatch.EMPTY;
    }

    public SpawnItem toSpawnItem(Vec3 offset) {
        return new SpawnItem(this.getStack(), offset, this.getChance());
    }
}
