package dev.dubhe.anvilcraft.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.dubhe.anvilcraft.block.entity.DeflectionRingBlockEntity;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@SuppressWarnings("DuplicatedCode")
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Vec3 deltaMovement;

    @Shadow private Level level;

    @Shadow public abstract Vec3 position();

    @Shadow public abstract void setPos(Vec3 pos);

    @Shadow public abstract void setPos(double x, double y, double z);

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract Vec3 getDeltaMovement();

    @Shadow public abstract void setPosRaw(double x, double y, double z);

    @Shadow public abstract void setBoundingBox(AABB bb);

    @Shadow protected abstract AABB makeBoundingBox();

    @Shadow public abstract float distanceTo(Entity entity);

    @Redirect(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V", ordinal = 1))
    public void anvilcraft$fixFallingBlockEntity(Entity instance, double x, double y, double z, @Share("isFixed") LocalBooleanRef isFixed) {
        isFixed.set(false);
        Vec3 vec3 = new Vec3(x - getX(), y - getY(), z - getZ());
        if (((Object) this instanceof Projectile || (Object) this instanceof FallingBlockEntity) && vec3.length() > 0.98) {
            Vec3 s = position();
            Vec3 e = vec3.add(s);
            ArrayList<Pair<BlockPos, Double>> blockPosList = new ArrayList<>();
            for (BlockPos blockPos : DeflectionRingBlockEntity.getAllBlocks(level)) {
                Vec3 q = blockPos.getCenter();
                double a = s.distanceTo(q);
                double b = e.distanceTo(q);
                double c = s.distanceTo(e);
                double d = -(b * b - c * c - a * a) / (2 * c);
                double distance = Math.sqrt(a * a - d * d);
                if (distance <= 0.56747 && d > 0)
                    blockPosList.add(Pair.of(blockPos, d));
            }
            double distance = Double.MAX_VALUE;
            BlockPos blockPos = null;
            for (Pair<BlockPos, Double> pos : blockPosList) {
                if (distance > pos.right()) {
                    distance = pos.right();
                    blockPos = pos.left();
                }
            }
            if (blockPos == null) {
                setPos(e);
                return;
            }
            double a = distance / vec3.length();

            if (a > 1) {
                setPos(e);
                return;
            }
            setPos(vec3.multiply(a, a, a).add(s));
            isFixed.set(true);
            return;
        }
        setPos(x, y, z);
    }

    @Redirect(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;equal(DD)Z", ordinal = 0))
    public boolean anvilcraft$cancelCollision1(double x, double y, @Share("isFixed") LocalBooleanRef isFixed) {
        return isFixed.get() || Mth.equal(x, y);
    }

    @Redirect(method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;equal(DD)Z", ordinal = 1))
    public boolean anvilcraft$cancelCollision2(double x, double y, @Share("isFixed") LocalBooleanRef isFixed) {
        return isFixed.get() || Mth.equal(x, y);
    }

    @Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true)
    public void anvilcraft$changeProjectilePosSetResult(double x, double y, double z, CallbackInfo ci) {
        if (!((Object) this instanceof Projectile)) return;
        Vec3 vec3 = new Vec3(x - getX(), y - getY(), z - getZ());
        if (vec3.add(getDeltaMovement().scale(-1)).length() > 0.5) return;
        if (((Object) this instanceof Projectile || (Object) this instanceof FallingBlockEntity) && vec3.length() > 0.98) {
            Vec3 s = position();
            Vec3 e = vec3.add(s);
            ArrayList<Pair<BlockPos, Double>> blockPosList = new ArrayList<>();
            for (BlockPos blockPos : DeflectionRingBlockEntity.getAllBlocks(level)) {
                Vec3 q = blockPos.getCenter();
                double a = s.distanceTo(q);
                double b = e.distanceTo(q);
                double c = s.distanceTo(e);
                double d = -(b * b - c * c - a * a) / (2 * c);
                double distance = Math.sqrt(a * a - d * d);
                if (distance <= 0.56747 && d > 0)
                    blockPosList.add(Pair.of(blockPos, d));
            }
            double distance = Double.MAX_VALUE;
            BlockPos blockPos = null;
            for (Pair<BlockPos, Double> pos : blockPosList) {
                if (distance > pos.right()) {
                    distance = pos.right();
                    blockPos = pos.left();
                }
            }
            if (blockPos == null) return;
            double a = distance / vec3.length();

            if (a > 1) return;
            Vec3 pos = vec3.multiply(a, a, a).add(s);
            setPosRaw(pos.x, pos.y, pos.z);
            setBoundingBox(makeBoundingBox());
            ci.cancel();
        }
    }
}
