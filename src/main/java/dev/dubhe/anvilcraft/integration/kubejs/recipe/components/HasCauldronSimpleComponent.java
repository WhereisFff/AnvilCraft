package dev.dubhe.anvilcraft.integration.kubejs.recipe.components;

import com.mojang.serialization.Codec;
import dev.dubhe.anvilcraft.recipe.anvil.wrap.components.HasCauldronSimple;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;
import org.jetbrains.annotations.NotNull;

public record HasCauldronSimpleComponent() implements RecipeComponent<HasCauldronSimple> {
    public static final HasCauldronSimpleComponent INSTANCE = new HasCauldronSimpleComponent();

    @Override
    public Codec<HasCauldronSimple> codec() {
        return HasCauldronSimple.CODEC.codec();
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(HasCauldronSimple.class);
    }

    @Override
    public @NotNull String toString() {
        return "has_cauldron_simple";
    }
}
