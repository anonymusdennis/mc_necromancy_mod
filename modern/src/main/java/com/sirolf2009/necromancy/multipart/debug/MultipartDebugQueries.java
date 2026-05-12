package com.sirolf2009.necromancy.multipart.debug;

import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.collision.ResolvedObb;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Supplies oriented simulation volumes for client debug overlays (wireframe drawers consume {@link ResolvedObb}).
 * <p>For multipart <strong>broad-phase</strong> inspection use {@link com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseSnapshot}
 * / {@link com.sirolf2009.necromancy.multipart.broadphase.MultipartWorldReadFrame} so queries never race simulation writes.
 * Per-part <strong>render interpolation</strong> continues to use {@link BodyPartNode#setPreviousSimulationWorldPose}
 * when {@link com.sirolf2009.necromancy.multipart.RootMobEntity#multipartPoseInterpolationCapture()} is enabled.
 */
public final class MultipartDebugQueries {

    private MultipartDebugQueries() {}

    public static List<ResolvedObb> simulationObbs(TransformHierarchy hierarchy) {
        List<ResolvedObb> out = new ArrayList<>();
        for (BodyPartNode n : hierarchy.nodes()) {
            if (!n.hitbox().collisionEnabled() || !n.attachedToParent()) continue;
            out.add(n.simulationCollisionObb());
        }
        return out;
    }
}
