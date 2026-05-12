package com.sirolf2009.necromancy.multipart;

import com.sirolf2009.necromancy.multipart.math.QuaternionOps;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Immutable world-space rigid pose (simulation or render snapshot).
 *
 * <p><strong>Ownership:</strong> {@link org.joml.Quaternionf} / {@link org.joml.Vector3f} are never exposed by reference.
 * Use {@link #orientationInto(Quaternionf)}, {@link #scaleInto(Vector3f)}, or {@link #copyOf(WorldPose)} when handing
 * poses to external APIs that might retain or mutate arguments.
 */
public final class WorldPose {

    private final Vec3 position;
    private final Quaternionf orientation;
    private final Vector3f scale;

    public WorldPose(Vec3 position, Quaternionf orientation, Vector3f scale) {
        this.position = position;
        this.orientation = new Quaternionf(Objects.requireNonNull(orientation)).normalize();
        this.scale = new Vector3f(Objects.requireNonNull(scale));
    }

    /**
     * Deep copy for ingest into caches ({@link com.sirolf2009.necromancy.multipart.part.BodyPartNode}) or network decode paths
     * so caller-held quaternions cannot mutate authoritative snapshots.
     */
    public static WorldPose copyOf(WorldPose src) {
        if (src == null) {
            return identity();
        }
        return new WorldPose(src.position, src.orientation, src.scale);
    }

    public static WorldPose identity() {
        return new WorldPose(Vec3.ZERO, new Quaternionf(), new Vector3f(1f, 1f, 1f));
    }

    public static WorldPose root(Vec3 pivot, Quaternionf rootOrientation, Vector3f rootScale) {
        return new WorldPose(pivot, rootOrientation, rootScale);
    }

    public static WorldPose rootUniformScale(Vec3 pivot, Quaternionf rootOrientation, float uniformScale) {
        float s = Math.max(1e-4f, uniformScale);
        return new WorldPose(pivot, rootOrientation, new Vector3f(s, s, s));
    }

    public static WorldPose rootYawPitchRollDegrees(Vec3 pivot, float yawDeg, float pitchDeg, float rollDeg) {
        return new WorldPose(pivot, QuaternionOps.fromYawPitchRollDegrees(yawDeg, pitchDeg, rollDeg), new Vector3f(1f, 1f, 1f));
    }

    public static WorldPose root(Vec3 entityPivot, float entityYawDegrees) {
        return rootYawPitchRollDegrees(entityPivot, entityYawDegrees, 0f, 0f);
    }

    /** Writes composed pose into {@code dest} without allocating a {@link WorldPose} (thread-safe if {@code dest} is local). */
    public static void lerpInto(WorldPose a, WorldPose b, float t, WorldPose.Mutable dest) {
        float clamped = Math.max(0f, Math.min(1f, t));
        Vec3 p = a.position.lerp(b.position, clamped);
        Quaternionf qa = new Quaternionf();
        Quaternionf qb = new Quaternionf();
        a.orientationInto(qa);
        b.orientationInto(qb);
        qa.slerp(qb, clamped).normalize();
        Vector3f sa = new Vector3f();
        Vector3f sb = new Vector3f();
        a.scaleInto(sa);
        b.scaleInto(sb);
        sa.lerp(sb, clamped);
        dest.set(p, qa, sa);
    }

    /** Allocating lerp for call sites that need an immutable snapshot. */
    public static WorldPose lerp(WorldPose a, WorldPose b, float t) {
        Mutable m = new Mutable();
        lerpInto(a, b, t, m);
        return m.toImmutable();
    }

    public Vec3 position() {
        return position;
    }

    public void orientationInto(Quaternionf dest) {
        dest.set(orientation);
    }

    public void scaleInto(Vector3f dest) {
        dest.set(scale);
    }

    public Quaternionf orientationCopy() {
        return new Quaternionf(orientation);
    }

    public Vector3f scaleCopy() {
        return new Vector3f(scale);
    }

    /** True if {@code other} is the same instance or shares any mutable backing (should never occur for valid immutable snapshots). */
    public boolean mutableAlias(WorldPose other) {
        return other != null && this == other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorldPose worldPose)) return false;
        return position.equals(worldPose.position)
            && orientation.equals(worldPose.orientation, 1e-5f)
            && scale.equals(worldPose.scale, 1e-5f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, orientation.hashCode(), scale.hashCode());
    }

    /** Mutable accumulation buffer for composition / interpolation; not thread-safe — keep per-thread / stack-local. */
    public static final class Mutable {
        private Vec3 position = Vec3.ZERO;
        private final Quaternionf orientation = new Quaternionf();
        private final Vector3f scale = new Vector3f(1f, 1f, 1f);

        public void set(Vec3 position, Quaternionf orientation, Vector3f scale) {
            this.position = position;
            this.orientation.set(orientation).normalize();
            this.scale.set(scale);
        }

        public WorldPose toImmutable() {
            return new WorldPose(position, orientation, scale);
        }

        public void orientationInto(Quaternionf dest) {
            dest.set(orientation);
        }

        public void scaleInto(Vector3f dest) {
            dest.set(scale);
        }

        public Vec3 position() {
            return position;
        }
    }
}
