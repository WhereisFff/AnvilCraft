package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemPredicate;
import lombok.Getter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public class HasItem extends HasItemBase<HasItem, ItemPredicate> {
    public HasItem(Vec3 offset, Vec3 range, ItemPredicate item) {
        super(offset, range, item);
    }

    @Override
    public IRecipePredicate.Type<HasItem> getType() {
        return ModRecipePredicateTypes.HAS_ITEM.get();
    }

    public static class Type implements IRecipePredicate.Type<HasItem> {
        private static final MapCodec<HasItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(HasItem::getOffset),
                Vec3.CODEC.fieldOf("range").forGetter(HasItem::getRange),
                ItemPredicate.CODEC.fieldOf("item").forGetter(HasItem::getItem)
            ).apply(instance, HasItem::new)
        );

        @Override
        public @NotNull MapCodec<HasItem> codec() {
            return Type.CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, HasItem> streamCodec() {
            return StreamCodec.of(Type::encode, Type::decode);
        }

        public static void encode(@NotNull FriendlyByteBuf buf, @NotNull HasItem hasItem) {
            buf.writeVec3(hasItem.getOffset());
            buf.writeVec3(hasItem.getRange());
            Tag tag = ItemPredicate.CODEC.encodeStart(NbtOps.INSTANCE, hasItem.item).getOrThrow();
            buf.writeNbt(tag);
        }

        public static @NotNull HasItem decode(@NotNull FriendlyByteBuf buf) {
            Vec3 offset = buf.readVec3();
            Vec3 range = buf.readVec3();
            ItemPredicate item = ItemPredicate.CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow().getFirst();
            return new HasItem(offset, range, item);
        }
    }
}
