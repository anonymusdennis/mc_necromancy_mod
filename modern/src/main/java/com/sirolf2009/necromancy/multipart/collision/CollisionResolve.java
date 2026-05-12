package com.sirolf2009.necromancy.multipart.collision;

import com.sirolf2009.necromancy.multipart.WorldPose;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Combines part simulation pose with hit volume expressed in part-local space. */
public final class CollisionResolve {

    private CollisionResolve() {}

    public static ResolvedObb resolve(WorldPose partSimPose, OrientedLocalVolume local) {
        Vector3f scratch = new Vector3f((float) local.centerOffset().x, (float) local.centerOffset().y, (float) local.centerOffset().z);

        Vector3f scale = new Vector3f();
        partSimPose.scaleInto(scale);
        scratch.mul(scale);

        Quaternionf orient = new Quaternionf();
        partSimPose.orientationInto(orient);
        orient.transform(scratch);

        Vec3 centerWorld = partSimPose.position().add(scratch.x, scratch.y, scratch.z);

        Quaternionf worldRot = new Quaternionf();
        partSimPose.orientationInto(worldRot);
        Quaternionf localR = new Quaternionf();
        local.localRotationInto(localR);
        worldRot.mul(localR, worldRot).normalize();

        Vector3f he = new Vector3f();
        local.halfExtentsInto(he);
        he.mul(scale);
        he.absolute();

        return new ResolvedObb(centerWorld, worldRot, he);
    }

    public static boolean containsWorldPoint(Vec3 worldPoint, ResolvedObb obb) {
        Quaternionf inv = new Quaternionf();
        obb.orientationInto(inv);
        inv.conjugate();

        Vector3f scratch = new Vector3f(
            (float) (worldPoint.x - obb.centerWorld().x),
            (float) (worldPoint.y - obb.centerWorld().y),
            (float) (worldPoint.z - obb.centerWorld().z));
        inv.transform(scratch);

        Vector3f h = new Vector3f();
        obb.halfExtentsInto(h);
        return Math.abs(scratch.x) <= h.x + 1e-6f
            && Math.abs(scratch.y) <= h.y + 1e-6f
            && Math.abs(scratch.z) <= h.z + 1e-6f;
    }
}
