package com.sirolf2009.necromancy.multipart.broadphase;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-hierarchy temporal coherence for multipart broad-phase — runs after pose propagation, before spatial-hash publish.
 */
public final class MultipartActivityGovernor {

    private final TransformHierarchy hierarchy;
    private boolean topologyPrimed;
    private int topologyRevisionCached;

    private static final class StablePose {
        Vec3 position;
        final Quaternionf orientation = new Quaternionf();

        StablePose(Vec3 position, Quaternionf orientIn) {
            this.position = position;
            this.orientation.set(orientIn).normalize();
        }

        void set(Vec3 position, Quaternionf orientIn) {
            this.position = position;
            this.orientation.set(orientIn).normalize();
        }
    }

    private final Map<ResourceLocation, StablePose> stableByPart = new HashMap<>();

    public MultipartActivityGovernor(TransformHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * Advances quiet timers / motion detection in deterministic post-order (deepest bones first so ancestor wakes propagate cleanly).
     */
    public void tick(RootMobEntity root) {
        TransformHierarchy tr = hierarchy;
        int topo = tr.topologyRevision();
        if (!topologyPrimed) {
            topologyPrimed = true;
            topologyRevisionCached = topo;
        } else if (topo != topologyRevisionCached) {
            resetTransientActivityState(tr);
            topologyRevisionCached = topo;
        }

        MultipartActivityConfig cfg = root.multipartActivityConfig();
        boolean dirty = false;
        Quaternionf qScratch = new Quaternionf();
        int rootEntityId = root.asMultipartRoot().getId();
        List<BodyPartNode> order = tr.collectNodesPostOrder();
        for (BodyPartNode node : order) {
            dirty |= step(rootEntityId, node, cfg, qScratch);
        }
        if (dirty) {
            tr.bumpBroadphaseActivitySerial();
        }
    }

    /**
     * Invoked after non-monotonic structural replays (e.g. memento rollback restoring an identical topology revision).
     */
    public void notifyHierarchyReplayed() {
        topologyPrimed = true;
        topologyRevisionCached = hierarchy.topologyRevision();
        stableByPart.clear();
        resetTransientActivityState(hierarchy);
    }

    private void resetTransientActivityState(TransformHierarchy tr) {
        stableByPart.clear();
        for (BodyPartNode n : tr.nodes()) {
            if (n.partActivityState().participatesInAutomaticTransitions()) {
                n.setPartActivityState(MultipartPartActivityState.ACTIVE);
            }
            n.resetActivityQuietTicks();
        }
        tr.bumpBroadphaseActivitySerial();
    }

    private boolean step(int multipartRootEntityId, BodyPartNode node, MultipartActivityConfig cfg, Quaternionf qScratch) {
        if (!node.attachedToParent()) {
            return false;
        }
        MultipartPartActivityState state = node.partActivityState();
        if (!state.participatesInAutomaticTransitions()) {
            return false;
        }

        Vec3 pos = node.simulationWorldPose().position();
        node.simulationWorldPose().orientationInto(qScratch);

        StablePose stable = stableByPart.get(node.id());
        if (stable == null) {
            stableByPart.put(node.id(), new StablePose(pos, qScratch));
            return false;
        }

        double dPosSq = pos.distanceToSqr(stable.position);
        float dot = Math.abs(stable.orientation.dot(qScratch));
        float orthoSq = 1f - dot * dot;
        boolean motion = dPosSq > cfg.translationEpsilonSq() || orthoSq > cfg.orientationOrthoEpsilonSq();

        boolean changed = false;
        if (motion) {
            stable.set(pos, qScratch);
            node.resetActivityQuietTicks();
            if (state != MultipartPartActivityState.ACTIVE) {
                if (state == MultipartPartActivityState.SLEEPING) {
                    MultipartTelemetry.recordPartWakeTransition(multipartRootEntityId);
                }
                node.setPartActivityState(MultipartPartActivityState.ACTIVE);
                changed = true;
            }
            changed |= wakeSleepingAncestors(multipartRootEntityId, node);
        } else {
            node.incrementActivityQuietTicks();
            int q = node.activityQuietTicks();
            if (state == MultipartPartActivityState.ACTIVE && q >= cfg.ticksQuietBeforeIdle()) {
                node.setPartActivityState(MultipartPartActivityState.IDLE);
                node.resetActivityQuietTicks();
                changed = true;
            } else if (state == MultipartPartActivityState.IDLE && q >= cfg.ticksQuietBeforeSleep()) {
                node.setPartActivityState(MultipartPartActivityState.SLEEPING);
                MultipartTelemetry.recordPartSleepTransition(multipartRootEntityId);
                node.resetActivityQuietTicks();
                changed = true;
            }
        }
        return changed;
    }

    private boolean wakeSleepingAncestors(int multipartRootEntityId, BodyPartNode node) {
        ResourceLocation pid = node.parentId();
        boolean dirty = false;
        while (pid != null) {
            BodyPartNode parent = hierarchy.get(pid);
            if (parent == null) {
                break;
            }
            if (parent.partActivityState() == MultipartPartActivityState.SLEEPING) {
                MultipartTelemetry.recordPartWakeTransition(multipartRootEntityId);
                parent.setPartActivityState(MultipartPartActivityState.IDLE);
                parent.resetActivityQuietTicks();
                dirty = true;
            }
            pid = parent.parentId();
        }
        return dirty;
    }
}
