package dev.dubhe.anvilcraft.integration.ponder.api.instruction;

import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.PonderSceneElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class InterpolationAnimationElementInstruction<T extends PonderSceneElement> extends TickingInstruction {
    protected final ElementLink<T> link;
    protected final Vec3 delta;
    protected final Interpolation interpolation;
    private final BiConsumer<T, Vec3> setter;
    private final Function<T, Vec3> getter;

    protected final Vec3 direction;
    protected Vec3 origin;
    protected Vec3 target;

    protected T element;

    protected InterpolationAnimationElementInstruction(
        ElementLink<T> link,
        Vec3 delta,
        Interpolation interpolation,
        BiConsumer<T, Vec3> setter,
        Function<T, Vec3> getter
    ) {
        super(false, (int) Math.ceil(interpolation.duration(delta.length())));
        this.link = link;
        this.delta = delta;
        this.interpolation = interpolation;
        this.setter = setter;
        this.getter = getter;
        this.direction = delta.normalize();
        this.origin = Vec3.ZERO;
        this.target = delta;
    }

    @Override
    protected final void firstTick(PonderScene scene) {
        super.firstTick(scene);
        this.element = scene.resolve(this.link);
        if (this.element == null) {
            return;
        }
        this.origin = this.getter.apply(this.element);
        this.target = this.origin.add(this.delta);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (this.element == null) {
            return;
        }
        Vec3 delta = this.direction.scale(this.interpolation.instantaneous(this.totalTicks - this.remainingTicks));
        Vec3 current = this.getter.apply(this.element);
        Vec3 target = current.add(delta);
        if (target.subtract(this.origin).lengthSqr() >= this.delta.lengthSqr()) {
            target = this.target;
        }
        this.setter.accept(this.element, target);
    }
}
