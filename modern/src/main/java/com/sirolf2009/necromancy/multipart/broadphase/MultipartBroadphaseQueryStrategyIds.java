package com.sirolf2009.necromancy.multipart.broadphase;

/** Stable identifiers for {@link MultipartBroadphaseQueryStrategy#id()} (profiler columns, editor tooling). */
public final class MultipartBroadphaseQueryStrategyIds {

    public static final String SPATIAL_MERGED_LINEAR_RAY = "spatial_hash.merged.linear_ray";
    public static final String SPATIAL_MERGED_MORTON_RAY = "spatial_hash.merged.morton_ray";
    public static final String SPATIAL_MERGED_BVH_PLACEHOLDER = "spatial_hash.merged.bvh_placeholder";
    public static final String SUBTREE_GROUPED_RAY = "spatial_hash.subtree_grouped";

    private MultipartBroadphaseQueryStrategyIds() {
    }
}
