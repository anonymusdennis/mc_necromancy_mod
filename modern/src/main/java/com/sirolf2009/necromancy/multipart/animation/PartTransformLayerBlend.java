package com.sirolf2009.necromancy.multipart.animation;

import com.sirolf2009.necromancy.multipart.WorldPose;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.math.TransformCompose;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Collapses weighted layers into a single bone-local {@link PartTransform} for composition onto simulation poses.
 *
 * <p><strong>Semantics:</strong>
 * <ul>
 *   <li>{@link RenderLayerPhase#PROCEDURAL}, {@link RenderLayerPhase#ADDITIVE_OVERLAY}, {@link RenderLayerPhase#TEMPORARY_EFFECT} —
 *       incremental overlays; each channel respects {@link TransformBlendMask} (per-axis translation/scale, rotation via whole
 *       quaternion or masked YXZ Euler components). Rotation composition order selects {@link RotationBlendStyle}.</li>
 *   <li>{@link RenderLayerPhase#IK_OVERRIDE} — blends accumulated TRS toward the layer's {@code localDelta} as an absolute local-space
 *       target; masks restrict which axes / Euler channels interpolate.</li>
 * </ul>
 *
 * <p>For masked Euler rotation, components follow {@link org.joml.Quaternionf#getEulerAnglesYXZ(org.joml.Vector3f)} /
 * {@link org.joml.Quaternionf#rotationYXZ(float, float, float)} — see {@link TransformBlendMask}.
 */
public final class PartTransformLayerBlend {

    private PartTransformLayerBlend() {
    }

    public static void blendInto(List<WeightedPartTransform> sortedLayers, PartTransform dest) {
        dest.setToIdentity();
        if (sortedLayers.isEmpty()) {
            return;
        }

        Vector3f tAcc = new Vector3f();
        Quaternionf qAcc = new Quaternionf().identity();
        Vector3f sAcc = new Vector3f(1f, 1f, 1f);

        Vector3f scratchT = new Vector3f();
        Quaternionf scratchQ = new Quaternionf();
        Quaternionf qId = new Quaternionf().identity();
        Quaternionf step = new Quaternionf();
        Quaternionf qApply = new Quaternionf();
        Quaternionf qTmpMul = new Quaternionf();
        Quaternionf qPoseIk = new Quaternionf();
        Quaternionf qTargetIk = new Quaternionf();
        Vector3f scratchS = new Vector3f();
        Vector3f one = new Vector3f(1f, 1f, 1f);
        Vector3f eulerScratch = new Vector3f();
        Vector3f eulerPoseIk = new Vector3f();
        Vector3f eulerTargetIk = new Vector3f();
        Vector3f eulerMixIk = new Vector3f();
        Vector3f poseScaleIk = new Vector3f();
        Vector3f targetScaleIk = new Vector3f();
        Vector3f scaleOutIk = new Vector3f();

        PartTransform ikScratch = new PartTransform();

        for (WeightedPartTransform w : sortedLayers) {
            float wt = w.weight();
            if (wt <= 1e-6f) {
                continue;
            }
            TransformBlendMask m = w.blendMask();
            PartTransform d = w.localDelta();

            switch (w.phase()) {
                case PROCEDURAL, ADDITIVE_OVERLAY, TEMPORARY_EFFECT -> applyIncrementalOverlay(
                    tAcc, qAcc, sAcc, d, wt, m, w.rotationStyle(),
                    scratchT, scratchQ, qId, step, qApply, qTmpMul, scratchS, one, eulerScratch);
                case IK_OVERRIDE -> {
                    ikScratch.setTranslation(new Vec3(tAcc.x, tAcc.y, tAcc.z));
                    ikScratch.setRotation(qAcc);
                    ikScratch.setScale(sAcc);
                    applyIkTowardMasked(ikScratch, d, wt, m,
                        eulerPoseIk, eulerTargetIk, eulerMixIk,
                        qPoseIk, qTargetIk, poseScaleIk, targetScaleIk, scaleOutIk);
                    Vec3 p = ikScratch.translation();
                    tAcc.set((float) p.x, (float) p.y, (float) p.z);
                    ikScratch.rotationInto(qAcc);
                    ikScratch.scaleInto(sAcc);
                }
            }
        }

        dest.setTranslation(new Vec3(tAcc.x, tAcc.y, tAcc.z));
        dest.setRotation(qAcc);
        dest.setScale(sAcc);
    }

    private static void applyIncrementalOverlay(Vector3f tAcc, Quaternionf qAcc, Vector3f sAcc,
                                                PartTransform d, float w, TransformBlendMask mask,
                                                RotationBlendStyle rotationStyle,
                                                Vector3f scratchT, Quaternionf scratchQ,
                                                Quaternionf qId, Quaternionf step, Quaternionf qApply,
                                                Quaternionf qTmpMul, Vector3f scratchS, Vector3f one,
                                                Vector3f eulerScratch) {
        if (mask.affectsTranslation()) {
            d.translationInto(scratchT);
            maskTranslationAxes(scratchT, mask.translationAxesMask());
            tAcc.fma(w, scratchT);
        }

        if (mask.rotationActive()) {
            d.rotationInto(scratchQ);
            mask.filterRotationDelta(scratchQ, eulerScratch, qApply);
            step.set(qId).slerp(qApply, w).normalize();
            if (rotationStyle == RotationBlendStyle.PRE_MULTIPLY) {
                qTmpMul.set(step).mul(qAcc);
                qAcc.set(qTmpMul).normalize();
            } else {
                qAcc.mul(step).normalize();
            }
        }

        if (mask.affectsScale()) {
            d.scaleInto(scratchS);
            scratchS.lerp(one, 1f - w);
            maskScaleAxesNeutral(scratchS, mask.scaleAxesMask());
            sAcc.mul(scratchS);
        }
    }

    private static void maskTranslationAxes(Vector3f t, int axesMask) {
        if ((axesMask & TransformBlendMask.AXIS_X) == 0) {
            t.x = 0f;
        }
        if ((axesMask & TransformBlendMask.AXIS_Y) == 0) {
            t.y = 0f;
        }
        if ((axesMask & TransformBlendMask.AXIS_Z) == 0) {
            t.z = 0f;
        }
    }

    /** Axes absent from the mask contribute identity scale ({@code 1}) so {@code sAcc} is unchanged on those axes. */
    private static void maskScaleAxesNeutral(Vector3f lerpedScale, int axesMask) {
        if ((axesMask & TransformBlendMask.AXIS_X) == 0) {
            lerpedScale.x = 1f;
        }
        if ((axesMask & TransformBlendMask.AXIS_Y) == 0) {
            lerpedScale.y = 1f;
        }
        if ((axesMask & TransformBlendMask.AXIS_Z) == 0) {
            lerpedScale.z = 1f;
        }
    }

    private static void applyIkTowardMasked(PartTransform pose, PartTransform target, float w,
                                            TransformBlendMask mask,
                                            Vector3f eulerPose, Vector3f eulerTarget, Vector3f eulerMix,
                                            Quaternionf qPose, Quaternionf qTarget,
                                            Vector3f poseScale, Vector3f targetScale, Vector3f scaleOut) {
        Vec3 pt = pose.translation();
        Vec3 tt = target.translation();
        double px = pt.x;
        double py = pt.y;
        double pz = pt.z;
        int tm = mask.translationAxesMask();
        if ((tm & TransformBlendMask.AXIS_X) != 0) {
            px = pt.x + (tt.x - pt.x) * w;
        }
        if ((tm & TransformBlendMask.AXIS_Y) != 0) {
            py = pt.y + (tt.y - pt.y) * w;
        }
        if ((tm & TransformBlendMask.AXIS_Z) != 0) {
            pz = pt.z + (tt.z - pt.z) * w;
        }
        pose.setTranslation(new Vec3(px, py, pz));

        pose.rotationInto(qPose);
        target.rotationInto(qTarget);
        if (mask.rotationActive()) {
            if (mask.rotationAsWholeQuaternion()) {
                qPose.slerp(qTarget, w).normalize();
                pose.setRotation(qPose);
            } else {
                qPose.getEulerAnglesYXZ(eulerPose);
                qTarget.getEulerAnglesYXZ(eulerTarget);
                int rm = mask.rotationEulerMask();
                eulerMix.x = ((rm & TransformBlendMask.ROT_PITCH) != 0)
                    ? eulerPose.x + (eulerTarget.x - eulerPose.x) * w : eulerPose.x;
                eulerMix.y = ((rm & TransformBlendMask.ROT_YAW) != 0)
                    ? eulerPose.y + (eulerTarget.y - eulerPose.y) * w : eulerPose.y;
                eulerMix.z = ((rm & TransformBlendMask.ROT_ROLL) != 0)
                    ? eulerPose.z + (eulerTarget.z - eulerPose.z) * w : eulerPose.z;
                qPose.rotationYXZ(eulerMix.y, eulerMix.x, eulerMix.z).normalize();
                pose.setRotation(qPose);
            }
        }

        pose.scaleInto(poseScale);
        target.scaleInto(targetScale);
        int sm = mask.scaleAxesMask();
        scaleOut.x = ((sm & TransformBlendMask.AXIS_X) != 0)
            ? poseScale.x + (targetScale.x - poseScale.x) * w : poseScale.x;
        scaleOut.y = ((sm & TransformBlendMask.AXIS_Y) != 0)
            ? poseScale.y + (targetScale.y - poseScale.y) * w : poseScale.y;
        scaleOut.z = ((sm & TransformBlendMask.AXIS_Z) != 0)
            ? poseScale.z + (targetScale.z - poseScale.z) * w : poseScale.z;
        pose.setScale(scaleOut);
    }

    /** Composes editor/cosmetic overlay after systemic layers (matches legacy single-overlay ordering). */
    public static void mergeEditorOverlayInto(PartTransform layeredOverlay, PartTransform editorOverlay, PartTransform dest,
                                              WorldPose.Mutable scratchA,
                                              WorldPose.Mutable scratchB) {
        TransformCompose.composeInto(WorldPose.identity(), layeredOverlay, scratchA);
        TransformCompose.composeInto(scratchA.toImmutable(), editorOverlay, scratchB);
        WorldPose wf = scratchB.toImmutable();
        dest.setTranslation(wf.position());
        dest.setRotation(wf.orientationCopy());
        dest.setScale(wf.scaleCopy());
    }
}
