package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Pluggable acceleration policy after coarse spatial-hash / chunk culling: overlap enumeration,
 * segment-ray candidate refinement ordering, and optional per-entity subtree passes.
 * <p>
 * Narrow-phase ray tests remain on {@link MultipartInternalBroadphase}; strategies choose how candidate lists are produced,
 * partitioned, and ordered before that seam runs.
 */
public interface MultipartBroadphaseQueryStrategy {

    /** Stable identifier for profiling / HUD (see {@link MultipartBroadphaseQueryStrategyIds}). */
    String id();

    /** Default {@link MultipartInternalBroadphase} when callers pass {@code null} refine overrides on ray queries. */
    MultipartInternalBroadphase baseMultipartRayBroadphase();

    void overlapCandidates(
        MultipartBroadphaseQueryContext ctx,
        AABB query,
        boolean chunkPrefilter,
        Consumer<BroadphaseSlot> consumer
    );

    List<BroadphaseSlot> segmentRay(
        MultipartBroadphaseQueryContext ctx,
        Vec3 start,
        Vec3 end,
        boolean chunkPrefilter,
        @Nullable MultipartInternalBroadphase refineOverride
    );
}
