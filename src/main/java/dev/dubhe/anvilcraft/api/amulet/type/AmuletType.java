package dev.dubhe.anvilcraft.api.amulet.type;

import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.amulet.fromto.Effect;
import dev.dubhe.anvilcraft.api.amulet.fromto.ImmuneDamage;
import dev.dubhe.anvilcraft.api.amulet.fromto.InventoryTick;
import dev.dubhe.anvilcraft.api.amulet.fromto.Obtain;
import dev.dubhe.anvilcraft.init.ModRegistries;
import dev.dubhe.anvilcraft.util.predicate.DamageSourcePredicate;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Getter
@Accessors(fluent = true, chain = false)
@MethodsReturnNonnullByDefault
public class AmuletType {
    public static final Codec<AmuletType> CODEC = Codec.lazyInitialized(ModRegistries.AMULET_TYPE_REGISTRY::byNameCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf, AmuletType> STREAM_CODEC = ByteBufCodecs.registry(
        ModRegistries.AMULET_TYPE_KEY);
    private final Obtain obtain;
    private final Effect effect;
    private final Supplier<ItemStack> amulet;

    protected AmuletType(Obtain obtain, Effect effect, Supplier<ItemStack> amulet) {
        this.obtain = obtain;
        this.effect = effect;
        this.amulet = Lazy.of(amulet);
    }

    public static Builder builder(ResourceLocation typeId) {
        return new Builder(typeId);
    }

    public static Builder builderAnc(String type) {
        return new Builder(type);
    }

    public boolean canObtain(ServerPlayer player, DamageSource source) {
        return this.obtain.canObtain(player, source);
    }

    public boolean matches(ItemLike item) {
        return this.amulet.get().is(item.asItem());
    }

    public void inventoryTick(ServerPlayer player, ItemStack amulet, boolean isEnabled) {
        this.effect.inventoryTick(player, amulet, isEnabled);
    }

    public boolean shouldImmuneDamage(ServerPlayer player, DamageSource source) {
        return this.effect.shouldImmuneDamage(player, source);
    }

    public static class Builder {
        private DamageSourcePredicate.DamageSourceSubPredicate.Builder obtain;
        private final Object2BooleanMap<Obtain> otherObtain = new Object2BooleanArrayMap<>();
        private InventoryTick inventoryTick;
        private ImmuneDamage immuneDamage;
        private Supplier<ItemStack> amulet;
        private boolean canObtainFromDeath = true;
        private boolean isImmuneDamageFromObtain;

        Builder(ResourceLocation typeId) {
            this.obtain = defaultObtain(typeId);
        }

        Builder(String type) {
            this.obtain = defaultObtain(AnvilCraft.of("amulet_valid/" + type));
        }

        private static DamageSourcePredicate.DamageSourceSubPredicate.Builder defaultObtain(
            ResourceLocation typeId
        ) {
            return DamageSourcePredicate.Builder.builder()
                .victim(EntityType.PLAYER)
                .build().and().sub()
                .type(TagKey.create(Registries.DAMAGE_TYPE, typeId))
                .murder(TagKey.create(Registries.ENTITY_TYPE, typeId));
        }

        public Builder obtain(UnaryOperator<DamageSourcePredicate.DamageSourceSubPredicate.Builder> builder) {
            this.obtain = builder.apply(this.obtain);
            return this;
        }

        public Builder obtain(Obtain obtain) {
            this.otherObtain.put(obtain, true);
            return this;
        }

        public Builder obtainOr(Obtain obtain) {
            this.otherObtain.put(obtain, false);
            return this;
        }

        public Builder inventoryTick(InventoryTick inventoryTick) {
            if (this.inventoryTick == null) {
                this.inventoryTick = inventoryTick;
                return this;
            }
            this.inventoryTick = this.inventoryTick.andThen(inventoryTick);
            return this;
        }

        public Builder immuneDamage(ImmuneDamage immuneDamage) {
            if (this.immuneDamage == null) {
                this.immuneDamage = immuneDamage;
                return this;
            }
            this.immuneDamage = this.immuneDamage.and(immuneDamage);
            return this;
        }

        public Builder immuneDamageOr(ImmuneDamage immuneDamage) {
            if (this.immuneDamage == null) {
                this.immuneDamage = immuneDamage;
                return this;
            }
            this.immuneDamage = this.immuneDamage.or(immuneDamage);
            return this;
        }

        public Builder immuneDamageFromObtain() {
            this.isImmuneDamageFromObtain = true;
            return this;
        }

        public Builder cannotObtainFromDeath() {
            this.canObtainFromDeath = false;
            return this;
        }

        public Builder amulet(Supplier<ItemStack> amuletGetter) {
            this.amulet = amuletGetter;
            return this;
        }

        public Builder amulet(ItemStack amulet) {
            return this.amulet(() -> amulet);
        }

        public Builder amulet(ItemLike amulet) {
            return this.amulet(() -> amulet.asItem().getDefaultInstance());
        }

        public AmuletType build() {
            Obtain obtain = this.obtain.build().build()::matches;
            for (var entry : this.otherObtain.object2BooleanEntrySet()) {
                obtain = entry.getBooleanValue() ? obtain.and(entry.getKey()) : obtain.or(entry.getKey());
            }
            if (!this.canObtainFromDeath) {
                obtain = (player, source) -> false;
            }
            InventoryTick inventoryTick = this.inventoryTick;
            if (inventoryTick == null) {
                inventoryTick = InventoryTick.NOP;
            }
            ImmuneDamage immuneDamage = this.immuneDamage;
            if (this.isImmuneDamageFromObtain) {
                immuneDamage = obtain::canObtain;
            }
            if (immuneDamage == null) {
                immuneDamage = ImmuneDamage.NEVER;
            }
            if (this.amulet == null) {
                throw new IllegalArgumentException("The amulet of the amulet type cannot be null!");
            }
            return new AmuletType(obtain, new Effect(inventoryTick, immuneDamage), this.amulet);
        }
    }
}
