package com.sirolf2009.necromancy.altar.mock;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.SequencedMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>Paper-only mock for the future altar <strong>3×3 attachment grid</strong> (ideas.md idea&nbsp;2). No gameplay wiring yet —
 * {@link AltarAttachmentGridMock} holds slot intent + locking flags so UI coders can prototype without touching ritual slots.</p>
 *
 * <p><strong>Deadlock sketch (RC-F1):</strong> {@link AltarAttachmentGridMock#simulateInsertSequence} processes a FIFO queue on a single thread,
 * acquiring only one grid lock per step — multi-grid surgical editors should mirror {@link com.sirolf2009.necromancy.multipart.editor.session.MultipartServerTopologyEditService}
 * ordering instead of nested grid locks.</p>
 */
public final class AltarAttachmentGridMock {

    public enum GridSlotState {
        /** Socket accepts inserts. */
        OPEN,
        /** Socket locked until neighbour resolves (grey endpoint). */
        LOCKED_PENDING,
        /** Occupied — requires explicit removal op. */
        OCCUPIED
    }

    /** Row-major 0..8 — centre reserved for ritual pivot in future UX. */
    public record SlotMock(int index, GridSlotState state, ResourceLocation occupiedPartId) {}

    /**
     * Nearest-socket heuristic spec (ideas.md lines&nbsp;133–135): rank candidates by Manhattan distance on the 3×3 grid,
     * break ties by attachment priority from {@link com.sirolf2009.necromancy.bodypart.BodypartAttachmentJson#priority}.
     */
    public static int nearestOpenSlot(int insertFromEdgeIndex, List<SlotMock> snapshot) {
        int best = -1;
        int bestDist = Integer.MAX_VALUE;
        for (SlotMock s : snapshot) {
            if (s.state != GridSlotState.OPEN) continue;
            int dr = Math.abs((insertFromEdgeIndex / 3) - (s.index / 3));
            int dc = Math.abs((insertFromEdgeIndex % 3) - (s.index % 3));
            int d = dr + dc;
            if (d < bestDist) {
                bestDist = d;
                best = s.index;
            }
        }
        return best;
    }

    /** Lightweight queue replay for deadlock sanity — caller supplies externally locked snapshots only. */
    public static void simulateInsertSequence(SequencedMap<Integer, ResourceLocation> orderedInserts) {
        ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<>();
        orderedInserts.keySet().forEach(q::add);
        while (!q.isEmpty()) {
            q.poll();
            // Real integration would validate topology leases here — keep RC-F1 paper-only.
        }
    }

    private AltarAttachmentGridMock() {}
}
