package dev.dubhe.anvilcraft.integration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.AnvilCraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.annotation.Nullable;

public class IntegrationUtil {
    public static Root root = Root.EMPTY;

    public static void load() {
        try (InputStream stream = AnvilCraft.class.getClassLoader().getResourceAsStream("integrations.json")) {
            if (stream == null) return;
            InputStreamReader reader = new InputStreamReader(stream);
            JsonObject object = AnvilCraft.GSON.fromJson(reader, JsonObject.class);
            DataResult<Pair<Root, JsonElement>> result = Root.CODEC.decode(JsonOps.INSTANCE, object);
            Pair<Root, JsonElement> pair = result.getOrThrow();
            root = pair.getFirst();
        } catch (Exception e) {
            AnvilCraft.LOGGER.error(e.getMessage(), e);
        }
    }

    public record Root(Integrations integration, List<Additional> additional) {
        public static final Root EMPTY = new Root(Integrations.EMPTY, List.of());
        public static final MapCodec<Root> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Integrations.MAP_CODEC.fieldOf("integrations").forGetter(Root::integration),
            Additional.MAP_CODEC.codec().listOf().optionalFieldOf("additional", List.of()).forGetter(Root::additional)
        ).apply(instance, Root::new));
        public static final Codec<Root> CODEC = MAP_CODEC.codec();
    }

    public record Integrations(
        List<Integration> guide,
        List<Integration> recipeQuery,
        List<Integration> infoHud,
        List<Integration> modify,
        List<Integration> interaction,
        List<Integration> compatible
    ) {
        public static final Integrations EMPTY = new Integrations(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        public static final MapCodec<Integrations> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Integration.MAP_CODEC.codec().listOf().optionalFieldOf("guide", List.of()).forGetter(Integrations::guide),
            Integration.MAP_CODEC.codec().listOf().optionalFieldOf("recipe_query", List.of()).forGetter(Integrations::recipeQuery),
            Integration.MAP_CODEC.codec().listOf().optionalFieldOf("info_hud", List.of()).forGetter(Integrations::infoHud),
            Integration.MAP_CODEC.codec().listOf().optionalFieldOf("modify", List.of()).forGetter(Integrations::modify),
            Integration.MAP_CODEC.codec().listOf().optionalFieldOf("interaction", List.of()).forGetter(Integrations::interaction),
            Integration.MAP_CODEC.codec().listOf().optionalFieldOf("compatible", List.of()).forGetter(Integrations::compatible)
        ).apply(instance, Integrations::new));
    }

    public record Integration(String id, IntegrationType type, Component name, @Nullable Component description, Links links) {
        public static final MapCodec<Integration> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Integration::id),
            IntegrationType.MAP_CODEC.forGetter(Integration::type),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Integration::name),
            ComponentSerialization.CODEC.optionalFieldOf("description", null).forGetter(Integration::description),
            Links.MAP_CODEC.fieldOf("links").forGetter(Integration::links)
        ).apply(instance, Integration::new));
    }

    public record Additional(String id, Component name, @Nullable Component description, Links links) {
        public static final MapCodec<Additional> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Additional::id),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Additional::name),
            ComponentSerialization.CODEC.optionalFieldOf("description", null).forGetter(Additional::description),
            Links.MAP_CODEC.fieldOf("links").forGetter(Additional::links)
        ).apply(instance, Additional::new));
    }

    public record Links(List<Link> target, List<Link> extra) {
        public static final MapCodec<Links> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Link.MAP_CODEC.codec().listOf().optionalFieldOf("target", List.of()).forGetter(Links::target),
            Link.MAP_CODEC.codec().listOf().optionalFieldOf("extra", List.of()).forGetter(Links::extra)
        ).apply(instance, Links::new));
    }

    public record Link(String type, String url) {
        public static final MapCodec<Link> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(Link::type),
            Codec.STRING.fieldOf("url").forGetter(Link::url)
        ).apply(instance, Link::new));
    }

    public enum IntegrationType {
        EXTRA,
        BUILTIN;

        public static final MapCodec<IntegrationType> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(type -> type.name().toLowerCase())
        ).apply(instance, name -> IntegrationType.valueOf(name.toUpperCase())));
    }
}
