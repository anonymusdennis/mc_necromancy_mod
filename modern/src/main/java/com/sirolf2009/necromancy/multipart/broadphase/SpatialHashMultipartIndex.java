package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.LongConsumer;

/**
 * Tiered spatial index for multipart slots:
 * <ul>
 *   <li>Fine uniform grid for normal-sized bounds (efficient neighborhood queries).</li>
 *   <li>Coarser macro grid for very large single bounds without filling every fine cell.</li>
 *   <li>Oversized bucket (per entity, bounded by slot count) when both grids would still explode.</li>
 * </ul>
 */
public final class SpatialHashMultipartIndex {

    private final double cellSize;
    private final double macroCellSize;
    private final MultipartBroadphaseSpatialPolicy policy;

    private final Map<Long, List<BroadphaseSlot>> fineCells = new HashMap<>();
    private final Map<Integer, Set<Long>> entityFineCells = new HashMap<>();

    private final Map<Long, List<BroadphaseSlot>> macroCells = new HashMap<>();
    private final Map<Integer, Set<Long>> entityMacroCells = new HashMap<>();

    private final Map<Integer, List<BroadphaseSlot>> oversizedByEntity = new HashMap<>();

    public SpatialHashMultipartIndex(double cellSize) {
        this(cellSize, MultipartBroadphaseSpatialPolicy.DEFAULT);
    }

    public SpatialHashMultipartIndex(double cellSize, MultipartBroadphaseSpatialPolicy policy) {
        if (cellSize <= 1e-9) {
            throw new IllegalArgumentException("cellSize must be positive");
        }
        this.cellSize = cellSize;
        this.policy = policy;
        this.macroCellSize = policy.macroCellSize(cellSize);
        if (macroCellSize <= cellSize + 1e-9) {
            throw new IllegalArgumentException("macro cell must be strictly larger than fine cell");
        }
    }

    public double cellSize() {
        return cellSize;
    }

    /** Coarse tier cell extent (world units). */
    public double macroCellSize() {
        return macroCellSize;
    }

    public MultipartBroadphaseSpatialPolicy policy() {
        return policy;
    }

    public void clear() {
        fineCells.clear();
        entityFineCells.clear();
        macroCells.clear();
        entityMacroCells.clear();
        oversizedByEntity.clear();
    }

    public void removeEntity(int entityId) {
        removeTier(entityId, entityFineCells, fineCells);
        removeTier(entityId, entityMacroCells, macroCells);
        oversizedByEntity.remove(entityId);
    }

    private static void removeTier(int entityId, Map<Integer, Set<Long>> entityCells, Map<Long, List<BroadphaseSlot>> cells) {
        Set<Long> keys = entityCells.remove(entityId);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (long key : keys) {
            List<BroadphaseSlot> list = cells.get(key);
            if (list == null) continue;
            list.removeIf(s -> s.entityId() == entityId);
            if (list.isEmpty()) {
                cells.remove(key);
            }
        }
    }

    public void insertSlots(List<BroadphaseSlot> slots) {
        for (BroadphaseSlot s : slots) {
            insertOne(s);
        }
    }

    private void insertOne(BroadphaseSlot s) {
        AABB b = s.bounds();
        int fineCount = SpatialCellKey.countCellsOverlapping(b, cellSize);
        if (fineCount <= policy.maxFineCellsPerSlot()) {
            insertIntoGrid(s, cellSize, fineCells, entityFineCells);
            return;
        }
        int macroCount = SpatialCellKey.countCellsOverlapping(b, macroCellSize);
        if (macroCount <= policy.maxMacroCellsPerSlot()) {
            insertIntoGrid(s, macroCellSize, macroCells, entityMacroCells);
            return;
        }
        oversizedByEntity.computeIfAbsent(s.entityId(), k -> new ArrayList<>(4)).add(s);
    }

    private static void insertIntoGrid(
        BroadphaseSlot s,
        double gridCellSize,
        Map<Long, List<BroadphaseSlot>> cells,
        Map<Integer, Set<Long>> entityCells
    ) {
        SpatialCellKey.forEachCellOverlapping(s.bounds(), gridCellSize, key -> {
            cells.computeIfAbsent(key, k -> new ArrayList<>(4)).add(s);
            entityCells.computeIfAbsent(s.entityId(), k -> new HashSet<>()).add(key);
        });
    }

    /** Visits fine tier, macro tier, and oversized buckets (see overload for oversized chunk filtering). */
    public void forEachSlotOverlapping(AABB query, Consumer<BroadphaseSlot> consumer) {
        forEachSlotOverlapping(query, null, consumer);
    }

    /**
     * Visits slots whose bounds may intersect {@code query}.
     *
     * @param oversizedEntityFilter when non-null, oversized-bucket slots are only emitted when the predicate accepts
     *                              {@link BroadphaseSlot#entityId()} (e.g. chunk prefilter). Fine and macro tiers ignore this filter.
     */
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

    /** Cells touched by the broad-phase query on the fine grid (for debug draw / profiling). */
    public void forEachCellKeyOverlapping(AABB query, LongConsumer consumer) {
        SpatialCellKey.forEachCellOverlapping(query, cellSize, consumer::accept);
    }

    /** Cells touched on the macro grid (for debug draw / profiling). */
    public void forEachMacroCellKeyOverlapping(AABB query, LongConsumer consumer) {
        SpatialCellKey.forEachCellOverlapping(query, macroCellSize, consumer::accept);
    }

    /** Same-package tests / diagnostics — whether {@code entityId} has slots stored in each tier. */
    boolean debugEntityIndexedInFine(int entityId) {
        Set<Long> s = entityFineCells.get(entityId);
        return s != null && !s.isEmpty();
    }

    boolean debugEntityIndexedInMacro(int entityId) {
        Set<Long> s = entityMacroCells.get(entityId);
        return s != null && !s.isEmpty();
    }

    boolean debugEntityIndexedInOversized(int entityId) {
        List<BroadphaseSlot> l = oversizedByEntity.get(entityId);
        return l != null && !l.isEmpty();
    }

    Map<Long, List<BroadphaseSlot>> fineCellsForSnapshotCopy() {
        return fineCells;
    }

    Map<Long, List<BroadphaseSlot>> macroCellsForSnapshotCopy() {
        return macroCells;
    }

    Map<Integer, List<BroadphaseSlot>> oversizedForSnapshotCopy() {
        return oversizedByEntity;
    }
}
