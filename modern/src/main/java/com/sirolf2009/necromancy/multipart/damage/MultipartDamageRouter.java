package com.sirolf2009.necromancy.multipart.damage;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.collision.CollisionResolve;
import com.sirolf2009.necromancy.multipart.collision.ObbRaycasts;
import com.sirolf2009.necromancy.multipart.collision.ResolvedObb;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.OptionalDouble;

/**
 * Routes hits into bodyparts (oriented narrow-phase) and applies modular damage policies.
 */
public final class MultipartDamageRouter {

    private MultipartDamageRouter() {}

    public record DamageApplyResult(boolean applied, float dealtToPart, boolean partDestroyed, boolean severed) {}

    /** Legacy single-root drain — retained when {@link RootMobEntity#multipartUsesPerPartHealth()} is false. */
    public static boolean hurtFromPart(RootMobEntity root, BodyPartNode part, DamageSource source, float amount) {
        if (root.multipartUsesPerPartHealth()) {
            DamageApplyResult r = applyDamageToPart(root, part, source, amount);
            return r.applied();
        }
        float m = DamagePipeline.modifyIncoming(amount, part);
        return root.asMultipartRoot().hurt(source, m);
    }

    public static DamageApplyResult applyDamageToPart(RootMobEntity root, BodyPartNode part, DamageSource source, float rawAmount) {
        if (part.damageState().destroyed()) {
            return new DamageApplyResult(false, 0f, true, part.damageState().severed());
        }
        float v = DamagePipeline.modifyIncoming(rawAmount, part);
        float cur = part.damageState().currentHealth();
        float next = Math.max(0f, cur - v);
        part.damageState().setCurrentHealth(next);
        boolean destroyed = next <= 1e-4f;
        if (destroyed) {
            part.damageState().setDestroyed(true);
            part.damageState().setSevered(part.damageState().severed() || root.multipartSeverPartWhenDestroyed());
            part.damageState().addFlag(PartFunctionalFlag.DISABLED);
        }
        float residual = root.multipartResidualDamageToVanilla(rawAmount, v, part, source);
        if (residual > 1e-4f) {
            root.asMultipartRoot().hurt(source, residual);
        }
        return new DamageApplyResult(true, v, destroyed, part.damageState().severed());
    }

    public static BodyPartNode findPartAtWorldPoint(TransformHierarchy hierarchy, Vec3 hitPos) {
        BodyPartNode best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (BodyPartNode n : hierarchy.nodes()) {
            if (!n.hitbox().collisionEnabled() || !n.attachedToParent()) continue;
            ResolvedObb obb = n.simulationCollisionObb();
            boolean narrow = CollisionResolve.containsWorldPoint(hitPos, obb);
            if (!narrow && !n.simulationBroadphase().contains(hitPos)) continue;
            if (!narrow) continue;
            Vec3 c = obb.centerWorld();
            double d = c.distanceToSqr(hitPos);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = n;
            }
        }
        return best;
    }

    /** Narrow-phase segment vs oriented limbs; falls back to broad-phase point sampling at segment midpoint. */
    public static BodyPartNode findPartAlongSegment(TransformHierarchy hierarchy, Vec3 start, Vec3 end) {
        BodyPartNode best = null;
        double bestU = Double.MAX_VALUE;
        for (BodyPartNode n : hierarchy.nodes()) {
            if (!n.hitbox().collisionEnabled() || !n.attachedToParent()) continue;
            OptionalDouble u = ObbRaycasts.segmentHitParameter(start, end, n.simulationCollisionObb());
            if (u.isEmpty()) {
                Vec3 mid = start.lerp(end, 0.5);
                if (!CollisionResolve.containsWorldPoint(mid, n.simulationCollisionObb())) continue;
                u = OptionalDouble.of(0.5);
            }
            double hit = u.getAsDouble();
            if (hit < bestU) {
                bestU = hit;
                best = n;
            }
        }
        return best;
    }

    public static float combineMultipliers(BodyPartNode part, Map<String, Float> tagBonus) {
        float m = part.hitbox().damageMultiplier();
        if (tagBonus == null || tagBonus.isEmpty()) return m;
        return tagBonus.values().stream().reduce(m, (a, b) -> a * b);
    }
}
