package com.sirolf2009.necromancy.multipart.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Axis-level inclusion mask for layered bone-local overlays ({@link WeightedPartTransform}).
 *
 * <p>Translation and scale use Cartesian axis bits. Rotation uses either the full quaternion from the layer delta or,
 * when {@link #rotationAsWholeQuaternion()} is {@code false}, local Euler components in <strong>YXZ</strong> order
 * matching {@link com.sirolf2009.necromancy.multipart.math.QuaternionOps#fromYawPitchRollDegrees} /
 * {@link org.joml.Quaternionf#rotationYXZ(float, float, float)} (yaw → pitch → roll as stored in Euler triple).
 *
 * <p>Inactive channels skip accumulation for that contribution — procedural locomotion can drive translation while a recoil
 * layer touches only pitch Euler, and IK can override only feet translation without disturbing spine twist.
 */
public record TransformBlendMask(
    int translationAxesMask,
    int rotationEulerMask,
    boolean rotationActive,
    boolean rotationAsWholeQuaternion,
    int scaleAxesMask
) {

    /** Translation X / scale X / local X Euler component (pitch channel in YXZ triple). */
    public static final int AXIS_X = 1;
    public static final int AXIS_Y = 2;
    public static final int AXIS_Z = 4;

    /** Rotation around Y (yaw component of YXZ Euler triple). */
    public static final int ROT_YAW = 1;
    /** Rotation around X (pitch component). */
    public static final int ROT_PITCH = 2;
    /** Rotation around Z (roll component). */
    public static final int ROT_ROLL = 4;

    public static final TransformBlendMask FULL = new TransformBlendMask(
        AXIS_X | AXIS_Y | AXIS_Z,
        ROT_YAW | ROT_PITCH | ROT_ROLL,
        true,
        true,
        AXIS_X | AXIS_Y | AXIS_Z);

    /** Nothing from TRS (no-op contributor — rare; prefer omitting the layer). */
    public static final TransformBlendMask NONE = new TransformBlendMask(0, 0, false, true, 0);

    public TransformBlendMask {
        translationAxesMask = translationAxesMask & (AXIS_X | AXIS_Y | AXIS_Z);
        rotationEulerMask = rotationEulerMask & (ROT_YAW | ROT_PITCH | ROT_ROLL);
        scaleAxesMask = scaleAxesMask & (AXIS_X | AXIS_Y | AXIS_Z);
    }

    /** Translation-only full axes; rotation and scale channels disabled. */
    public static TransformBlendMask translationOnly(int axesMask) {
        return new TransformBlendMask(axesMask & (AXIS_X | AXIS_Y | AXIS_Z), 0, false, true, 0);
    }

    /** Rotation-only using masked Euler components (not whole quaternion). */
    public static TransformBlendMask rotationEulerOnly(int eulerMask) {
        int em = eulerMask & (ROT_YAW | ROT_PITCH | ROT_ROLL);
        return new TransformBlendMask(0, em, true, false, 0);
    }

    /** Rotation-only using the layer quaternion as a single delta (standard clip-style overlay). */
    public static TransformBlendMask rotationWholeQuaternionOnly() {
        return new TransformBlendMask(0, ROT_YAW | ROT_PITCH | ROT_ROLL, true, true, 0);
    }

    public boolean affectsTranslation() {
        return translationAxesMask != 0;
    }

    public boolean affectsScale() {
        return scaleAxesMask != 0;
    }

    /**
     * Combines channel masks for clip-graph style authoring — per-axis translation/scale and optional Euler-masked or
     * whole-quaternion rotation.
     */
    public static TransformBlendMask channels(int translationAxes, int rotationEuler, boolean rotationOn,
                                              boolean wholeQuaternionRotation, int scaleAxes) {
        return new TransformBlendMask(translationAxes, rotationEuler, rotationOn, wholeQuaternionRotation, scaleAxes);
    }

    /**
     * Copies {@code qIn} into {@code dest} respecting Euler-component masking; identity when rotation inactive.
     * Whole-quaternion mode copies normalized {@code qIn}.
     */
    public void filterRotationDelta(Quaternionf qIn, Vector3f eulerScratch, Quaternionf dest) {
        if (!rotationActive) {
            dest.identity();
            return;
        }
        if (rotationAsWholeQuaternion) {
            dest.set(qIn).normalize();
            return;
        }
        qIn.getEulerAnglesYXZ(eulerScratch);
        if ((rotationEulerMask & ROT_YAW) == 0) {
            eulerScratch.y = 0f;
        }
        if ((rotationEulerMask & ROT_PITCH) == 0) {
            eulerScratch.x = 0f;
        }
        if ((rotationEulerMask & ROT_ROLL) == 0) {
            eulerScratch.z = 0f;
        }
        dest.rotationYXZ(eulerScratch.y, eulerScratch.x, eulerScratch.z).normalize();
    }
}
