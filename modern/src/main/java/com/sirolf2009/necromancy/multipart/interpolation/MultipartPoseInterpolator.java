package com.sirolf2009.necromancy.multipart.interpolation;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.math.TransformCompose;
import com.sirolf2009.necromancy.multipart.WorldPose;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

/**
 * Samples poses across ticks for render smoothing.
 *
 * <p><strong>Ownership:</strong> Authoritative poses live on {@link BodyPartNode} as immutable {@link WorldPose}
 * snapshots. Disabled smoothing returns those snapshots directly (safe — no mutable escape). Enabled smoothing allocates a
 * new {@link WorldPose} via {@link WorldPose#lerp} each sample (acceptable render-bandwidth cost).
 */
public final class MultipartPoseInterpolator {

    private MultipartPoseInterpolator() {}

    public static WorldPose partSimulationSmoothed(BodyPartNode node, float partialTick, boolean enabled) {
        if (!enabled) {
            return node.simulationWorldPose();
        }
        WorldPose prev = node.previousSimulationWorldPose();
        if (prev == null) {
            return node.simulationWorldPose();
        }
        return WorldPose.lerp(prev, node.simulationWorldPose(), partialTick);
    }

    /**
     * Lerps simulation poses and composed render overlays independently, then recomposes — compatible with layered animation
     * contributors captured in {@link BodyPartNode#composedRenderOverlayInto}.
     */
    public static WorldPose partRenderSmoothed(BodyPartNode node, float partialTick, boolean enabled) {
        if (!enabled) {
            return node.renderWorldPose();
        }
        WorldPose prev = node.previousSimulationWorldPose();
        if (prev == null) {
            return node.renderWorldPose();
        }
        WorldPose simSmoothed = WorldPose.lerp(prev, node.simulationWorldPose(), partialTick);

        PartTransform nextOv = new PartTransform();
        node.composedRenderOverlayInto(nextOv);
        PartTransform prevOv = new PartTransform();
        prevOv.setToIdentity();
        if (node.hasPreviousComposedRenderOverlay()) {
            node.previousComposedRenderOverlayInto(prevOv);
        }
        PartTransform lerpedOv = new PartTransform();
        PartTransform.lerpInto(prevOv, nextOv, partialTick, lerpedOv);
        return TransformCompose.compose(simSmoothed, lerpedOv);
    }
}
