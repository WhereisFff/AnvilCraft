package dev.dubhe.anvilcraft.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class GenericZombieAttackGoal<T extends PathfinderMob> extends MeleeAttackGoal {
    private final T zombie;
    private int raiseArmTicks;

    public GenericZombieAttackGoal(T zombie, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(zombie, speedModifier, followingTargetEvenIfNotSeen);
        this.zombie = zombie;
    }

    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    public void stop() {
        super.stop();
        this.zombie.setAggressive(false);
    }

    public void tick() {
        super.tick();
        ++this.raiseArmTicks;
        this.zombie.setAggressive(
            this.raiseArmTicks >= 5
                && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2
        );

    }
}
