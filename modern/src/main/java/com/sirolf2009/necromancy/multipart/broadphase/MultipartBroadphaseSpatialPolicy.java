package com.sirolf2009.necromancy.multipart.broadphase;

/**
 * Tier thresholds for multipart spatial indexing: fine uniform grid, coarser macro grid, then oversized buckets.
 */
public record MultipartBroadphaseSpatialPolicy(
    int maxFineCellsPerSlot,
    double macroCellSizeMultiplier,
    int maxMacroCellsPerSlot
) {
    public static final MultipartBroadphaseSpatialPolicy DEFAULT =
        new MultipartBroadphaseSpatialPolicy(64, 8.0, 64);

    public MultipartBroadphaseSpatialPolicy {
        if (maxFineCellsPerSlot < 1) {
            throw new IllegalArgumentException("maxFineCellsPerSlot must be >= 1");
        }
        if (macroCellSizeMultiplier <= 1.0 + 1e-9) {
            throw new IllegalArgumentException("macroCellSizeMultiplier must be > 1");
        }
        if (maxMacroCellsPerSlot < 1) {
            throw new IllegalArgumentException("maxMacroCellsPerSlot must be >= 1");
        }
    }

    public double macroCellSize(double fineCellSize) {
        return fineCellSize * macroCellSizeMultiplier;
    }
}
