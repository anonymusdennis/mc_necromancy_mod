package com.sirolf2009.necromancy.multipart.animation;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * One weighted contributor to the render overlay stack for a single bodypart. Lower {@link #priority} sorts earlier
 * within the same {@link RenderLayerPhase}.
 *
 * @param weight blend strength in [0, 1]; interpreted per phase in {@link PartTransformLayerBlend}.
 * @param blendMask which translation axes, rotation (Euler-masked or whole quaternion), and scale axes participate.
 * @param rotationStyle quaternion multiplication order versus accumulated overlay rotation ({@link RotationBlendStyle}).
 */
public record WeightedPartTransform(
    ResourceLocation contributorId,
    RenderLayerPhase phase,
    int priority,
    float weight,
    PartTransform localDelta,
    TransformBlendMask blendMask,
    RotationBlendStyle rotationStyle
) {
    public WeightedPartTransform(ResourceLocation contributorId, RenderLayerPhase phase, int priority, float weight,
                                 PartTransform localDelta) {
        this(contributorId, phase, priority, weight, localDelta, TransformBlendMask.FULL, RotationBlendStyle.POST_MULTIPLY);
    }

    public WeightedPartTransform {
        Objects.requireNonNull(contributorId, "contributorId");
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(localDelta, "localDelta");
        Objects.requireNonNull(blendMask, "blendMask");
        Objects.requireNonNull(rotationStyle, "rotationStyle");
        localDelta = localDelta.copy();
        weight = Math.max(0f, Math.min(1f, weight));
    }
}
