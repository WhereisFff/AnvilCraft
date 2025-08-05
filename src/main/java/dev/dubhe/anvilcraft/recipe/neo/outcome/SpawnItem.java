package dev.dubhe.anvilcraft.recipe.neo.outcome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeOutcomeTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemCache;
import dev.dubhe.anvilcraft.util.RecipeUtil;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class SpawnItem implements IRecipeOutcome<SpawnItem> {
    private final ItemStack item;
    private final Vec3 offset;
    private final NumberProvider chance;

    public SpawnItem(ItemStack item, Vec3 offset, NumberProvider chance) {
        this.item = item;
        this.offset = offset;
        this.chance = chance;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public Type getType() {
        return ModRecipeOutcomeTypes.SPAWN_ITEM.get();
    }

    @Override
    public void accept(@NotNull InWorldRecipeContext context) {
        ItemCache cache = context.computeIfAbsent(ItemCache.ITEM_CACHE);
        ItemCache.ICacheOutput output = cache.getOutput(this.item, context.getPos().add(this.offset));
        output.grow(this.item, true);
        context.putAcceptor(ItemCache.ITEM_CACHE.location(), ItemCache.DEFAULT_ACCEPTOR);
    }

    public static class Type implements IRecipeOutcome.Type<SpawnItem> {
        private static final MapCodec<SpawnItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ItemStack.CODEC.fieldOf("item")
                    .forGetter(SpawnItem::getItem),
                Vec3.CODEC.fieldOf("offset")
                    .forGetter(SpawnItem::getOffset),
                NumberProviders.CODEC.optionalFieldOf("chance", ConstantValue.exactly(1.0f))
                    .forGetter(SpawnItem::getChance)
            ).apply(instance, SpawnItem::new)
        );

        @Override
        public @NotNull MapCodec<SpawnItem> codec() {
            return Type.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, SpawnItem> streamCodec() {
            return StreamCodec.of(Type::encode, Type::decode);
        }

        public static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull SpawnItem spawnItem) {
            ItemStack.STREAM_CODEC.encode(buf, spawnItem.item);
            buf.writeVec3(spawnItem.offset);
            RecipeUtil.toNetwork(buf, spawnItem.chance);
        }

        public static @NotNull SpawnItem decode(@NotNull RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            Vec3 vec3 = buf.readVec3();
            NumberProvider chance = RecipeUtil.fromNetwork(buf);
            return new SpawnItem(stack, vec3, chance);
        }
    }

    public static class Builder {
        private Vec3 offset = Vec3.ZERO;
        private NumberProvider chance = ConstantValue.exactly(1.0f);
        private ItemStack item = ItemStack.EMPTY;

        public Builder offset(Vec3 offset) {
            this.offset = offset;
            return this;
        }

        public Builder offset(double x, double y, double z) {
            this.offset = new Vec3(x, y, z);
            return this;
        }

        public Builder below(double below) {
            return this.offset(Vec3.ZERO.subtract(0, below, 0));
        }

        public Builder below() {
            return this.below(1);
        }

        public Builder above(double above) {
            return this.offset(Vec3.ZERO.add(0, above, 0));
        }

        public Builder above() {
            return this.above(1);
        }

        public Builder chance(NumberProvider chance) {
            this.chance = chance;
            return this;
        }

        public Builder chance(float chance) {
            return this.chance(ConstantValue.exactly(chance));
        }

        public Builder item(ItemStack item) {
            this.item = item;
            return this;
        }

        public Builder item(@NotNull Item item) {
            return this.item(item.getDefaultInstance());
        }

        public Builder item(@NotNull ItemLike item) {
            return this.item(item.asItem());
        }

        public SpawnItem build() {
            return new SpawnItem(this.item, this.offset, this.chance);
        }
    }
}
