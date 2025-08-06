package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.recipe.neo.outcome.SpawnItem;
import dev.dubhe.anvilcraft.util.RecipeUtil;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChanceItemStack {
    private final ItemStack stack;
    private final NumberProvider amount;

    public ChanceItemStack(Holder<Item> item, int maxCount, DataComponentPatch components, NumberProvider amount) {
        this(new ItemStack(item, maxCount, components), amount);
    }

    public ChanceItemStack(Holder<Item> item, int maxCount, DataComponentPatch components, float chance) {
        this(item, maxCount, components, BinomialDistributionGenerator.binomial(maxCount, chance));
    }

    public ChanceItemStack(Holder<Item> item, int maxCount, DataComponentPatch components) {
        this(item, maxCount, components, ConstantValue.exactly(maxCount));
    }

    private ChanceItemStack(ItemStack stack, NumberProvider amount) {
        this.stack = stack;
        this.amount = amount;
    }

    public ChanceItemStack(ItemStack stack, float chance) {
        this(stack, BinomialDistributionGenerator.binomial(stack.getCount(), chance));
    }

    public static @NotNull ChanceItemStack of(ItemLike item, int maxCount, NumberProvider amount) {
        return new ChanceItemStack(new ItemStack(item, maxCount), amount);
    }

    public static @NotNull ChanceItemStack of(ItemStack stack) {
        return new ChanceItemStack(stack, ConstantValue.exactly(1));
    }

    public static @NotNull ChanceItemStack of(ItemStack stack, float chance) {
        return new ChanceItemStack(stack, chance);
    }

    public static @NotNull ChanceItemStack of(ItemStack stack, int count, float chance) {
        return new ChanceItemStack(stack.copyWithCount(count), chance);
    }

    public static final Codec<ChanceItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.ITEM_NON_AIR_CODEC
            .fieldOf("id")
            .forGetter(ChanceItemStack::getItemHolder),
        ExtraCodecs.POSITIVE_INT
            .optionalFieldOf("maxCount", 1)
            .forGetter(ChanceItemStack::getMaxCount),
        DataComponentPatch.CODEC
            .optionalFieldOf("components", DataComponentPatch.EMPTY)
            .forGetter(ChanceItemStack::getComponentsPatch),
        NumberProviders.CODEC
            .optionalFieldOf("chance", ConstantValue.exactly(1.0f))
            .forGetter(ChanceItemStack::getAmount)
    ).apply(instance, ChanceItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceItemStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ChanceItemStack value) {
            ItemStack.STREAM_CODEC.encode(buffer, value.stack);
            RecipeUtil.toNetwork(buffer, value.amount);
        }

        @Override
        public @NotNull ChanceItemStack decode(@NotNull RegistryFriendlyByteBuf buffer) {
            ItemStack decode = ItemStack.STREAM_CODEC.decode(buffer);
            NumberProvider chance = RecipeUtil.fromNetwork(buffer);
            return new ChanceItemStack(decode, chance);
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
        return SpawnItem.builder().item(this.stack).chance(this.amount).offset(offset).build();
    }
}
