package dev.dubhe.anvilcraft.recipe.neo.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@Getter
public class NbtPredicate implements Predicate<Tag> {
    public static final Codec<NbtPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(NbtPredicate::new, NbtPredicate::getTag);
    public static final StreamCodec<ByteBuf, NbtPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
        NbtPredicate::new,
        NbtPredicate::getTag
    );
    private final CompoundTag tag;

    public NbtPredicate(CompoundTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(Tag tag) {
        return tag != null && NbtUtils.compareNbt(this.tag, tag, true);
    }

    public boolean test(@NotNull ItemStack stack) {
        CustomData customdata = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customdata.matchedBy(this.tag);
    }

    public boolean test(Entity entity) {
        return this.test(getEntityTagToCompare(entity));
    }

    public static @NotNull CompoundTag getEntityTagToCompare(@NotNull Entity entity) {
        CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
        if (entity instanceof Player) {
            ItemStack itemstack = ((Player) entity).getInventory().getSelected();
            if (!itemstack.isEmpty()) {
                compoundtag.put("SelectedItem", itemstack.save(entity.registryAccess()));
            }
        }
        return compoundtag;
    }
}
