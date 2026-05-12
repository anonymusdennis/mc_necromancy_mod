package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpatialMultipartBroadphaseTierTest {

    @Test
    void countCellsOverlappingMatchesForEachCellCount() {
        AABB box = new AABB(0, 0, 0, 257, 4, 4);
        double cs = 4.0;
        int counted = SpatialCellKey.countCellsOverlapping(box, cs);
        AtomicInteger visits = new AtomicInteger();
        SpatialCellKey.forEachCellOverlapping(box, cs, k -> visits.incrementAndGet());
        assertEquals(counted, visits.get());
        assertEquals(65, counted);
    }

    @Test
    void largeFlatMobUsesMacroTierNotFineDuplication() {
        var index = new SpatialHashMultipartIndex(4.0, MultipartBroadphaseSpatialPolicy.DEFAULT);
        var slot = BroadphaseSlot.aggregate(7001, new AABB(0, 0, 0, 257, 4, 4), 1L);
        index.insertSlots(List.of(slot));

        assertTrue(index.debugEntityIndexedInMacro(slot.entityId()));
        assertFalse(index.debugEntityIndexedInFine(slot.entityId()));
        assertFalse(index.debugEntityIndexedInOversized(slot.entityId()));

        List<BroadphaseSlot> hits = new ArrayList<>();
        index.forEachSlotOverlapping(new AABB(128, 1, 1, 129, 3, 3), hits::add);
        assertEquals(List.of(slot), hits);
    }

    @Test
    void colossalBoundsFallBackToOversizedBucket() {
        var index = new SpatialHashMultipartIndex(4.0, MultipartBroadphaseSpatialPolicy.DEFAULT);
        var slot = BroadphaseSlot.aggregate(9001, new AABB(-16, -16, -16, 512, 512, 512), 2L);
        index.insertSlots(List.of(slot));

        assertTrue(SpatialCellKey.countCellsOverlapping(slot.bounds(), index.macroCellSize())
            > MultipartBroadphaseSpatialPolicy.DEFAULT.maxMacroCellsPerSlot());

        assertFalse(index.debugEntityIndexedInFine(slot.entityId()));
        assertFalse(index.debugEntityIndexedInMacro(slot.entityId()));
        assertTrue(index.debugEntityIndexedInOversized(slot.entityId()));

        List<BroadphaseSlot> hits = new ArrayList<>();
        index.forEachSlotOverlapping(new AABB(250, 250, 250, 251, 251, 251), hits::add);
        assertEquals(List.of(slot), hits);
    }

    @Test
    void chunkFilteredOversizedPredicateSkipsEntity() {
        var index = new SpatialHashMultipartIndex(4.0, MultipartBroadphaseSpatialPolicy.DEFAULT);
        var kept = BroadphaseSlot.aggregate(11, new AABB(-16, -16, -16, 512, 512, 512), 3L);
        var skipped = BroadphaseSlot.aggregate(22, new AABB(-16, -16, -16, 512, 512, 512), 4L);
        index.insertSlots(List.of(kept, skipped));

        List<BroadphaseSlot> hits = new ArrayList<>();
        AABB q = new AABB(250, 250, 250, 251, 251, 251);
        index.forEachSlotOverlapping(q, id -> id == 11, hits::add);
        assertEquals(List.of(kept), hits);
    }
}
