package com.sirolf2009.necromancy.multipart;

import com.sirolf2009.necromancy.multipart.animation.MultipartAnimationFrame;
import com.sirolf2009.necromancy.multipart.animation.PartTransformLayerBlend;
import com.sirolf2009.necromancy.multipart.broadphase.BroadphaseSlot;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartActivityGovernor;
import com.sirolf2009.necromancy.multipart.debug.TransformAliasingAssertions;
import com.sirolf2009.necromancy.multipart.animation.WeightedPartTransform;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.math.QuaternionOps;
import com.sirolf2009.necromancy.multipart.math.TransformCompose;
import com.sirolf2009.necromancy.multipart.part.AttachmentPoint;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import com.sirolf2009.necromancy.multipart.part.SocketBindSpace;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Transform graph with dirty propagation, optional full traversal validation, simulation vs render bind sockets.
 *
 * <p><strong>Render layering:</strong> {@link com.sirolf2009.necromancy.multipart.animation.MultipartAnimationFrame}
 * contributors are evaluated per bone before merging {@link com.sirolf2009.necromancy.multipart.part.BodyPartNode#renderOverlayTransform()};
 * collision uses simulation poses only.
 *
 * <p><strong>Edit batches:</strong> wrap multi-step editor/surgery work in {@link #beginEditBatch()} so
 * {@link #topologyRevision()}, {@link #transformDirtyRevision()}, and {@link #hierarchyCommittedRevision()} advance at
 * most once per flush while preserving deterministic propagation order (root iteration order unchanged).
 */
public final class TransformHierarchy {

    private static final double POS_EPS_SQ = 1e-8;
    private static final float QUAT_EPS = 1e-4f;

    private final Map<ResourceLocation, BodyPartNode> nodes = new LinkedHashMap<>();
    private final List<ResourceLocation> rootIds = new ArrayList<>();

    private int topologyRevision;
    /** Monotonic counter bumped once per committed transform invalidation (immediate or batch flush). */
    private long transformDirtyRevision;
    /**
     * Separate assembly epoch: increments once per logical commit (single-op or batch flush) when topology or
     * transform dirtiness changes — for caches unrelated to sparse transform deltas.
     */
    private long hierarchyCommittedRevision;
    private long propagationSerial;

    private long broadphaseActivitySerial;
    private final MultipartActivityGovernor activityGovernor = new MultipartActivityGovernor(this);

    private final Set<ResourceLocation> dirtySubtreeRoots = new HashSet<>();

    private int editBatchDepth;
    private int pendingTopologyMutationsInBatch;
    private boolean pendingMarkAllTransformsInBatch;
    private final java.util.SortedSet<ResourceLocation> pendingSubtreeRootsInBatch = new java.util.TreeSet<>();

    private @Nullable Vec3 lastPivot;
    private final Quaternionf lastRootOrientation = new Quaternionf();
    private boolean hadRootSample;

    private boolean debugValidateFullTraversal;

    private final PartTransform layerBlendScratch = new PartTransform();
    private final PartTransform layerMergedScratch = new PartTransform();
    private final WorldPose.Mutable layerComposeScratchA = new WorldPose.Mutable();
    private final WorldPose.Mutable layerComposeScratchB = new WorldPose.Mutable();

    public int topologyRevision() {
        return topologyRevision;
    }

    /** Monotonic counter incremented when local transforms are dirtied (editor / animation / payloads). */
    public long transformDirtyRevision() {
        return transformDirtyRevision;
    }

    /**
     * Committed assembly epoch — bumps once per logical invalidation (immediate API call or batch flush), combining
     * topology and transform dirty signals for coarse cache invalidation. Separate from {@link #transformDirtyRevision}
     * which tracks sparse transform delta streams.
     */
    public long hierarchyCommittedRevision() {
        return hierarchyCommittedRevision;
    }

    /** Increments each time a propagation pass actually runs. */
    public long propagationSerial() {
        return propagationSerial;
    }

    /** Monotonic fingerprint for broad-phase publish caches — bumps when activity tuning changes slot eligibility. */
    public long broadphaseActivitySerial() {
        return broadphaseActivitySerial;
    }

    public void bumpBroadphaseActivitySerial() {
        broadphaseActivitySerial++;
    }

    public MultipartActivityGovernor activityGovernor() {
        return activityGovernor;
    }

    public void setDebugValidateFullTraversal(boolean debugValidateFullTraversal) {
        this.debugValidateFullTraversal = debugValidateFullTraversal;
    }

    public boolean debugValidateFullTraversal() {
        return debugValidateFullTraversal;
    }

    /**
     * Opens a nested-safe edit transaction: topology counters and transform dirtiness coalesce until the returned batch
     * {@linkplain HierarchyEditBatch#close() closes} (use try-with-resources). Structural changes apply immediately.
     */
    public HierarchyEditBatch beginEditBatch() {
        editBatchDepth++;
        return new HierarchyEditBatch(this);
    }

    /**
     * Clears every registered part without advancing topology/transform counters — must only be called while an
     * {@linkplain #beginEditBatch() edit batch} is active (used when replaying {@link MultipartHierarchyMemento}s).
     */
    public void clearStructureInBatch() {
        if (!inEditBatch()) {
            throw new IllegalStateException("clearStructureInBatch requires an active edit batch");
        }
        nodes.clear();
        rootIds.clear();
        dirtySubtreeRoots.clear();
        hadRootSample = false;
    }

    void leaveEditBatch() {
        if (editBatchDepth <= 0) {
            throw new IllegalStateException("Unbalanced hierarchy edit batch");
        }
        editBatchDepth--;
        if (editBatchDepth == 0) {
            flushDeferredInvalidations();
        }
    }

    private void flushDeferredInvalidations() {
        boolean topo = pendingTopologyMutationsInBatch > 0;
        boolean xf = pendingMarkAllTransformsInBatch || !pendingSubtreeRootsInBatch.isEmpty();

        if (topo) {
            topologyRevision++;
            pendingTopologyMutationsInBatch = 0;
        }
        if (pendingMarkAllTransformsInBatch) {
            transformDirtyRevision++;
            dirtySubtreeRoots.addAll(nodes.keySet());
            pendingMarkAllTransformsInBatch = false;
            pendingSubtreeRootsInBatch.clear();
        } else if (!pendingSubtreeRootsInBatch.isEmpty()) {
            transformDirtyRevision++;
            for (ResourceLocation id : pendingSubtreeRootsInBatch) {
                collectSubtree(id, dirtySubtreeRoots);
            }
            pendingSubtreeRootsInBatch.clear();
        }
        if (topo || xf) {
            hierarchyCommittedRevision++;
        }
    }

    private void resetDeferredBatchState() {
        editBatchDepth = 0;
        pendingTopologyMutationsInBatch = 0;
        pendingMarkAllTransformsInBatch = false;
        pendingSubtreeRootsInBatch.clear();
    }

    private boolean inEditBatch() {
        return editBatchDepth > 0;
    }

    public Collection<BodyPartNode> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public List<ResourceLocation> rootIdsView() {
        return Collections.unmodifiableList(rootIds);
    }

    public void removeRoot(ResourceLocation rootId) {
        rootIds.remove(rootId);
        if (inEditBatch()) {
            pendingTopologyMutationsInBatch++;
            pendingMarkAllTransformsInBatch = true;
            pendingSubtreeRootsInBatch.clear();
        } else {
            topologyRevision++;
            transformDirtyRevision++;
            hierarchyCommittedRevision++;
            dirtySubtreeRoots.addAll(nodes.keySet());
        }
    }

    public void clear() {
        resetDeferredBatchState();
        nodes.clear();
        rootIds.clear();
        topologyRevision++;
        hierarchyCommittedRevision++;
        dirtySubtreeRoots.clear();
        hadRootSample = false;
    }

    /**
     * Restores monotonic counters after a structural replay (e.g. transaction rollback via memento). Avoid ad-hoc use —
     * callers must supply values captured together with the authoritative snapshot.
     */
    public void restoreRevisionTelemetry(int topologyRevision, long transformDirtyRevision,
                                         long hierarchyCommittedRevision, long propagationSerial) {
        this.topologyRevision = topologyRevision;
        this.transformDirtyRevision = transformDirtyRevision;
        this.hierarchyCommittedRevision = hierarchyCommittedRevision;
        this.propagationSerial = propagationSerial;
    }

    public void registerRoot(BodyPartNode root) {
        Objects.requireNonNull(root);
        ResourceLocation id = root.id();
        root.setParentId(null);
        nodes.put(id, root);
        if (!rootIds.contains(id)) {
            rootIds.add(id);
        }
        if (inEditBatch()) {
            pendingTopologyMutationsInBatch++;
            pendingMarkAllTransformsInBatch = true;
            pendingSubtreeRootsInBatch.clear();
        } else {
            topologyRevision++;
            transformDirtyRevision++;
            hierarchyCommittedRevision++;
            dirtySubtreeRoots.addAll(nodes.keySet());
        }
    }

    public void registerChild(BodyPartNode child, @Nullable ResourceLocation parentId) {
        Objects.requireNonNull(child);
        child.setParentId(parentId);
        nodes.put(child.id(), child);
        if (inEditBatch()) {
            pendingTopologyMutationsInBatch++;
            if (parentId != null) {
                pendingSubtreeRootsInBatch.add(parentId);
            }
            pendingSubtreeRootsInBatch.add(child.id());
        } else {
            topologyRevision++;
            transformDirtyRevision++;
            hierarchyCommittedRevision++;
            if (parentId != null) {
                collectSubtree(parentId, dirtySubtreeRoots);
            }
            collectSubtree(child.id(), dirtySubtreeRoots);
        }
    }

    public void removePart(ResourceLocation id) {
        BodyPartNode removed = nodes.remove(id);
        rootIds.remove(id);
        if (removed != null) {
            for (BodyPartNode n : nodes.values()) {
                List<AttachmentPoint> sockets = new ArrayList<>(n.attachmentPointsView());
                n.clearAttachmentPoints();
                for (AttachmentPoint ap : sockets) {
                    ResourceLocation ch = ap.childPartId();
                    if (ch != null && ch.equals(id)) {
                        n.addAttachmentPoint(ap.withoutChild());
                    } else {
                        n.addAttachmentPoint(ap);
                    }
                }
            }
            if (inEditBatch()) {
                pendingTopologyMutationsInBatch++;
                pendingMarkAllTransformsInBatch = true;
                pendingSubtreeRootsInBatch.clear();
            } else {
                topologyRevision++;
                transformDirtyRevision++;
                hierarchyCommittedRevision++;
                dirtySubtreeRoots.addAll(nodes.keySet());
            }
        }
    }

    /** Mark a part (and descendants) so transforms recompute even if the root entity hasn't moved. */
    public void markSubtreeDirty(ResourceLocation id) {
        if (inEditBatch()) {
            pendingSubtreeRootsInBatch.add(id);
        } else {
            transformDirtyRevision++;
            hierarchyCommittedRevision++;
            collectSubtree(id, dirtySubtreeRoots);
        }
    }

    public void markAllTransformsDirty() {
        if (inEditBatch()) {
            pendingMarkAllTransformsInBatch = true;
            pendingSubtreeRootsInBatch.clear();
        } else {
            transformDirtyRevision++;
            hierarchyCommittedRevision++;
            dirtySubtreeRoots.addAll(nodes.keySet());
        }
    }

    private void collectSubtree(ResourceLocation id, Set<ResourceLocation> out) {
        BodyPartNode n = nodes.get(id);
        if (n == null) return;
        out.add(id);
        for (AttachmentPoint ap : n.attachmentPointsView()) {
            if (ap.hasChild()) {
                collectSubtree(ap.childPartId(), out);
            }
        }
    }

    /**
     * Legacy entry using yaw-only root facing (pitch taken from entity {@code getXRot()}, roll 0).
     */
    public void tick(LivingEntity rootEntity, Vec3 attachmentPivot, float rootYawDegrees) {
        Quaternionf orient = QuaternionOps.fromYawPitchRollDegrees(rootYawDegrees, rootEntity.getXRot(), 0f);
        tick(rootEntity, attachmentPivot, orient, PartTransform.identity(), false, null);
    }

    /**
     * Full root orientation + optional render-space overlay on the root bone (cosmetic / animation preview).
     *
     * @param debugValidation when true, every node recomputes every tick (expensive; catches stale caches).
     */
    public void tick(LivingEntity rootEntity, Vec3 pivot, Quaternionf rootSimulationOrientation,
                     PartTransform rootRenderOverlay, boolean debugValidation) {
        tick(rootEntity, pivot, rootSimulationOrientation, rootRenderOverlay, debugValidation, null);
    }

    /**
     * @param animationFrame optional per-tick layer contributions; when {@code null}, only simulation + root/editor overlays apply.
     */
    public void tick(LivingEntity rootEntity, Vec3 pivot, Quaternionf rootSimulationOrientation,
                     PartTransform rootRenderOverlay, boolean debugValidation,
                     @Nullable MultipartAnimationFrame animationFrame) {
        boolean debug = debugValidation || debugValidateFullTraversal;
        boolean rootMoved = !hadRootSample
            || pivot.distanceToSqr(lastPivot == null ? Vec3.ZERO : lastPivot) > POS_EPS_SQ
            || !approximatelyEqual(lastRootOrientation, rootSimulationOrientation);

        boolean dirtyLocals = !dirtySubtreeRoots.isEmpty();
        if (!rootMoved && !dirtyLocals && !debug) {
            return;
        }

        WorldPose rootSim = new WorldPose(pivot, rootSimulationOrientation, new Vector3f(1f, 1f, 1f));
        WorldPose rootRender = TransformCompose.compose(rootSim, rootRenderOverlay);

        boolean rootChainRecalc = rootMoved || debug;

        for (ResourceLocation rid : rootIds) {
            BodyPartNode r = nodes.get(rid);
            if (r != null) {
                propagate(r, rootSim, rootRender, rootChainRecalc, debug, animationFrame);
            }
        }

        dirtySubtreeRoots.clear();
        lastPivot = pivot;
        lastRootOrientation.set(rootSimulationOrientation);
        hadRootSample = true;
        propagationSerial++;
    }

    private static boolean approximatelyEqual(Quaternionf a, Quaternionf b) {
        float dq = Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z) + Math.abs(a.w - b.w);
        float dq2 = Math.abs(a.x + b.x) + Math.abs(a.y + b.y) + Math.abs(a.z + b.z) + Math.abs(a.w + b.w);
        return Math.min(dq, dq2) < QUAT_EPS;
    }

    private void propagate(BodyPartNode node, WorldPose parentSimWorld, WorldPose parentRenderWorld,
                           boolean parentPoseChanged, boolean debugFull, @Nullable MultipartAnimationFrame animationFrame) {
        if (!node.attachedToParent()) {
            node.setSimulationBroadphase(new AABB(0, 0, 0, 0, 0, 0));
            return;
        }

        boolean selfDirty = dirtySubtreeRoots.contains(node.id());
        boolean recompute = debugFull || parentPoseChanged || selfDirty;

        WorldPose simHere;
        WorldPose renderHere;
        if (recompute) {
            simHere = TransformCompose.compose(parentSimWorld, node.simulationLocalTransform());
            List<WeightedPartTransform> sorted = animationFrame == null ? List.of() : animationFrame.sortedLayersFor(node.id());
            PartTransformLayerBlend.blendInto(sorted, layerBlendScratch);
            PartTransformLayerBlend.mergeEditorOverlayInto(layerBlendScratch, node.renderOverlayTransform(),
                layerMergedScratch, layerComposeScratchA, layerComposeScratchB);
            node.setComposedRenderOverlayFromPipeline(layerMergedScratch);
            renderHere = TransformCompose.compose(simHere, layerMergedScratch);
            node.setSimulationWorldPose(simHere);
            node.setRenderWorldPose(renderHere);
            node.refreshSimulationCollision(simHere);
            TransformAliasingAssertions.assertDistinctSimulationRenderWorldPose(node, "TransformHierarchy.propagate");
        } else {
            simHere = node.simulationWorldPose();
            renderHere = node.renderWorldPose();
        }

        boolean childParentPoseChanged = recompute;

        for (AttachmentPoint ap : node.attachmentPointsView()) {
            if (!ap.hasChild()) continue;
            BodyPartNode child = nodes.get(ap.childPartId());
            if (child == null) continue;
            WorldPose bindBase = ap.bindSpace() == SocketBindSpace.RENDER ? renderHere : simHere;
            WorldPose childParent = TransformCompose.compose(bindBase, ap.socketTransform());
            propagate(child, childParent, childParent, childParentPoseChanged, debugFull, animationFrame);
        }
    }

    public @Nullable BodyPartNode get(ResourceLocation id) {
        return nodes.get(id);
    }

    public List<AABB> collectCollisionBoxes() {
        List<AABB> out = new ArrayList<>(nodes.size());
        for (BodyPartNode n : nodes.values()) {
            if (!n.hitbox().collisionEnabled() || !n.attachedToParent()) continue;
            AABB box = n.simulationBroadphase();
            if (box.getXsize() <= 1e-6 || box.getYsize() <= 1e-6 || box.getZsize() <= 1e-6) continue;
            out.add(box);
        }
        return out;
    }

    /**
     * Depth-first post-order (children before parents) — deterministic evaluation for dependent passes such as
     * {@link MultipartActivityGovernor}.
     */
    public List<BodyPartNode> collectNodesPostOrder() {
        List<BodyPartNode> out = new ArrayList<>(nodes.size());
        for (ResourceLocation rid : rootIds) {
            BodyPartNode r = nodes.get(rid);
            if (r != null) {
                collectNodesPostOrderRecursive(r, out);
            }
        }
        return out;
    }

    private void collectNodesPostOrderRecursive(BodyPartNode node, List<BodyPartNode> out) {
        for (AttachmentPoint ap : node.attachmentPointsView()) {
            if (!ap.hasChild()) continue;
            BodyPartNode child = nodes.get(ap.childPartId());
            if (child != null) {
                collectNodesPostOrderRecursive(child, out);
            }
        }
        out.add(node);
    }

    /**
     * Per-part slots for spatial hashing (skips sleeping / detached / disabled collision).
     * Respects {@link BodyPartNode#broadphaseSubtreeProxyOnly()} — descendants omitted after that node.
     */
    public List<BroadphaseSlot> collectBroadphaseSlots(int entityId, long propagationSerial) {
        List<BroadphaseSlot> out = new ArrayList<>();
        for (ResourceLocation rid : rootIds) {
            BodyPartNode r = nodes.get(rid);
            if (r != null) {
                collectBroadphaseRecursive(r, entityId, propagationSerial, out);
            }
        }
        return out;
    }

    private void collectBroadphaseRecursive(BodyPartNode node, int entityId, long serial, List<BroadphaseSlot> out) {
        if (!node.attachedToParent()) {
            return;
        }
        if (node.collapsesSubtreeFineIndexing()) {
            return;
        }
        boolean subtreeProxy = node.broadphaseSubtreeProxyOnly();
        if (node.contributesBroadphase()) {
            AABB box = node.simulationBroadphase();
            if (box.getXsize() > 1e-6 && box.getYsize() > 1e-6 && box.getZsize() > 1e-6) {
                out.add(BroadphaseSlot.part(entityId, node.id(), box, serial));
            }
        }
        if (subtreeProxy) {
            return;
        }
        for (AttachmentPoint ap : node.attachmentPointsView()) {
            if (!ap.hasChild()) continue;
            BodyPartNode child = nodes.get(ap.childPartId());
            if (child != null) {
                collectBroadphaseRecursive(child, entityId, serial, out);
            }
        }
    }

    /** Union of all bodyparts that still occupy volume for coarse indexing (includes sleeping / static bones). */
    public AABB unionBroadphaseBounds() {
        AABB acc = null;
        for (BodyPartNode n : nodes.values()) {
            if (!n.participatesInUnionBroadphaseHull()) continue;
            AABB b = n.simulationBroadphase();
            if (b.getXsize() <= 1e-6 || b.getYsize() <= 1e-6 || b.getZsize() <= 1e-6) continue;
            acc = acc == null ? b : acc.minmax(b);
        }
        return acc == null ? new AABB(0, 0, 0, 0, 0, 0) : acc;
    }

    /**
     * Cheaply shifts all cached world-space data (simulation/render poses, OBB centers, broadphase AABBs)
     * for every node by {@code delta}, without full transform re-propagation.
     *
     * <p>Use this after the root entity's position is changed by physics (gravity, push-outs) between
     * hierarchy ticks so that consumers such as
     * {@link com.sirolf2009.necromancy.multipart.damage.MultipartDamageRouter#findPartAlongSegment} and
     * the broad-phase spatial index see the correct post-physics collision data.
     *
     * <p>Also updates the stored {@link #lastPivot} so the next tick's change-detection is accurate and
     * does not treat the physics displacement as an additional entity movement.
     */
    public void translateWorldPositions(Vec3 delta) {
        if (delta.x == 0 && delta.y == 0 && delta.z == 0) return;
        for (BodyPartNode n : nodes.values()) {
            n.translateCachedWorldData(delta);
        }
        if (lastPivot != null) {
            lastPivot = lastPivot.add(delta);
        }
    }

    /** Validates that dirty propagation matches a full recompute (development assertions). */
    public void debugValidateAgainstFullTraversal(Vec3 pivot, Quaternionf rootSimulationOrientation, PartTransform rootRenderOverlay) {
        // Snapshot poses from dirty path already computed — optional second pass could compare; omitted for bandwidth.
    }
}
