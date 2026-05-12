package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

/**
 * Immutable broad-phase spatial tiers ({@link BroadphaseSlot} lists are unmodifiable; maps are unmodifiable).
 * Safe to query from worker / render threads while the live {@link SpatialHashMultipartIndex} advances on the tick thread.
 */
public final class MultipartFrozenSpatialIndex {

    private final Map<Long, List<BroadphaseSlot>> fineCells;
    private final Map<Long, List<BroadphaseSlot>> macroCells;
    private final Map<Integer, List<BroadphaseSlot>> oversizedByEntity;
    private final double cellSize;
    private final double macroCellSize;

    MultipartFrozenSpatialIndex(
        Map<Long, List<BroadphaseSlot>> fineCells,
        Map<Long, List<BroadphaseSlot>> macroCells,
        Map<Integer, List<BroadphaseSlot>> oversizedByEntity,
        double cellSize,
        double macroCellSize
    ) {
        this.fineCells = fineCells;
        this.macroCells = macroCells;
        this.oversizedByEntity = oversizedByEntity;
        this.cellSize = cellSize;
        this.macroCellSize = macroCellSize;
    }

    static MultipartFrozenSpatialIndex copyOf(SpatialHashMultipartIndex src) {
        Map<Long, List<BroadphaseSlot>> ff = copyCellMap(src.fineCellsForSnapshotCopy());
        Map<Long, List<BroadphaseSlot>> mm = copyCellMap(src.macroCellsForSnapshotCopy());
        Map<Integer, List<BroadphaseSlot>> oo = new HashMap<>(src.oversizedForSnapshotCopy().size());
        for (var e : src.oversizedForSnapshotCopy().entrySet()) {
            oo.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return new MultipartFrozenSpatialIndex(
            Map.copyOf(ff),
            Map.copyOf(mm),
            Map.copyOf(oo),
            src.cellSize(),
            src.macroCellSize()
        );
    }

    /**
     * Builds a frozen index from the latest staged slot lists (operation-table / deferred publish path).
     */
    static MultipartFrozenSpatialIndex freezePendingSlotLists(
        Iterable<List<BroadphaseSlot>> slotLists,
        double cellSize,
        MultipartBroadphaseSpatialPolicy policy
    ) {
        SpatialHashMultipartIndex tmp = new SpatialHashMultipartIndex(cellSize, policy);
        for (List<BroadphaseSlot> list : slotLists) {
            tmp.insertSlots(list);
        }
        return copyOf(tmp);
    }

    private static Map<Long, List<BroadphaseSlot>> copyCellMap(Map<Long, List<BroadphaseSlot>> src) {
        Map<Long, List<BroadphaseSlot>> out = HashMap.newHashMap(src.size());
        for (var e : src.entrySet()) {
            out.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return out;
    }

    public double cellSize() {
        return cellSize;
    }

    public double macroCellSize() {
        return macroCellSize;
    }

    public void forEachSlotOverlapping(AABB query, Consumer<BroadphaseSlot> consumer) {
        forEachSlotOverlapping(query, null, consumer);
    }

    public void forEachSlotOverlapping(AABB query, @Nullable IntPredicate oversizedEntityFilter, Consumer<BroadphaseSlot> consumer) {
        MultipartBroadphaseSpatialTraversal.forEachSlotOverlapping(
            fineCells,
            macroCells,
            oversizedByEntity,
            cellSize,
            macroCellSize,
            query,
            oversizedEntityFilter,
            consumer
        );
    }
}
