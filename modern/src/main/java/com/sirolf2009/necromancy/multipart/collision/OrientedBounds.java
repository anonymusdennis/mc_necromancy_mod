package com.sirolf2009.necromancy.multipart.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Broad-phase AABB from OBB + utilities. */
public final class OrientedBounds {

    private static final Vector3f AX = new Vector3f();
    private static final Vector3f AY = new Vector3f();
    private static final Vector3f AZ = new Vector3f();

    private OrientedBounds() {}

    /** Conservative axis-aligned bound enclosing an oriented box (rotation uses quaternion basis). */
    public static AABB expandingAabb(Vec3 centerWorld, Quaternionf orientationWorld, Vector3f halfExtents) {
        Matrix3f m = new Matrix3f().rotation(orientationWorld);
        m.getColumn(0, AX);
        m.getColumn(1, AY);
        m.getColumn(2, AZ);
        float ex = halfExtents.x * Math.abs(AX.x) + halfExtents.y * Math.abs(AY.x) + halfExtents.z * Math.abs(AZ.x);
        float ey = halfExtents.x * Math.abs(AX.y) + halfExtents.y * Math.abs(AY.y) + halfExtents.z * Math.abs(AZ.y);
        float ez = halfExtents.x * Math.abs(AX.z) + halfExtents.y * Math.abs(AY.z) + halfExtents.z * Math.abs(AZ.z);
        double cx = centerWorld.x;
        double cy = centerWorld.y;
        double cz = centerWorld.z;
        return new AABB(cx - ex, cy - ey, cz - ez, cx + ex, cy + ey, cz + ez);
    }
}
