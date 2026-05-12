package com.sirolf2009.necromancy.multipart.broadphase;

/** Factories for {@link MultipartBroadphaseQueryStrategy} implementations (swap via {@link MultipartBroadphaseWorld#configureQueryAcceleration}). */
public final class MultipartBroadphaseQueryStrategies {

    private MultipartBroadphaseQueryStrategies() {
    }

    /** Spatial tiers merged + {@link MultipartInternalBroadphase.LinearScan} narrow-phase default. */
    public static MultipartBroadphaseQueryStrategy spatialHashMerged(MultipartInternalBroadphase baseRayRefinement) {
        return new SpatialHashMergedQueryStrategy(baseRayRefinement, MultipartBroadphaseQueryStrategyIds.SPATIAL_MERGED_LINEAR_RAY);
    }

    /** Morton-ordered narrow-phase traversal over merged candidates (wide bosses / cache-friendly scans). */
    public static MultipartBroadphaseQueryStrategy spatialHashMergedMortonRay() {
        return new SpatialHashMergedQueryStrategy(
            new MultipartInternalBroadphase.MortonOrderedLinearScan(),
            MultipartBroadphaseQueryStrategyIds.SPATIAL_MERGED_MORTON_RAY
        );
    }

    /**
     * Same behavior as {@link #spatialHashMerged(MultipartInternalBroadphase)} today — reserved hook for a future BVH-backed
     * overlap stream without changing {@link MultipartInternalBroadphase}.
     */
    public static MultipartBroadphaseQueryStrategy spatialHashMergedBvhPlaceholder(MultipartInternalBroadphase baseRayRefinement) {
        return new SpatialHashMergedQueryStrategy(baseRayRefinement, MultipartBroadphaseQueryStrategyIds.SPATIAL_MERGED_BVH_PLACEHOLDER);
    }

    /**
     * Groups broad-phase hits per {@link BroadphaseSlot#entityId()} and runs {@link MultipartBroadphaseEntityAcceleratorRegistry}
     * narrow passes (sorted by ray entry distance — editor picking / surgical traces).
     */
    public static MultipartBroadphaseQueryStrategy subtreeGrouped(MultipartInternalBroadphase baseRayRefinement) {
        return new SubtreeGroupedRayQueryStrategy(baseRayRefinement);
    }
}
