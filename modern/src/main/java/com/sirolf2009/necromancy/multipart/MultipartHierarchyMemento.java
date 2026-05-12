package com.sirolf2009.necromancy.multipart;

import com.sirolf2009.necromancy.multipart.part.AttachmentPoint;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Authoritative structural snapshot of a {@link TransformHierarchy} for transaction rollback / cancel flows.
 *
 * <p>Captured skeleton clones omit transient propagation caches (world poses, broad-phase boxes); the next
 * {@link TransformHierarchy#tick} recomputes them.
 */
public final class MultipartHierarchyMemento {

    private final List<ResourceLocation> rootIds;
    private final Map<ResourceLocation, BodyPartNode> prototypes;
    private final int topologyRevision;
    private final long transformDirtyRevision;
    private final long hierarchyCommittedRevision;
    private final long propagationSerial;

    private MultipartHierarchyMemento(List<ResourceLocation> rootIds, Map<ResourceLocation, BodyPartNode> prototypes,
                                     int topologyRevision, long transformDirtyRevision,
                                     long hierarchyCommittedRevision, long propagationSerial) {
        this.rootIds = List.copyOf(rootIds);
        this.prototypes = Map.copyOf(prototypes);
        this.topologyRevision = topologyRevision;
        this.transformDirtyRevision = transformDirtyRevision;
        this.hierarchyCommittedRevision = hierarchyCommittedRevision;
        this.propagationSerial = propagationSerial;
    }

    /**
     * Captures disconnected prototypes suitable for replay via {@link BodyPartNode#copySkeleton()}.
     */
    public static MultipartHierarchyMemento capture(TransformHierarchy hierarchy) {
        Objects.requireNonNull(hierarchy, "hierarchy");
        List<ResourceLocation> roots = new ArrayList<>(hierarchy.rootIdsView());
        Map<ResourceLocation, BodyPartNode> protos = new HashMap<>();
        for (BodyPartNode n : hierarchy.nodes()) {
            protos.put(n.id(), n.copySkeleton());
        }
        return new MultipartHierarchyMemento(roots, protos,
            hierarchy.topologyRevision(),
            hierarchy.transformDirtyRevision(),
            hierarchy.hierarchyCommittedRevision(),
            hierarchy.propagationSerial());
    }

    /** Replays this snapshot onto {@code hierarchy}, overwriting revision telemetry with captured counters. */
    public void applyTo(TransformHierarchy hierarchy) {
        Objects.requireNonNull(hierarchy, "hierarchy");
        if (prototypes.isEmpty()) {
            try (var __ = hierarchy.beginEditBatch()) {
                hierarchy.clearStructureInBatch();
            }
            hierarchy.restoreRevisionTelemetry(topologyRevision, transformDirtyRevision, hierarchyCommittedRevision,
                propagationSerial);
        } else {
            try (var __ = hierarchy.beginEditBatch()) {
                hierarchy.clearStructureInBatch();

                HashSet<ResourceLocation> registered = new HashSet<>();
                for (ResourceLocation rid : rootIds) {
                    BodyPartNode proto = prototypes.get(rid);
                    if (proto == null) {
                        throw new IllegalStateException("Missing multipart prototype for root " + rid);
                    }
                    hierarchy.registerRoot(proto.copySkeleton());
                    registered.add(rid);
                }

                boolean progressed = true;
                while (progressed) {
                    progressed = false;
                    HashSet<ResourceLocation> frontier = new HashSet<>(registered);
                    for (ResourceLocation parentId : frontier) {
                        BodyPartNode pProto = prototypes.get(parentId);
                        if (pProto == null) continue;
                        for (AttachmentPoint ap : pProto.attachmentPointsView()) {
                            if (!ap.hasChild()) continue;
                            ResourceLocation cid = ap.childPartId();
                            if (registered.contains(cid)) continue;
                            BodyPartNode childProto = prototypes.get(cid);
                            if (childProto == null) {
                                throw new IllegalStateException("Missing multipart prototype for child " + cid);
                            }
                            hierarchy.registerChild(childProto.copySkeleton(), childProto.parentId());
                            registered.add(cid);
                            progressed = true;
                        }
                    }
                }

                if (registered.size() != prototypes.size()) {
                    throw new IllegalStateException(
                        "Multipart replay stalled — expected %d nodes, registered %d".formatted(prototypes.size(),
                            registered.size()));
                }
            }

            hierarchy.restoreRevisionTelemetry(topologyRevision, transformDirtyRevision, hierarchyCommittedRevision,
                propagationSerial);
        }
        hierarchy.activityGovernor().notifyHierarchyReplayed();
    }
}
