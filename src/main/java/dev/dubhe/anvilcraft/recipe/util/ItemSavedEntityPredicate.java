package dev.dubhe.anvilcraft.recipe.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.item.HasMobBlockItem;
import dev.dubhe.anvilcraft.recipe.transform.NumericTagValuePredicate;
import dev.dubhe.anvilcraft.util.CodecUtil;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ItemSavedEntityPredicate implements ItemSubPredicate {

    public final EntityType<?> entityType;
    private final List<NumericTagValuePredicate> predicates;

    public static final Codec<ItemSavedEntityPredicate> CODEC = RecordCodecBuilder.create(ins -> ins.group(
            CodecUtil.ENTITY_CODEC.fieldOf("entityType").forGetter(o -> o.entityType),
            NumericTagValuePredicate.CODEC
                .listOf()
                .optionalFieldOf("tagPredicates")
                .forGetter(o -> Util.intoOptional(o.predicates))
        )
        .apply(ins, ItemSavedEntityPredicate::new));

    public ItemSavedEntityPredicate(EntityType<?> entityType, List<NumericTagValuePredicate> predicates) {
        this.entityType = entityType;
        this.predicates = predicates;
    }

    public ItemSavedEntityPredicate(EntityType<?> entityType, Optional<List<NumericTagValuePredicate>> predicates) {
        this(entityType, predicates.orElseGet(ArrayList::new));
    }

    public static ItemSavedEntityPredicate of(EntityType<?> entityType) {
        return new ItemSavedEntityPredicate(entityType, new ArrayList<>());
    }

    public ItemSavedEntityPredicate predicate(@NotNull Consumer<NumericTagValuePredicate.Builder> predicateBuilder) {
        NumericTagValuePredicate.Builder builder = NumericTagValuePredicate.builder();
        predicateBuilder.accept(builder);
        predicates.add(builder.build());
        return this;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        if (!itemStack.has(ModComponents.SAVED_ENTITY)) return false;
        HasMobBlockItem.SavedEntity component = itemStack.get(ModComponents.SAVED_ENTITY);
        if (component == null) return false;
        CompoundTag tag = component.getTag();
        Optional<EntityType<?>> optional = EntityType.by(tag);
        if (optional.isEmpty()) return false;
        EntityType<?> type = optional.get();
        if (!type.equals(this.entityType)) return false;
        return predicates.stream().allMatch(it -> it.test(tag));
    }



}
