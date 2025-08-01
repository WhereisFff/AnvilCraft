package dev.dubhe.anvilcraft.recipe.neo.outcome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipeOutcomeTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipeOutcome;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemCache;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class SpawnItem implements IRecipeOutcome<SpawnItem> {
    private final ItemStack item;
    private final Vec3 offset;
    private final double chance;

    public SpawnItem(ItemStack item, Vec3 offset, double chance) {
        this.item = item;
        this.offset = offset;
        this.chance = chance;
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
                Codec.DOUBLE.optionalFieldOf("chance", 1.0)
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
            buf.writeDouble(spawnItem.chance);
        }

        public static @NotNull SpawnItem decode(@NotNull RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            Vec3 vec3 = buf.readVec3();
            double chance = buf.readDouble();
            return new SpawnItem(stack, vec3, chance);
        }
    }

    public static class Builder {
    }
}
