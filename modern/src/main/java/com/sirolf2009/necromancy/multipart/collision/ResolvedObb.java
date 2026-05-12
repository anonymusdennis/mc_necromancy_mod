package com.sirolf2009.necromancy.multipart.collision;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

/** Immutable world-space OBB for narrow-phase queries (debug + raycasts). */
public final class ResolvedObb {

    private final Vec3 centerWorld;
    private final Quaternionf orientationWorld;
    private final Vector3f halfExtents;

    public ResolvedObb(Vec3 centerWorld, Quaternionf orientationWorld, Vector3f halfExtents) {
        this.centerWorld = centerWorld;
        this.orientationWorld = new Quaternionf(Objects.requireNonNull(orientationWorld)).normalize();
        this.halfExtents = new Vector3f(Objects.requireNonNull(halfExtents)).max(new Vector3f(1e-4f, 1e-4f, 1e-4f));
    }

    public static ResolvedObb copyOf(ResolvedObb src) {
        if (src == null) {
            return new ResolvedObb(Vec3.ZERO, new Quaternionf(), new Vector3f(1e-4f, 1e-4f, 1e-4f));
        }
        return new ResolvedObb(src.centerWorld, src.orientationWorld, src.halfExtents);
    }

    public Vec3 centerWorld() {
        return centerWorld;
    }

    public void orientationInto(Quaternionf dest) {
        dest.set(orientationWorld);
    }

    public void halfExtentsInto(Vector3f dest) {
        dest.set(halfExtents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResolvedObb that)) return false;
        return centerWorld.equals(that.centerWorld)
            && orientationWorld.equals(that.orientationWorld, 1e-5f)
            && halfExtents.equals(that.halfExtents, 1e-5f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerWorld, orientationWorld.hashCode(), halfExtents.hashCode());
    }
}
