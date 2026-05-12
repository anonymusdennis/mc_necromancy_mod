package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipartBroadphaseSnapshotIsolationTest {

    @Test
    void frozenSpatialIgnoresLiveSpatialClears() {
        SpatialHashMultipartIndex live = new SpatialHashMultipartIndex(4.0);
        live.insertSlots(List.of(BroadphaseSlot.aggregate(42, new AABB(0, 0, 0, 1, 1, 1), 9L)));
        MultipartFrozenSpatialIndex frozen = MultipartFrozenSpatialIndex.copyOf(live);
        live.clear();

        AtomicInteger hits = new AtomicInteger();
        frozen.forEachSlotOverlapping(new AABB(-4, -4, -4, 8, 8, 8), s -> hits.incrementAndGet());
        assertEquals(1, hits.get());
    }

    @Test
    void frozenChunkIndexIgnoresLiveClears() {
        ChunkEntityBroadphaseIndex liveChunks = new ChunkEntityBroadphaseIndex();
        liveChunks.updateEntityChunks(77,
            BroadphaseSlot.aggregate(77, new AABB(0, 0, 0, 20, 20, 20), 3L));
        MultipartFrozenChunkBroadphaseIndex frozen = MultipartFrozenChunkBroadphaseIndex.copyOf(liveChunks);
        liveChunks.clear();

        Set<Integer> inside = frozen.entityIdsOverlappingChunks(new AABB(0, 0, 0, 4, 4, 4));
        assertEquals(Set.of(77), inside);
    }
}
