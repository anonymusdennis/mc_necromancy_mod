package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** Shared merged broad-phase queries for snapshots that overlay staged pending slots on flushed spatial tiers. */
final class MultipartBroadphaseQueryMerger {

    private record DedupeKey(int entityId, @Nullable ResourceLocation partId, boolean aggregate) {}

    private MultipartBroadphaseQueryMerger() {
    }

    static double raySweepPadding(MultipartFrozenSpatialIndex main, @Nullable MultipartFrozenSpatialIndex pending) {
        double pad = Math.max(main.cellSize(), main.macroCellSize() * 0.5);
        if (pending != null) {
            pad = Math.max(pad, Math.max(pending.cellSize(), pending.macroCellSize() * 0.5));
        }
        return pad;
    }

    static void forEachCandidateOverlapping(
        MultipartFrozenSpatialIndex mainSpatial,
        @Nullable MultipartFrozenSpatialIndex pendingSpatial,
        MultipartFrozenChunkBroadphaseIndex chunks,
        AABB query,
        boolean chunkPrefilter,
        Consumer<BroadphaseSlot> consumer
    ) {
        Set<Integer> allowed = null;
        if (chunkPrefilter) {
            allowed = chunks.entityIdsOverlappingChunks(query);
            if (allowed.isEmpty()) {
                return;
            }
        }
        Set<DedupeKey> dedupe = new HashSet<>();
        Set<Integer> finalAllowed = allowed;
        visitMergedFiltered(mainSpatial, pendingSpatial, query, finalAllowed, dedupe, consumer);
    }

    static List<BroadphaseSlot> collectOverlappingCandidates(
        MultipartFrozenSpatialIndex mainSpatial,
        @Nullable MultipartFrozenSpatialIndex pendingSpatial,
        MultipartFrozenChunkBroadphaseIndex chunks,
        AABB query,
        boolean chunkPrefilter
    ) {
        List<BroadphaseSlot> out = new ArrayList<>();
        forEachCandidateOverlapping(mainSpatial, pendingSpatial, chunks, query, chunkPrefilter, out::add);
        return out;
    }

    private static void visitMergedFiltered(
        MultipartFrozenSpatialIndex mainSpatial,
        @Nullable MultipartFrozenSpatialIndex pendingSpatial,
        AABB query,
        @Nullable Set<Integer> allowedIds,
        Set<DedupeKey> dedupe,
        Consumer<BroadphaseSlot> consumer
    ) {
        java.util.function.IntPredicate oversizedFilter = allowedIds != null ? allowedIds::contains : null;
        Consumer<BroadphaseSlot> sink = slot -> {
            if (allowedIds != null && !allowedIds.contains(slot.entityId())) {
                return;
            }
            DedupeKey key = new DedupeKey(slot.entityId(), slot.partId(), slot.aggregate());
            if (dedupe.add(key)) {
                consumer.accept(slot);
            }
        };
        mainSpatial.forEachSlotOverlapping(query, oversizedFilter, sink);
        if (pendingSpatial != null) {
            pendingSpatial.forEachSlotOverlapping(query, oversizedFilter, sink);
        }
    }
}
