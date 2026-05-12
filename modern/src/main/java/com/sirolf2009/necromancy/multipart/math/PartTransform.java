package com.sirolf2009.necromancy.multipart.math;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Mutable TRS relative to a parent bone.
 *
 * <p><strong>Ownership:</strong> Each {@link com.sirolf2009.necromancy.multipart.part.BodyPartNode} owns its
 * {@link PartTransform} instances exclusively; do not retain references returned from {@link com.sirolf2009.necromancy.multipart.part.BodyPartNode#simulationLocalTransform()}
 * across ticks unless that node still owns them. External systems should read via {@code into(dest)} or {@link #copy()}.
 */
public final class PartTransform {

    private final Vector3f translation = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Vector3f scale = new Vector3f(1f, 1f, 1f);

    public PartTransform() {
        rotation.identity();
    }

    public PartTransform(Vec3 translation, Quaternionf rotation, Vector3f scale) {
        Vecs.copyFromVec3(this.translation, translation);
        setRotation(rotation);
        setScale(scale);
    }

    public static PartTransform identity() {
        return new PartTransform();
    }

    public static PartTransform fromTranslationEulerDegrees(Vec3 translation, float yawDeg, float pitchDeg, float rollDeg) {
        Quaternionf q = QuaternionOps.fromYawPitchRollDegrees(yawDeg, pitchDeg, rollDeg);
        PartTransform t = new PartTransform();
        Vecs.copyFromVec3(t.translation, translation);
        t.setRotation(q);
        return t;
    }

    public static PartTransform fromTranslationYawDegrees(Vec3 translation, float yawDegrees) {
        return fromTranslationEulerDegrees(translation, yawDegrees, 0f, 0f);
    }

    public void translationInto(Vector3f dest) {
        dest.set(translation);
    }

    public Vec3 translation() {
        return new Vec3(translation.x, translation.y, translation.z);
    }

    public void setTranslation(Vec3 t) {
        Vecs.copyFromVec3(translation, t);
    }

    public void setTranslation(Vector3f t) {
        translation.set(t);
    }

    public void rotationInto(Quaternionf dest) {
        dest.set(rotation);
    }

    public void setRotation(Quaternionf q) {
        rotation.set(q).normalize();
    }

    public void scaleInto(Vector3f dest) {
        dest.set(scale);
    }

    public void setScale(Vector3f s) {
        scale.set(Math.max(1e-4f, s.x), Math.max(1e-4f, s.y), Math.max(1e-4f, s.z));
    }

    public void setUniformScale(float s) {
        float v = Math.max(1e-4f, s);
        scale.set(v, v, v);
    }

    public PartTransform copy() {
        Quaternionf q = new Quaternionf();
        rotationInto(q);
        Vector3f s = new Vector3f();
        scaleInto(s);
        return new PartTransform(translation(), q, s);
    }

    public void set(PartTransform other) {
        other.translationInto(translation);
        Quaternionf q = new Quaternionf();
        other.rotationInto(q);
        rotation.set(q);
        Vector3f s = new Vector3f();
        other.scaleInto(s);
        scale.set(s);
    }

    /** Resets to identity TRS (bone-local neutral pose). */
    public void setToIdentity() {
        translation.set(0f, 0f, 0f);
        rotation.identity();
        scale.set(1f, 1f, 1f);
    }

    /**
     * Linear blend of translation / scale; spherical interpolation of rotation. {@code t} is clamped to [0, 1].
     */
    public static void lerpInto(PartTransform a, PartTransform b, float t, PartTransform dest) {
        float u = Math.max(0f, Math.min(1f, t));
        Vector3f ta = new Vector3f();
        Vector3f tb = new Vector3f();
        a.translationInto(ta);
        b.translationInto(tb);
        ta.lerp(tb, u);
        dest.setTranslation(ta);

        Quaternionf qa = new Quaternionf();
        Quaternionf qb = new Quaternionf();
        a.rotationInto(qa);
        b.rotationInto(qb);
        qa.slerp(qb, u).normalize();
        dest.setRotation(qa);

        Vector3f sa = new Vector3f();
        Vector3f sb = new Vector3f();
        a.scaleInto(sa);
        b.scaleInto(sb);
        sa.lerp(sb, u);
        dest.setScale(sa);
    }

    public static PartTransform lerp(PartTransform a, PartTransform b, float t) {
        PartTransform out = new PartTransform();
        lerpInto(a, b, t, out);
        return out;
    }
}
