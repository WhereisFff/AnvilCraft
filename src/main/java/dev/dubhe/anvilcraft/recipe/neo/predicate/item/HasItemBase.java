package dev.dubhe.anvilcraft.recipe.neo.predicate.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.recipe.neo.IRecipePredicate;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeContext;
import dev.dubhe.anvilcraft.recipe.neo.InWorldRecipeData;
import dev.dubhe.anvilcraft.recipe.neo.util.IItemStackPredicate;
import dev.dubhe.anvilcraft.recipe.neo.util.ItemCache;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class HasItemBase<T extends HasItemBase<T, P>, P extends IItemStackPredicate> implements IRecipePredicate<T> {
    private final Vec3 offset;
    private final Vec3 range;
    protected final P item;

    public HasItemBase(Vec3 offset, Vec3 range, P item) {
        this.offset = offset;
        this.range = range;
        this.item = item;
    }

    @Override
    public boolean test(@NotNull InWorldRecipeContext context) {
        return this.item.testCount(this.getItem(context).getCount());
    }

    public ItemCache.ICacheInput getItem(@NotNull InWorldRecipeContext context) {
        final InWorldRecipeData<ItemCache.ICacheInput> cacheInput = InWorldRecipeData.of(
            AnvilCraft.of("item_cache_input/%s".formatted(this.hashCode())),
            (ctx, key) -> {
                ItemCache itemCache = ctx.computeIfAbsent(ItemCache.ITEM_CACHE);
                return itemCache.getInput(this.item.testIgnoreCount(), context.getPos().add(this.offset), this.range);
            }
        );
        return context.computeIfAbsent(cacheInput);
    }

    public abstract static class AbstractType<T extends HasItemBase<T, P>, P extends IItemStackPredicate>
        implements IRecipePredicate.Type<T> {
        private final MapCodec<T> codec = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Vec3.CODEC.fieldOf("offset").forGetter(T::getOffset),
                Vec3.CODEC.fieldOf("range").forGetter(T::getRange),
                this.itemCodec()
            ).apply(instance, this::create)
        );
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.of(this::encode, this::decode);

        protected abstract T create(Vec3 offset, Vec3 range, P item);

        protected abstract P decodeItem(FriendlyByteBuf buf);

        protected abstract void encodeItem(FriendlyByteBuf buf, P item);

        protected abstract RecordCodecBuilder<T, P> itemCodec();

        @Override
        public @NotNull MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }

        public void encode(@NotNull FriendlyByteBuf buf, @NotNull T hasItem) {
            buf.writeVec3(hasItem.getOffset());
            buf.writeVec3(hasItem.getRange());
            this.encodeItem(buf, hasItem.item);
        }

        public @NotNull T decode(@NotNull FriendlyByteBuf buf) {
            Vec3 offset = buf.readVec3();
            Vec3 range = buf.readVec3();
            P item = this.decodeItem(buf);
            return this.create(offset, range, item);
        }
    }
}
