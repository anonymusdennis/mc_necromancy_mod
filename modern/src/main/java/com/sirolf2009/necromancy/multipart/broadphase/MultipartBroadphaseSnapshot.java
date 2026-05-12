package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Immutable read-only view of multipart broad-phase state for a single logical publish step.
 * <p>
 * When topology edits batch {@linkplain MultipartBroadphaseWorld deferred spatial inserts}, {@link #spatial()} exposes
 * the flushed tiers while {@link #pendingSpatial()} carries staged slots merged transparently into query APIs.
 * <p>
 * Query traversal policy is carried by {@link #queryStrategy()} ({@link MultipartBroadphaseQueryStrategy}); instrumentation via
 * {@link #instrumentation()} captures overlap / ray timings emitted during reads against this snapshot.
 */
public final class MultipartBroadphaseSnapshot {

    private final Level level;
    private final long sequence;
    private final MultipartFrozenSpatialIndex spatial;
    private final @Nullable MultipartFrozenSpatialIndex pendingSpatial;
    private final MultipartFrozenChunkBroadphaseIndex chunks;
    private final MultipartBroadphaseQueryStrategy queryStrategy;
    private final MultipartBroadphaseInstrumentation instrumentation;

    MultipartBroadphaseSnapshot(
        Level level,
        long sequence,
        MultipartFrozenSpatialIndex spatial,
        @Nullable MultipartFrozenSpatialIndex pendingSpatial,
        MultipartFrozenChunkBroadphaseIndex chunks,
        MultipartBroadphaseQueryStrategy queryStrategy,
        MultipartBroadphaseInstrumentation instrumentation
    ) {
        this.level = level;
        this.sequence = sequence;
        this.spatial = spatial;
        this.pendingSpatial = pendingSpatial;
        this.chunks = chunks;
        this.queryStrategy = queryStrategy;
        this.instrumentation = instrumentation;
    }

    public Level level() {
        return level;
    }

    /** Monotonic per {@link MultipartBroadphaseWorld} publish generation (resets when the world snapshot is cleared). */
    public long sequence() {
        return sequence;
    }

    /** Flushed spatial tiers (entities without staged deferred inserts appear only here). */
    public MultipartFrozenSpatialIndex spatial() {
        return spatial;
    }

    /** Latest staged slots from {@linkplain MultipartBroadphaseWorld deferred publishing}; {@code null} when idle. */
    public @Nullable MultipartFrozenSpatialIndex pendingSpatial() {
        return pendingSpatial;
    }

    /** Frozen traversal policy + instrumentation bundled into this snapshot. */
    public MultipartBroadphaseQueryStrategy queryStrategy() {
        return queryStrategy;
    }

    public MultipartBroadphaseInstrumentation instrumentation() {
        return instrumentation;
    }

    /** Default narrow-phase refinement backing ray queries when overrides are {@code null}. */
    public MultipartInternalBroadphase defaultRayBroadphase() {
        return queryStrategy.baseMultipartRayBroadphase();
    }

    private MultipartBroadphaseQueryContext queryContext() {
        return new MultipartBroadphaseQueryContext(level, spatial, pendingSpatial, chunks, instrumentation);
    }

    /**
     * Axis-aligned candidate enumeration with optional chunk prefilter (same semantics as
     * {@link MultipartBroadphaseWorld#forEachCandidateOverlapping}).
     */
    public void forEachCandidateOverlapping(AABB query, boolean chunkPrefilter, Consumer<BroadphaseSlot> consumer) {
        queryStrategy.overlapCandidates(queryContext(), query, chunkPrefilter, consumer);
    }

    public List<BroadphaseSlot> collectCandidatesAlongSegment(Vec3 start, Vec3 end, boolean chunkPrefilter,
                                                              MultipartInternalBroadphase refine) {
        return queryStrategy.segmentRay(queryContext(), start, end, chunkPrefilter, refine);
    }

    /** Chunk occupancy frozen with this snapshot (may be empty when no multipart roots published chunks yet). */
    public boolean hasChunkOccupancy() {
        return !chunks.isEmpty();
    }
}
