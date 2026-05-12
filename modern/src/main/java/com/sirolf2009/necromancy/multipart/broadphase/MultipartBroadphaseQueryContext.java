package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable bundle passed into {@link MultipartBroadphaseQueryStrategy} for one snapshot query (spatial tiers + chunk mask + hooks).
 */
public record MultipartBroadphaseQueryContext(
    @Nullable Level level,
    MultipartFrozenSpatialIndex mainSpatial,
    @Nullable MultipartFrozenSpatialIndex pendingSpatial,
    MultipartFrozenChunkBroadphaseIndex chunks,
    MultipartBroadphaseInstrumentation instrumentation
) {
}
