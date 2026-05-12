package com.sirolf2009.necromancy.multipart.broadphase;

import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Per-level multipart broad-phase: chunk occupancy (coarse) + tiered spatial hash (fine grid, optional macro grid,
 * oversized buckets for extreme bounds).
 * <p>
 * Simulation publishes structural edits via {@link MultipartBroadphaseHooks}; readers should prefer
 * {@link #publishQuerySnapshot()} / {@link #readSnapshot()} ({@link MultipartBroadphaseSnapshot}) instead of walking
 * {@link #spatialIndex()} while multipart ticks may still be reordering slots. Topology / tooling batches may bracket work with
 * {@link #beginDeferredBroadphasePublish()} / {@link #endDeferredBroadphasePublish()} so rapid edits issue one spatial insert burst per scope.
 */
public final class MultipartBroadphaseWorld {

    private static final double PIVOT_EPS_SQ = 1e-8;

    private record PublishCache(long propagationSerial, long activitySerial, int topologyRevision, Vec3 pivot, boolean dormant,
                                int slotCount) {
    }

    private final Object snapshotLock = new Object();

    private final Level level;
    private final SpatialHashMultipartIndex spatial;
    private final ChunkEntityBroadphaseIndex chunks = new ChunkEntityBroadphaseIndex();
    private final MultipartInternalBroadphase internalFine;
    private MultipartBroadphaseQueryStrategy queryStrategy;
    private MultipartBroadphaseInstrumentation instrumentation;

    private final java.util.Map<Integer, PublishCache> lastPublish = new java.util.HashMap<>();

    private volatile MultipartBroadphaseSnapshot publishedSnapshot;
    private volatile boolean spatialStructureDirty = true;

    /** Nested manual / topology-driven deferral — spatial inserts batch until depth returns to zero. */
    private int deferredPublishDepth;

    /** Latest computed slots per entity while deferred; {@linkplain List#copyOf copied} on insert. */
    private final java.util.HashMap<Integer, List<BroadphaseSlot>> deferredSlotsByEntity = new java.util.HashMap<>();

    public MultipartBroadphaseWorld(Level level, double spatialCellSize,
                                    MultipartInternalBroadphase internalFine) {
        this(level, spatialCellSize, MultipartBroadphaseSpatialPolicy.DEFAULT, internalFine);
    }

    public MultipartBroadphaseWorld(Level level, double spatialCellSize,
                                    MultipartBroadphaseSpatialPolicy spatialPolicy,
                                    MultipartInternalBroadphase internalFine) {
        this(level, spatialCellSize, spatialPolicy, internalFine, MultipartBroadphaseInstrumentation.noop());
    }

    public MultipartBroadphaseWorld(Level level, double spatialCellSize,
                                    MultipartBroadphaseSpatialPolicy spatialPolicy,
                                    MultipartInternalBroadphase internalFine,
                                    MultipartBroadphaseInstrumentation instrumentation) {
        this(level,
            spatialCellSize,
            spatialPolicy,
            internalFine,
            MultipartBroadphaseQueryStrategies.spatialHashMerged(internalFine),
            instrumentation);
    }

    public MultipartBroadphaseWorld(Level level, double spatialCellSize,
                                    MultipartBroadphaseSpatialPolicy spatialPolicy,
                                    MultipartInternalBroadphase internalFine,
                                    MultipartBroadphaseQueryStrategy queryStrategy,
                                    MultipartBroadphaseInstrumentation instrumentation) {
        this.level = level;
        this.spatial = new SpatialHashMultipartIndex(spatialCellSize, spatialPolicy);
        this.internalFine = internalFine;
        this.queryStrategy = Objects.requireNonNull(queryStrategy, "queryStrategy");
        this.instrumentation = instrumentation != null ? instrumentation : MultipartBroadphaseInstrumentation.noop();
    }

    /** Narrow-phase ray refinement backing {@linkplain MultipartInternalBroadphase} tooling hooks (paired with {@link #queryStrategy()}). */
    public MultipartInternalBroadphase internalRayBroadphase() {
        return internalFine;
    }

    public MultipartBroadphaseQueryStrategy queryStrategy() {
        return queryStrategy;
    }

    public MultipartBroadphaseInstrumentation instrumentation() {
        return instrumentation;
    }

    /**
     * Runtime tuning hook — swaps traversal/instrumentation (mark dirty so snapshots observe new hooks).
     * Pass {@code null} instrumentation to detach profiling counters.
     */
    public void configureQueryAcceleration(MultipartBroadphaseQueryStrategy strategy,
                                           MultipartBroadphaseInstrumentation instrument) {
        this.queryStrategy = Objects.requireNonNull(strategy, "strategy");
        this.instrumentation = instrument != null ? instrument : MultipartBroadphaseInstrumentation.noop();
        markSpatialStructureDirty();
    }

    public Level level() {
        return level;
    }

    /**
     * Mutable spatial tiers backing simulation writes ({@link #updateFromRoot}). Query systems must avoid iterating this
     * directly across threads or snapshot boundaries — use {@link #readSnapshot()} instead.
     */
    public SpatialHashMultipartIndex spatialIndex() {
        return spatial;
    }

    public ChunkEntityBroadphaseIndex chunkIndex() {
        return chunks;
    }

    public double spatialCellSize() {
        return spatial.cellSize();
    }

    /** Macro-tier cell size (world units); mirrors data frozen into {@link MultipartBroadphaseSnapshot}. */
    public double spatialMacroCellSize() {
        return spatial.macroCellSize();
    }

    /** Increments nesting depth — paired {@linkplain #endDeferredBroadphasePublish()} performs one spatial flush when depth hits zero. */
    public void beginDeferredBroadphasePublish() {
        deferredPublishDepth++;
    }

    /**
     * Ends one nesting level of deferred aggregation and flushes staged inserts into the live spatial hash when the outermost
     * scope completes.
     */
    public void endDeferredBroadphasePublish() {
        if (deferredPublishDepth <= 0) {
            throw new IllegalStateException("Unbalanced multipart broad-phase defer/end (depth=" + deferredPublishDepth + ")");
        }
        deferredPublishDepth--;
        if (deferredPublishDepth == 0) {
            flushDeferredBroadphaseToSpatial();
            markSpatialStructureDirty();
        }
    }

    public boolean isDeferredPublishActive() {
        return deferredPublishDepth > 0;
    }

    /** Drops staged slots for one entity (rollback / entity removal) without flushing unrelated deferred roots. */
    public void clearDeferredSlotsForEntity(int entityId) {
        deferredSlotsByEntity.remove(entityId);
    }

    private void flushDeferredBroadphaseToSpatial() {
        if (deferredSlotsByEntity.isEmpty()) {
            return;
        }
        for (List<BroadphaseSlot> slots : deferredSlotsByEntity.values()) {
            spatial.insertSlots(slots);
        }
        deferredSlotsByEntity.clear();
    }

    /** Latest immutable snapshot when {@link #publishQuerySnapshot()} already ran this tick; avoids rebuilding after mutations. */
    public Optional<MultipartBroadphaseSnapshot> peekPublishedSnapshot() {
        return Optional.ofNullable(publishedSnapshot);
    }

    /**
     * Refreshes the published immutable snapshot when the spatial structure changed since the last publish, otherwise
     * returns the cached instance (cheap fast-path at level tick boundaries).
     */
    public MultipartBroadphaseSnapshot publishQuerySnapshot() {
        if (!spatialStructureDirty) {
            MultipartBroadphaseSnapshot cached = publishedSnapshot;
            if (cached != null) {
                MultipartTelemetry.recordBroadphasePublishCached(level);
                return cached;
            }
        }
        synchronized (snapshotLock) {
            if (!spatialStructureDirty && publishedSnapshot != null) {
                MultipartTelemetry.recordBroadphasePublishCached(level);
                return publishedSnapshot;
            }
            long tRebuild = System.nanoTime();
            long seq = publishedSnapshot == null ? 1L : publishedSnapshot.sequence() + 1L;
            MultipartFrozenSpatialIndex sf = MultipartFrozenSpatialIndex.copyOf(spatial);
            MultipartFrozenSpatialIndex pendingFrozen = null;
            if (!deferredSlotsByEntity.isEmpty()) {
                pendingFrozen = MultipartFrozenSpatialIndex.freezePendingSlotLists(
                    deferredSlotsByEntity.values(),
                    spatial.cellSize(),
                    spatial.policy());
            }
            MultipartFrozenChunkBroadphaseIndex cf = MultipartFrozenChunkBroadphaseIndex.copyOf(chunks);
            MultipartBroadphaseSnapshot snap = new MultipartBroadphaseSnapshot(
                level, seq, sf, pendingFrozen, cf, queryStrategy, instrumentation);
            publishedSnapshot = snap;
            spatialStructureDirty = false;
            MultipartTelemetry.recordBroadphasePublishRebuild(level, System.nanoTime() - tRebuild);
            return snap;
        }
    }

    /** Convenience alias for {@link #publishQuerySnapshot()} — use after verifying mutations settled for the current step. */
    public MultipartBroadphaseSnapshot readSnapshot() {
        return publishQuerySnapshot();
    }

    private void markSpatialStructureDirty() {
        spatialStructureDirty = true;
    }

    /** Clears every structure — use on dimension unload if explicit lifecycle is preferred over weak maps. */
    public void clearAll() {
        deferredSlotsByEntity.clear();
        deferredPublishDepth = 0;
        spatial.clear();
        chunks.clear();
        lastPublish.clear();
        synchronized (snapshotLock) {
            publishedSnapshot = null;
            spatialStructureDirty = true;
        }
    }

    public void removeEntity(int entityId) {
        markSpatialStructureDirty();
        deferredSlotsByEntity.remove(entityId);
        spatial.removeEntity(entityId);
        chunks.removeEntity(entityId);
        lastPublish.remove(entityId);
    }

    /**
     * Incrementally refreshes one multipart root. Skips work when propagation serial, topology, pivot, dormant flag,
     * and slot count are unchanged (cheap fast-path for idle mobs).
     */
    public void updateFromRoot(RootMobEntity root) {
        Entity e = root.asMultipartRoot();
        Level lvl = e.level();
        if (lvl != level) {
            MultipartBroadphaseRegistry.get(lvl).updateFromRoot(root);
            return;
        }

        TransformHierarchy h = root.multipartHierarchy();
        long serial = h.propagationSerial();
        long activitySerial = h.broadphaseActivitySerial();
        int topo = h.topologyRevision();
        Vec3 pivot = root.multipartPivot();
        boolean dormant = root.multipartBroadphaseDormant();

        List<BroadphaseSlot> slots = buildSlots(root, h, serial, dormant);
        int slotCount = slots.size();

        PublishCache prev = lastPublish.get(e.getId());
        if (prev != null
            && prev.propagationSerial() == serial
            && prev.activitySerial() == activitySerial
            && prev.topologyRevision() == topo
            && prev.dormant() == dormant
            && prev.slotCount() == slotCount
            && pivot.distanceToSqr(prev.pivot()) <= PIVOT_EPS_SQ) {
            return;
        }

        markSpatialStructureDirty();
        spatial.removeEntity(e.getId());
        chunks.removeEntity(e.getId());
        if (deferredPublishDepth > 0) {
            if (slots.isEmpty()) {
                deferredSlotsByEntity.remove(e.getId());
            } else {
                deferredSlotsByEntity.put(e.getId(), List.copyOf(slots));
                BroadphaseSlot union = unionSlot(e.getId(), slots, serial);
                chunks.updateEntityChunks(e.getId(), union);
            }
        } else {
            if (!slots.isEmpty()) {
                spatial.insertSlots(slots);
                BroadphaseSlot union = unionSlot(e.getId(), slots, serial);
                chunks.updateEntityChunks(e.getId(), union);
            }
        }

        lastPublish.put(e.getId(), new PublishCache(serial, activitySerial, topo, pivot, dormant, slotCount));
    }

    private static List<BroadphaseSlot> buildSlots(RootMobEntity root, TransformHierarchy h, long serial,
                                                   boolean dormant) {
        int id = root.asMultipartRoot().getId();
        if (dormant) {
            AABB u = h.unionBroadphaseBounds();
            if (u.getXsize() <= 1e-6 || u.getYsize() <= 1e-6 || u.getZsize() <= 1e-6) {
                return List.of();
            }
            return List.of(BroadphaseSlot.aggregate(id, u, serial));
        }
        return h.collectBroadphaseSlots(id, serial);
    }

    private static BroadphaseSlot unionSlot(int entityId, List<BroadphaseSlot> slots, long serial) {
        AABB acc = slots.getFirst().bounds();
        for (int i = 1; i < slots.size(); i++) {
            acc = acc.minmax(slots.get(i).bounds());
        }
        return BroadphaseSlot.aggregate(entityId, acc, serial);
    }

    public void forEachCandidateOverlapping(AABB query, boolean chunkPrefilter, Consumer<BroadphaseSlot> consumer) {
        readSnapshot().forEachCandidateOverlapping(query, chunkPrefilter, consumer);
    }

    /**
     * Ray broad-phase: inflate segment by spatial cell size (conservative), optional chunk filter, then internal
     * refinement (linear scan / future BVH).
     */
    public List<BroadphaseSlot> collectCandidatesAlongSegment(Vec3 start, Vec3 end, boolean chunkPrefilter,
                                                              MultipartInternalBroadphase refine) {
        return readSnapshot().collectCandidatesAlongSegment(start, end, chunkPrefilter, refine);
    }
}
