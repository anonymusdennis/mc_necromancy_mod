package com.sirolf2009.necromancy.multipart.debug;

import com.sirolf2009.necromancy.multipart.WorldPose;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

/**
 * Enable with {@code -Dnecromancy.debugMultipartTransforms=true}. Catches accidental shared references between
 * simulation/render caches that must remain distinct instances even when numerically equal.
 */
public final class TransformAliasingAssertions {

    public static final boolean ENABLED = Boolean.getBoolean("necromancy.debugMultipartTransforms");

    private TransformAliasingAssertions() {}

    public static void assertDistinctSimulationRenderWorldPose(BodyPartNode node, String context) {
        if (!ENABLED || node == null) return;
        WorldPose sim = node.simulationWorldPose();
        WorldPose ren = node.renderWorldPose();
        if (sim != null && sim == ren) {
            throw new AssertionError(context + ": simulationWorldPose and renderWorldPose share reference — use separate immutable snapshots");
        }
    }

    public static void assertNotMutablyAliased(WorldPose a, WorldPose b, String context) {
        if (!ENABLED || a == null || b == null) return;
        if (a.mutableAlias(b)) {
            throw new AssertionError(context + ": unexpected shared WorldPose reference");
        }
    }
}
