package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Cheap conservative queries shared by ray / overlap paths (inflate segments, inflate rays).
 */
public final class BroadphaseQueries {

    private BroadphaseQueries() {
    }

    /** Minimal axis-aligned box containing segment {@code a}→{@code b}, expanded by {@code padding}. */
    public static AABB segmentBounds(Vec3 a, Vec3 b, double padding) {
        double minX = Math.min(a.x, b.x) - padding;
        double minY = Math.min(a.y, b.y) - padding;
        double minZ = Math.min(a.z, b.z) - padding;
        double maxX = Math.max(a.x, b.x) + padding;
        double maxY = Math.max(a.y, b.y) + padding;
        double maxZ = Math.max(a.z, b.z) + padding;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** Ray vs AABB slab test (fast narrow rejection before OBB work). */
    public static boolean rayIntersectsAabb(Vec3 origin, Vec3 direction, double maxDist, AABB box) {
        double invX = direction.x == 0 ? Double.POSITIVE_INFINITY : 1.0 / direction.x;
        double invY = direction.y == 0 ? Double.POSITIVE_INFINITY : 1.0 / direction.y;
        double invZ = direction.z == 0 ? Double.POSITIVE_INFINITY : 1.0 / direction.z;

        double tMin = 0.0;
        double tMax = maxDist;

        double t1 = (box.minX - origin.x) * invX;
        double t2 = (box.maxX - origin.x) * invX;
        tMin = Math.max(tMin, Math.min(t1, t2));
        tMax = Math.min(tMax, Math.max(t1, t2));

        t1 = (box.minY - origin.y) * invY;
        t2 = (box.maxY - origin.y) * invY;
        tMin = Math.max(tMin, Math.min(t1, t2));
        tMax = Math.min(tMax, Math.max(t1, t2));

        t1 = (box.minZ - origin.z) * invZ;
        t2 = (box.maxZ - origin.z) * invZ;
        tMin = Math.max(tMin, Math.min(t1, t2));
        tMax = Math.min(tMax, Math.max(t1, t2));

        return tMax >= Math.max(tMin, 0.0);
    }

    /**
     * Earliest non-negative ray parameter {@code t} along {@code origin + direction * t} where the ray enters {@code box},
     * or {@link Double#NaN} if there is no hit within {@code maxDist}.
     */
    public static double rayEnterDistanceOrNaN(Vec3 origin, Vec3 direction, double maxDist, AABB box) {
        double invX = direction.x == 0 ? Double.POSITIVE_INFINITY : 1.0 / direction.x;
        double invY = direction.y == 0 ? Double.POSITIVE_INFINITY : 1.0 / direction.y;
        double invZ = direction.z == 0 ? Double.POSITIVE_INFINITY : 1.0 / direction.z;

        double tMin = 0.0;
        double tMax = maxDist;

        double t1 = (box.minX - origin.x) * invX;
        double t2 = (box.maxX - origin.x) * invX;
        tMin = Math.max(tMin, Math.min(t1, t2));
        tMax = Math.min(tMax, Math.max(t1, t2));

        t1 = (box.minY - origin.y) * invY;
        t2 = (box.maxY - origin.y) * invY;
        tMin = Math.max(tMin, Math.min(t1, t2));
        tMax = Math.min(tMax, Math.max(t1, t2));

        t1 = (box.minZ - origin.z) * invZ;
        t2 = (box.maxZ - origin.z) * invZ;
        tMin = Math.max(tMin, Math.min(t1, t2));
        tMax = Math.min(tMax, Math.max(t1, t2));

        if (tMax < Math.max(tMin, 0.0)) {
            return Double.NaN;
        }
        double tHit = Math.max(tMin, 0.0);
        return tHit <= maxDist ? tHit : Double.NaN;
    }
}
