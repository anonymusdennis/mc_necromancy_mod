package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultipartBroadphaseQueryMergerTest {

    @Test
    void mergedTraversalDedupesAcrossLayers() {
        SpatialHashMultipartIndex a = new SpatialHashMultipartIndex(4.0);
        SpatialHashMultipartIndex b = new SpatialHashMultipartIndex(4.0);
        var slot = BroadphaseSlot.part(1, ResourceLocation.parse("necromancy:arm"), new AABB(0, 0, 0, 1, 1, 1), 9L);
        a.insertSlots(List.of(slot));
        b.insertSlots(List.of(slot));

        MultipartFrozenSpatialIndex fa = MultipartFrozenSpatialIndex.copyOf(a);
        MultipartFrozenSpatialIndex fb = MultipartFrozenSpatialIndex.copyOf(b);
        MultipartFrozenChunkBroadphaseIndex chunks = MultipartFrozenChunkBroadphaseIndex.copyOf(new ChunkEntityBroadphaseIndex());

        List<BroadphaseSlot> hits = new ArrayList<>();
        MultipartBroadphaseQueryMerger.forEachCandidateOverlapping(fa, fb, chunks, new AABB(-2, -2, -2, 4, 4, 4), false, hits::add);
        assertEquals(1, hits.size());
        assertEquals(slot, hits.getFirst());
    }

    @Test
    void deferredFreezePreservesOverlappingQueries() {
        Map<Integer, List<BroadphaseSlot>> staged = new HashMap<>();
        staged.put(9, List.of(BroadphaseSlot.aggregate(9, new AABB(2, 2, 2, 3, 3, 3), 1L)));
        MultipartFrozenSpatialIndex pending = MultipartFrozenSpatialIndex.freezePendingSlotLists(staged.values(), 4.0, MultipartBroadphaseSpatialPolicy.DEFAULT);

        Set<BroadphaseSlot> hits = new HashSet<>();
        pending.forEachSlotOverlapping(new AABB(2.5, 2.5, 2.5, 2.6, 2.6, 2.6), hits::add);
        assertEquals(1, hits.size());
    }
}
