package com.sirolf2009.necromancy.multipart.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Oriented volume in part-local space. Immutable; no live references escape {@link #localRotationInto}/{@link #halfExtentsInto}.
 */
public final class OrientedLocalVolume {

    private final Vec3 centerOffset;
    private final Vector3f halfExtents;
    private final Quaternionf localRotation;

    public OrientedLocalVolume(Vec3 centerOffset, Vector3f halfExtents, Quaternionf localRotation) {
        this.centerOffset = centerOffset;
        this.halfExtents = new Vector3f(Objects.requireNonNull(halfExtents));
        this.halfExtents.x = Math.max(1e-4f, Math.abs(this.halfExtents.x));
        this.halfExtents.y = Math.max(1e-4f, Math.abs(this.halfExtents.y));
        this.halfExtents.z = Math.max(1e-4f, Math.abs(this.halfExtents.z));
        this.localRotation = new Quaternionf(Objects.requireNonNull(localRotation)).normalize();
    }

    public static OrientedLocalVolume fromAxisAlignedBox(AABB localAabb) {
        Vec3 c = localAabb.getCenter();
        double hx = (localAabb.maxX - localAabb.minX) * 0.5;
        double hy = (localAabb.maxY - localAabb.minY) * 0.5;
        double hz = (localAabb.maxZ - localAabb.minZ) * 0.5;
        return new OrientedLocalVolume(c, new Vector3f((float) hx, (float) hy, (float) hz), new Quaternionf());
    }

    public Vec3 centerOffset() {
        return centerOffset;
    }

    public void halfExtentsInto(Vector3f dest) {
        dest.set(halfExtents);
    }

    public void localRotationInto(Quaternionf dest) {
        dest.set(localRotation);
    }

    public AABB conservativeLocalAabb() {
        Quaternionf q = new Quaternionf();
        localRotationInto(q);
        Vector3f he = new Vector3f();
        halfExtentsInto(he);
        return OrientedBounds.expandingAabb(centerOffset, q, he);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrientedLocalVolume that)) return false;
        return centerOffset.equals(that.centerOffset)
            && halfExtents.equals(that.halfExtents, 1e-5f)
            && localRotation.equals(that.localRotation, 1e-5f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerOffset, halfExtents.hashCode(), localRotation.hashCode());
    }
}
