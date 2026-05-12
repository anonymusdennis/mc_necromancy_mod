package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartBroadphaseQueryAccelerationTest {

    @Test
    void instrumentationCountersAccumulateOverlapAndRay() {
        MultipartBroadphaseInstrumentationCounters c = new MultipartBroadphaseInstrumentationCounters();
        c.recordOverlap("x", 100, 3);
        c.recordRaySegment("y", 200, 50, 12, 4);
        assertEquals(1, c.overlapQueries());
        assertEquals(3, c.overlapCandidates());
        assertEquals(100, c.overlapNanos());
        assertEquals(1, c.rayQueries());
        assertEquals(12, c.rayBroadCandidates());
        assertEquals(4, c.rayNarrowHits());
        assertEquals(200, c.rayBroadNanos());
        assertEquals(50, c.rayNarrowNanos());
    }

    @Test
    void rayEnterDistanceAlongPositiveX() {
        Vec3 origin = new Vec3(-1, 0.5, 0.5);
        Vec3 dir = new Vec3(1, 0, 0);
        AABB box = new AABB(0, 0, 0, 2, 2, 2);
        double t = BroadphaseQueries.rayEnterDistanceOrNaN(origin, dir, 10, box);
        assertEquals(1.0, t, 1e-6);
    }

    @Test
    void spatialMergedStrategyRunsOverlapWithoutLevelTouch() {
        SpatialHashMultipartIndex live = new SpatialHashMultipartIndex(4.0);
        live.insertSlots(java.util.List.of(BroadphaseSlot.part(
            42, ResourceLocation.parse("necromancy:test"), new AABB(0, 0, 0, 1, 1, 1), 1L)));
        MultipartFrozenSpatialIndex frozen = MultipartFrozenSpatialIndex.copyOf(live);
        MultipartFrozenChunkBroadphaseIndex chunks = MultipartFrozenChunkBroadphaseIndex.copyOf(new ChunkEntityBroadphaseIndex());
        MultipartBroadphaseInstrumentationCounters ctr = new MultipartBroadphaseInstrumentationCounters();
        MultipartBroadphaseQueryContext ctx = new MultipartBroadphaseQueryContext(null, frozen, null, chunks, ctr);

        MultipartBroadphaseQueryStrategy strat = MultipartBroadphaseQueryStrategies.spatialHashMerged(new MultipartInternalBroadphase.LinearScan());
        java.util.concurrent.atomic.AtomicInteger hits = new java.util.concurrent.atomic.AtomicInteger();
        strat.overlapCandidates(ctx, new AABB(-2, -2, -2, 4, 4, 4), false, s -> hits.incrementAndGet());

        assertEquals(1, hits.get());
        assertTrue(ctr.overlapQueries() >= 1);
    }
}
