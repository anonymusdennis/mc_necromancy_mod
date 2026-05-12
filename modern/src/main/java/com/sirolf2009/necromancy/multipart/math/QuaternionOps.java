package com.sirolf2009.necromancy.multipart.math;

import org.joml.Quaternionf;

/** Euler ↔ quaternion helpers for multipart rigs (degrees). */
public final class QuaternionOps {

    private QuaternionOps() {}

    /** Order: yaw(Y) → pitch(X) → roll(Z), degrees — matches common mob facing + limb tilt. */
    public static Quaternionf fromYawPitchRollDegrees(float yawDeg, float pitchDeg, float rollDeg) {
        float yr = (float) Math.toRadians(yawDeg);
        float pr = (float) Math.toRadians(pitchDeg);
        float rr = (float) Math.toRadians(rollDeg);
        return new Quaternionf().rotateYXZ(yr, pr, rr);
    }

    public static Quaternionf fromYawDegrees(float yawDeg) {
        return new Quaternionf().rotateY((float) Math.toRadians(yawDeg));
    }

    /** Approximate yaw (degrees) from quaternion for legacy yaw-only APIs (horizontal facing). */
    public static float yawDegreesHorizontal(Quaternionf q) {
        org.joml.Vector3f forward = new org.joml.Vector3f(0f, 0f, 1f);
        q.transform(forward);
        return (float) Math.toDegrees(Math.atan2(-forward.x, forward.z));
    }
}
