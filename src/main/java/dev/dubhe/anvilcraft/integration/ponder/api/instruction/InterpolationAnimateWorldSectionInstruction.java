package dev.dubhe.anvilcraft.integration.ponder.api.instruction;

import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class InterpolationAnimateWorldSectionInstruction extends InterpolationAnimationElementInstruction<WorldSectionElement> {
    public static InterpolationAnimateWorldSectionInstruction move(
        ElementLink<WorldSectionElement> link,
        Vec3 offset,
        Interpolation interpolation
    ) {
        return new InterpolationAnimateWorldSectionInstruction(
            link,
            offset,
            interpolation,
            (wse, v) -> wse.setAnimatedOffset(v, false),
            WorldSectionElement::getAnimatedOffset
        );
    }

    protected InterpolationAnimateWorldSectionInstruction(
        ElementLink<WorldSectionElement> link,
        Vec3 totalDelta,
        Interpolation interpolation,
        BiConsumer<WorldSectionElement, Vec3> setter,
        Function<WorldSectionElement, Vec3> getter
    ) {
        super(link, totalDelta, interpolation, setter, getter);
    }
}
