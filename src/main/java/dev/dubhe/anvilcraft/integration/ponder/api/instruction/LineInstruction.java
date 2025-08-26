package dev.dubhe.anvilcraft.integration.ponder.api.instruction;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.world.phys.Vec3;

public class LineInstruction extends TickingInstruction {
    private final int color;
    private final Vec3 start;
    private final Vec3 end;
    private final float thickness;

    public LineInstruction(int color, Vec3 start, Vec3 end, int ticks, float thickness) {
        super(false, ticks);
        this.color = color;
        this.start = start;
        this.end = end;
        this.thickness = thickness;
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        scene.getOutliner()
            .showLine(start, start, end)
            .lineWidth(thickness)
            .colored(color);
    }
}
