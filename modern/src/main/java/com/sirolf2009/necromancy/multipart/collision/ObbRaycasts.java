package com.sirolf2009.necromancy.multipart.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.OptionalDouble;

/** Ray vs world OBB narrow-phase (slab method in box-local space). Thread-safe — scratch vectors are stack-local. */
public final class ObbRaycasts {

    private ObbRaycasts() {}

    public static OptionalDouble segmentHitParameter(Vec3 origin, Vec3 end, ResolvedObb obb) {
        Vec3 rd = end.subtract(origin);
        double len = rd.length();
        if (len < 1e-8) return OptionalDouble.empty();
        Vec3 dir = rd.scale(1.0 / len);
        OptionalDouble t = rayDistance(origin, dir, len + 1e-4, obb);
        if (t.isEmpty()) return OptionalDouble.empty();
        double tw = t.getAsDouble();
        if (tw < 0 || tw > len + 1e-4) return OptionalDouble.empty();
        return OptionalDouble.of(tw / len);
    }

    public static OptionalDouble rayDistance(Vec3 origin, Vec3 directionUnit, double maxDistance, ResolvedObb obb) {
        Quaternionf invRot = new Quaternionf();
        obb.orientationInto(invRot);
        invRot.conjugate();

        Vector3f localOrigin = new Vector3f(
            (float) (origin.x - obb.centerWorld().x),
            (float) (origin.y - obb.centerWorld().y),
            (float) (origin.z - obb.centerWorld().z));
        invRot.transform(localOrigin);

        Vector3f localDir = new Vector3f((float) directionUnit.x, (float) directionUnit.y, (float) directionUnit.z);
        invRot.transform(localDir);
        float dl = localDir.length();
        if (dl < 1e-8f) return OptionalDouble.empty();
        localDir.div(dl);

        Vector3f h = new Vector3f();
        obb.halfExtentsInto(h);

        float tMin = 0f;
        float tMax = (float) maxDistance;

        for (int axis = 0; axis < 3; axis++) {
            float o = axis == 0 ? localOrigin.x : axis == 1 ? localOrigin.y : localOrigin.z;
            float d = axis == 0 ? localDir.x : axis == 1 ? localDir.y : localDir.z;
            float hm = axis == 0 ? h.x : axis == 1 ? h.y : h.z;
            if (Math.abs(d) < 1e-8f) {
                if (o < -hm || o > hm) return OptionalDouble.empty();
                continue;
            }
            float invD = 1f / d;
            float t1 = (-hm - o) * invD;
            float t2 = (hm - o) * invD;
            if (t1 > t2) {
                float swap = t1;
                t1 = t2;
                t2 = swap;
            }
            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);
            if (tMin > tMax) return OptionalDouble.empty();
        }
        float hit = tMin >= 0 ? tMin : tMax;
        if (hit < 0 || hit > maxDistance) return OptionalDouble.empty();
        return OptionalDouble.of(hit);
    }
}
