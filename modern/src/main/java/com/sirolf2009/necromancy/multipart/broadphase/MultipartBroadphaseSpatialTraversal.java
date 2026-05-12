package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

/** Shared spatial-hash visitation for live indices and immutable snapshot copies. */
final class MultipartBroadphaseSpatialTraversal {

    private MultipartBroadphaseSpatialTraversal() {
    }

    static void forEachSlotOverlapping(
        Map<Long, List<BroadphaseSlot>> fineCells,
        Map<Long, List<BroadphaseSlot>> macroCells,
        Map<Integer, List<BroadphaseSlot>> oversizedByEntity,
        double cellSize,
        double macroCellSize,
        AABB query,
        @Nullable IntPredicate oversizedEntityFilter,
        Consumer<BroadphaseSlot> consumer
    ) {
        SpatialCellKey.forEachCellOverlapping(query, cellSize, key -> {
            List<BroadphaseSlot> list = fineCells.get(key);
            if (list == null) return;
            for (BroadphaseSlot s : list) {
                if (s.bounds().intersects(query)) {
                    consumer.accept(s);
                }
            }
        });
        SpatialCellKey.forEachCellOverlapping(query, macroCellSize, key -> {
            List<BroadphaseSlot> list = macroCells.get(key);
            if (list == null) return;
            for (BroadphaseSlot s : list) {
                if (s.bounds().intersects(query)) {
                    consumer.accept(s);
                }
            }
        });
        if (oversizedByEntity.isEmpty()) {
            return;
        }
        for (Map.Entry<Integer, List<BroadphaseSlot>> e : oversizedByEntity.entrySet()) {
            if (oversizedEntityFilter != null && !oversizedEntityFilter.test(e.getKey())) {
                continue;
            }
            for (BroadphaseSlot s : e.getValue()) {
                if (s.bounds().intersects(query)) {
                    consumer.accept(s);
                }
            }
        }
    }
}
