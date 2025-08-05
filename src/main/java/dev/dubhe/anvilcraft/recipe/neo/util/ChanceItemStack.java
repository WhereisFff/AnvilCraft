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
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChanceItemStack {
    ItemStack stack;
    NumberProvider chance;

    public ChanceItemStack(Holder<Item> item, int count, DataComponentPatch components, NumberProvider chance) {
        this.stack = new ItemStack(item, count, components);
        this.chance = chance;
    }

    public ChanceItemStack(Holder<Item> item, int count, DataComponentPatch components, float chance) {
        this(item, count, components, ConstantValue.exactly(chance));
    }

    public ChanceItemStack(ItemStack stack, NumberProvider chance) {
        this.stack = stack;
        this.chance = chance;
    }

    public ChanceItemStack(ItemStack stack, float chance) {
        this.stack = stack;
        this.chance = ConstantValue.exactly(chance);
    }

    public static @NotNull ChanceItemStack of(ItemStack stack, float chance) {
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
        NumberProviders.CODEC
            .optionalFieldOf("chance", ConstantValue.exactly(1.0f))
            .forGetter(ChanceItemStack::getChance)
    ).apply(instance, ChanceItemStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChanceItemStack> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull ChanceItemStack value) {
            ItemStack.STREAM_CODEC.encode(buffer, value.stack);
            RecipeUtil.toNetwork(buffer, value.chance);
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

    public int getCount() {
        return this.stack.getCount();
    }

    public DataComponentPatch getComponentsPatch() {
        DataComponentMap components = this.stack.getComponents();
        if (components instanceof PatchedDataComponentMap patched) return patched.asPatch();
        else return DataComponentPatch.EMPTY;
    }

    public SpawnItem toSpawnItem(Vec3 offset) {
        return SpawnItem.builder().item(this.stack).chance(this.chance).offset(offset).build();
    }
}
