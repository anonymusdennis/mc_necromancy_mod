package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Default merged spatial-hash traversal + single-list narrow-phase refinement ({@link MultipartInternalBroadphase}).
 */
class SpatialHashMergedQueryStrategy implements MultipartBroadphaseQueryStrategy {

    protected final MultipartInternalBroadphase baseRayRefinement;
    private final String strategyId;

    SpatialHashMergedQueryStrategy(MultipartInternalBroadphase baseRayRefinement, String strategyId) {
        this.baseRayRefinement = baseRayRefinement;
        this.strategyId = strategyId;
    }

    @Override
    public String id() {
        return strategyId;
    }

    @Override
    public MultipartInternalBroadphase baseMultipartRayBroadphase() {
        return baseRayRefinement;
    }

    @Override
    public void overlapCandidates(MultipartBroadphaseQueryContext ctx, AABB query, boolean chunkPrefilter,
                                  Consumer<BroadphaseSlot> consumer) {
        long t0 = System.nanoTime();
        AtomicInteger emitted = new AtomicInteger();
        MultipartBroadphaseQueryMerger.forEachCandidateOverlapping(
            ctx.mainSpatial(),
            ctx.pendingSpatial(),
            ctx.chunks(),
            query,
            chunkPrefilter,
            s -> {
                emitted.incrementAndGet();
                consumer.accept(s);
            });
        ctx.instrumentation().recordOverlap("overlap." + strategyId, System.nanoTime() - t0, emitted.get());
    }

    @Override
    public List<BroadphaseSlot> segmentRay(MultipartBroadphaseQueryContext ctx, Vec3 start, Vec3 end, boolean chunkPrefilter,
                                             @Nullable MultipartInternalBroadphase refineOverride) {
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
            ctx.instrumentation().recordRaySegment("ray." + strategyId, tb1 - tb0, 0L, broad.size(), 0);
            return List.of();
        }
        Vec3 dirN = dir.scale(1.0 / len);
        MultipartInternalBroadphase narrow = refineOverride != null ? refineOverride : baseRayRefinement;
        List<BroadphaseSlot> hits = narrow.filterAlongRay(start, dirN, len, broad);
        long tb2 = System.nanoTime();
        ctx.instrumentation().recordRaySegment("ray." + strategyId, tb1 - tb0, tb2 - tb1, broad.size(), hits.size());
        return hits;
    }
}
