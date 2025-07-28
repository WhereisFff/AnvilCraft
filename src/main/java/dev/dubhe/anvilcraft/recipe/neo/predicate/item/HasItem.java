package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModRecipePredicateTypes;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemPredicate;
import lombok.Getter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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

    public static class Type extends AbstractType<HasItem, ItemPredicate> {
        @Override
        protected HasItem create(Vec3 offset, Vec3 range, ItemPredicate item) {
            return new HasItem(offset, range, item);
        }

        @Override
        protected ItemPredicate decodeItem(@NotNull FriendlyByteBuf buf) {
            return ItemPredicate.CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow().getFirst();
        }

        @Override
        protected void encodeItem(@NotNull FriendlyByteBuf buf, ItemPredicate item) {
            Tag tag = ItemPredicate.CODEC.encodeStart(NbtOps.INSTANCE, item).getOrThrow();
            buf.writeNbt(tag);
        }

        @Override
        protected RecordCodecBuilder<HasItem, ItemPredicate> itemCodec() {
            return ItemPredicate.CODEC.fieldOf("item").forGetter(HasItem::getItem);
        }
    }
}
