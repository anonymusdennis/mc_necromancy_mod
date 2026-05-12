package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Partition merged candidates by entity root, delegate narrow-phase per subtree via {@link MultipartBroadphaseEntityAcceleratorRegistry},
 * then order hits by ray entry distance (closest-first).
 */
final class SubtreeGroupedRayQueryStrategy extends SpatialHashMergedQueryStrategy {

    SubtreeGroupedRayQueryStrategy(MultipartInternalBroadphase baseRayRefinement) {
        super(baseRayRefinement, MultipartBroadphaseQueryStrategyIds.SUBTREE_GROUPED_RAY);
    }

    @Override
    public List<BroadphaseSlot> segmentRay(MultipartBroadphaseQueryContext ctx, Vec3 start, Vec3 end, boolean chunkPrefilter,
                                             @Nullable MultipartInternalBroadphase refineOverride) {
        if (ctx.level() == null) {
            return super.segmentRay(ctx, start, end, chunkPrefilter, refineOverride);
        }
        double pad = MultipartBroadphaseQueryMerger.raySweepPadding(ctx.mainSpatial(), ctx.pendingSpatial());
        AABB sweep = BroadphaseQueries.segmentBounds(start, end, pad);
        long tb0 = System.nanoTime();
        List<BroadphaseSlot> broad = MultipartBroadphaseQueryMerger.collectOverlappingCandidates(
            ctx.mainSpatial(),
            ctx.pendingSpatial(),
            ctx.chunks(),
            sweep,
            chunkPrefilter);
        long tb1 = System.nanoTime();

        Vec3 dir = end.subtract(start);
        double len = dir.length();
        if (len < 1e-9) {
            ctx.instrumentation().recordRaySegment("ray." + id(), tb1 - tb0, 0L, broad.size(), 0);
            return List.of();
        }
        Vec3 dirN = dir.scale(1.0 / len);
        MultipartInternalBroadphase narrow = refineOverride != null ? refineOverride : baseRayRefinement;

        Map<Integer, List<BroadphaseSlot>> groups = new HashMap<>();
        for (BroadphaseSlot s : broad) {
            groups.computeIfAbsent(s.entityId(), k -> new ArrayList<>()).add(s);
        }

        List<BroadphaseSlot> hits = new ArrayList<>();
        for (Map.Entry<Integer, List<BroadphaseSlot>> e : groups.entrySet()) {
            var mob = ctx.level().getEntity(e.getKey());
            MultipartBroadphaseEntityAccelerator acc =
                MultipartBroadphaseEntityAcceleratorRegistry.get(mob == null ? null : mob.getType());
            hits.addAll(acc.refineAlongRay(start, dirN, len, e.getValue(), narrow));
        }

        hits.sort(Comparator.comparingDouble(s -> {
            double t = BroadphaseQueries.rayEnterDistanceOrNaN(start, dirN, len, s.bounds());
            return Double.isNaN(t) ? Double.POSITIVE_INFINITY : t;
        }));

        long tb2 = System.nanoTime();
        ctx.instrumentation().recordRaySegment("ray." + id(), tb1 - tb0, tb2 - tb1, broad.size(), hits.size());
        return hits;
    }
}
