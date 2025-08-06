package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SpawnItem;
import dev.dubhe.anvilcraft.util.CodecUtil;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChanceItemStack {
    private final ItemStack stack;
    private final NumberProvider count;

    private ChanceItemStack(ItemStack stack, NumberProvider count) {
        this.stack = stack;
        this.count = count;
    }

    private ChanceItemStack(Holder<Item> item, DataComponentPatch components, NumberProvider count) {
        this(new ItemStack(item, 1, components), count);
    }

    public static @NotNull ChanceItemStack of(ItemLike item, NumberProvider amount) {
        return new ChanceItemStack(new ItemStack(item, 1), amount);
    }

    public static @NotNull ChanceItemStack of(ItemLike item, int count) {
        return new ChanceItemStack(new ItemStack(item, 1), ConstantValue.exactly(count));
    }

    public static @NotNull ChanceItemStack of(@NotNull ItemStack stack, NumberProvider amount) {
        return new ChanceItemStack(stack.copyWithCount(1), amount);
    }

    public static @NotNull ChanceItemStack of(@NotNull ItemStack stack) {
        return new ChanceItemStack(stack.copyWithCount(1), ConstantValue.exactly(stack.getCount()));
    }

    public static @NotNull ChanceItemStack of(@NotNull ItemStack stack, int count) {
        return new ChanceItemStack(stack.copyWithCount(1), ConstantValue.exactly(count));
    }

    public static @NotNull ChanceItemStack of(@NotNull ItemStack stack, int count, float chance) {
        return new ChanceItemStack(stack.copyWithCount(1), BinomialDistributionGenerator.binomial(count, chance));
    }

    public static @NotNull ChanceItemStack of(@NotNull ItemStack stack, float chance) {
        return new ChanceItemStack(stack.copyWithCount(1), BinomialDistributionGenerator.binomial(stack.getCount(), chance));
    }

    public static final Codec<ChanceItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.ITEM_NON_AIR_CODEC
            .fieldOf("id")
            .forGetter(ChanceItemStack::getItemHolder),
        DataComponentPatch.CODEC
            .optionalFieldOf("components", DataComponentPatch.EMPTY)
            .forGetter(ChanceItemStack::getComponentsPatch),
        CodecUtil.NUMBER_PROVIDER_CODEC
            .optionalFieldOf("count", ConstantValue.exactly(1.0f))
            .forGetter(ChanceItemStack::getCount)
    ).apply(instance, ChanceItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceItemStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ChanceItemStack value) {
            ItemStack.STREAM_CODEC.encode(buffer, value.stack);
            RecipeUtil.toNetwork(buffer, value.count);
        }

        @Override
        public @NotNull ChanceItemStack decode(@NotNull RegistryFriendlyByteBuf buffer) {
            ItemStack decode = ItemStack.STREAM_CODEC.decode(buffer);
            NumberProvider count = RecipeUtil.fromNetwork(buffer);
            return new ChanceItemStack(decode, count);
        }
    };

    public @NotNull Item getItem() {
        return this.stack.getItem();
    }

    public Holder<Item> getItemHolder() {
        return this.stack.getItemHolder();
    }

    public int getMaxCount() {
        return this.stack.getCount();
    }

    public DataComponentPatch getComponentsPatch() {
        DataComponentMap components = this.stack.getComponents();
        if (components instanceof PatchedDataComponentMap patched) return patched.asPatch();
        else return DataComponentPatch.EMPTY;
    }

    public SpawnItem toSpawnItem(Vec3 offset) {
        return SpawnItem.builder().item(this.stack).chance(this.count).offset(offset).build();
    }
}
